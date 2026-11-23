package com.example.cryonix.lakes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.LevelBadgeHelper;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.StaffBottomBarHelper;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.AlertLevel;
import com.example.cryonix.data.model.Lake;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A21 Lakes (LM-04, DR-02). Staff roles with VIEW_LAKES only.
 * - List or map.
 * - Filter by level, district and size; search by name or valley; sort.
 * - Each row shows level, lake ID, area, change and last update.
 * Organisation staff see only the lakes in their organisation's valleys.
 */
public class LakesListActivity extends AppCompatActivity {

    /** Optional: open the list already filtered to one level (AlertLevel name). */
    public static final String EXTRA_LEVEL = "level";

    // Positions in the spinners (same order as the string arrays)
    private static final int DISTRICT_ALL = 0;
    private static final int DISTRICT_GHIZER = 1;
    private static final int SIZE_ANY = 0;
    private static final int SIZE_SMALL = 1;
    private static final int SIZE_MEDIUM = 2;
    private static final int SORT_LEVEL = 0;
    private static final int SORT_AREA = 1;
    private static final int SORT_CHANGE = 2;

    private LinearLayout layoutLakesList;
    private LinearLayout layoutLakesMap;
    private LinearLayout layoutLakesFilters;
    private TextView textLakesEmpty;
    private TextInputEditText editLakesSearch;
    private Spinner spinnerLakesDistrict;
    private Spinner spinnerLakesSize;
    private Spinner spinnerLakesSort;
    private LakesMapView mapLakes;

    private AlertLevel selectedLevel = null;   // null = All
    private boolean isMapShown = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.VIEW_LAKES)) {
            return;
        }
        setContentView(R.layout.activity_lakes_list);
        TopBarHelper.setupWithoutBack(this, R.string.lakes_title);
        StaffBottomBarHelper.setup(this, StaffBottomBarHelper.TAB_LAKES);

        sessionManager = new SessionManager(this);
        layoutLakesList = findViewById(R.id.layoutLakesList);
        layoutLakesMap = findViewById(R.id.layoutLakesMap);
        layoutLakesFilters = findViewById(R.id.layoutLakesFilters);
        textLakesEmpty = findViewById(R.id.textLakesEmpty);
        editLakesSearch = findViewById(R.id.editLakesSearch);
        spinnerLakesDistrict = findViewById(R.id.spinnerLakesDistrict);
        spinnerLakesSize = findViewById(R.id.spinnerLakesSize);
        spinnerLakesSort = findViewById(R.id.spinnerLakesSort);
        mapLakes = findViewById(R.id.mapLakes);
        MaterialButtonToggleGroup toggleLakesView = findViewById(R.id.toggleLakesView);

        String levelName = getIntent().getStringExtra(EXTRA_LEVEL);
        if (levelName != null) {
            selectedLevel = AlertLevel.valueOf(levelName);
        }

        setupSpinner(spinnerLakesDistrict, R.array.lakes_district_choices);
        setupSpinner(spinnerLakesSize, R.array.lakes_size_choices);
        setupSpinner(spinnerLakesSort, R.array.lakes_sort_choices);

        toggleLakesView.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    isMapShown = (checkedId == R.id.buttonLakesViewMap);
                    showLakes();
                }
            }
        });

        mapLakes.setOnLakeTapListener(new LakesMapView.OnLakeTapListener() {
            @Override
            public void onLakeTapped(Lake lake) {
                openLake(lake);
            }
        });

        editLakesSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showLakes();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void setupSpinner(Spinner spinner, int arrayId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, arrayId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showLakes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (layoutLakesList != null) {
            showFilters();
            showLakes();
        }
    }

    private void showFilters() {
        layoutLakesFilters.removeAllViews();
        addFilterChip(null, getString(R.string.lakes_filter_all));
        for (AlertLevel level : AlertLevel.lakeLevels()) {
            addFilterChip(level, level.getLabel());
        }
    }

    private void addFilterChip(final AlertLevel level, String label) {
        TextView chip = new TextView(this);
        boolean isSelected = (level == selectedLevel);

        if (isSelected && level != null) {
            LevelBadgeHelper.show(chip, level);
        } else {
            chip.setText(label);
            chip.setBackgroundResource(isSelected ? R.drawable.bg_chip : R.drawable.bg_list_row);
            chip.setTextColor(ContextCompat.getColor(this, isSelected ? R.color.color_primary : R.color.color_text_secondary));
            chip.setPadding(dp(12), dp(6), dp(12), dp(6));
        }
        chip.setTextSize(14);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(dp(8));
        chip.setLayoutParams(params);

        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedLevel = level;
                showFilters();
                showLakes();
            }
        });
        layoutLakesFilters.addView(chip);
    }

    /** Lakes that pass search, level, district and size, in the chosen order. */
    private List<Lake> getFilteredLakes() {
        String search = editLakesSearch.getText() == null ? "" : editLakesSearch.getText().toString().trim().toLowerCase();
        int district = spinnerLakesDistrict.getSelectedItemPosition();
        int size = spinnerLakesSize.getSelectedItemPosition();

        List<Lake> result = new ArrayList<>();
        for (Lake lake : FakeDatabase.getLakesFor(sessionManager.getUserRole(), sessionManager.getUserEmail())) {
            boolean levelMatches = selectedLevel == null || lake.getLevel() == selectedLevel;
            boolean searchMatches = lake.getName().toLowerCase().contains(search)
                    || lake.getValley().toLowerCase().contains(search)
                    || lake.getId().toLowerCase().contains(search);

            boolean districtMatches = district == DISTRICT_ALL
                    || (district == DISTRICT_GHIZER ? lake.getDistrict().equals("Ghizer") : lake.getDistrict().equals("Chitral"));

            double area = lake.getAreaHectares();
            boolean sizeMatches;
            if (size == SIZE_ANY) {
                sizeMatches = true;
            } else if (size == SIZE_SMALL) {
                sizeMatches = area < 5;
            } else if (size == SIZE_MEDIUM) {
                sizeMatches = area >= 5 && area <= 10;
            } else {
                sizeMatches = area > 10;
            }

            if (levelMatches && searchMatches && districtMatches && sizeMatches) {
                result.add(lake);
            }
        }
        sortLakes(result, spinnerLakesSort.getSelectedItemPosition());
        return result;
    }

    /** Simple bubble sort, fine for a few dozen lakes. */
    private void sortLakes(List<Lake> list, int sortChoice) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                if (shouldSwap(list.get(j), list.get(j + 1), sortChoice)) {
                    Lake temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }

    private boolean shouldSwap(Lake first, Lake second, int sortChoice) {
        if (sortChoice == SORT_LEVEL) {
            if (first.getLevel() != second.getLevel()) {
                return first.getLevel().ordinal() < second.getLevel().ordinal();   // Warning first
            }
            return !first.isFlagged() && second.isFlagged();                         // then flagged first
        } else if (sortChoice == SORT_AREA) {
            return first.getAreaHectares() < second.getAreaHectares();
        } else if (sortChoice == SORT_CHANGE) {
            return first.getAreaChangePercent() < second.getAreaChangePercent();
        }
        return first.getName().compareToIgnoreCase(second.getName()) > 0;
    }

    private void showLakes() {
        List<Lake> lakeList = getFilteredLakes();
        textLakesEmpty.setVisibility(lakeList.isEmpty() ? View.VISIBLE : View.GONE);

        // Map and list both show the same filtered lakes
        layoutLakesMap.setVisibility(isMapShown ? View.VISIBLE : View.GONE);
        mapLakes.setLakes(lakeList);

        layoutLakesList.removeAllViews();
        for (final Lake lake : lakeList) {
            View rowView = makeLakeRow(this, layoutLakesList, lake);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openLake(lake);
                }
            });
            layoutLakesList.addView(rowView);
        }
    }

    private void openLake(Lake lake) {
        Intent intent = new Intent(this, LakeDetailActivity.class);
        intent.putExtra(LakeDetailActivity.EXTRA_LAKE_ID, lake.getId());
        startActivity(intent);
    }

    /** Builds one lake row. Also used by the staff home ("Lakes to review"). */
    public static View makeLakeRow(Context context, ViewGroup parent, Lake lake) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.item_lake_row, parent, false);
        TextView textLakeRowLevel = rowView.findViewById(R.id.textLakeRowLevel);
        TextView textLakeRowFlagged = rowView.findViewById(R.id.textLakeRowFlagged);
        TextView textLakeRowName = rowView.findViewById(R.id.textLakeRowName);
        TextView textLakeRowDetails = rowView.findViewById(R.id.textLakeRowDetails);

        LevelBadgeHelper.show(textLakeRowLevel, lake.getLevel());
        textLakeRowFlagged.setVisibility(lake.isFlagged() ? View.VISIBLE : View.GONE);
        textLakeRowName.setText(lake.getName());
        String details = context.getString(R.string.lakes_row_details, lake.getValley(),
                formatNumber(lake.getAreaHectares()), formatChange(lake.getAreaChangePercent()));
        String updated = context.getString(R.string.lakes_row_updated, lake.getId(), lake.getLastImageDate());
        textLakeRowDetails.setText(details + "\n" + updated);
        return rowView;
    }

    public static String formatNumber(double value) {
        return String.format(Locale.ENGLISH, "%.1f", value);
    }

    /** +6.2% or -0.5% */
    public static String formatChange(double percent) {
        return String.format(Locale.ENGLISH, "%+.1f%%", percent);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
