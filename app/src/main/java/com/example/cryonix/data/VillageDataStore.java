package com.example.cryonix.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.cryonix.common.NetworkHelper;
import com.example.cryonix.data.model.Alert;
import com.example.cryonix.data.model.AlertLevel;
import com.example.cryonix.data.model.Lake;
import com.example.cryonix.data.model.Shelter;
import com.example.cryonix.data.model.VillageAlert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Offline storage of the village pack (CA-05).
 *
 * getPack():
 *   - online  -> download a fresh pack (today: from FakeDatabase, later: from the API), save it, return it
 *   - offline -> return the pack saved on the phone (or null if it was never downloaded)
 *
 * The pack is saved as JSON text in SharedPreferences.
 */
public class VillageDataStore {

    private static final String PREFS_NAME = "cryonix_village_pack";
    private static final String KEY_PACK_JSON = "pack_json";

    public static VillagePack getPack(Context context, String villageName) {
        if (NetworkHelper.isOnline(context)) {
            VillagePack freshPack = downloadPack(villageName);
            savePack(context, freshPack);
            return freshPack;
        }

        VillagePack savedPack = loadSavedPack(context);
        // A saved pack for another village is no use (the user changed village while offline)
        if (savedPack != null && savedPack.villageName.equals(villageName)) {
            return savedPack;
        }
        return null;
    }

    // ---------------- "Download" ----------------

    /** TODO: replace with a call to GET /api/villages/{name}/pack when the backend is ready. */
    private static VillagePack downloadPack(String villageName) {
        VillagePack pack = new VillagePack();
        pack.villageName = villageName;
        pack.syncedAt = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).format(new Date());

        Lake lake = FakeDatabase.getLakeAboveVillage(villageName);
        if (lake != null) {
            pack.lakeName = lake.getName();
            pack.lakeLevel = lake.getLevel();
            pack.lakeCheckedOn = lake.getLastImageDate();
        }

        for (Alert alert : FakeDatabase.getAlertsForVillage(villageName)) {
            pack.alerts.add(new VillageAlert(alert.getId(), alert.getLevel(), alert.getLakeName(),
                    alert.getMessage(), alert.getTimeText(), alert.isActive()));
        }
        pack.shelters.addAll(FakeDatabase.getSheltersForVillage(villageName));
        return pack;
    }

    // ---------------- Save and load ----------------

    private static void savePack(Context context, VillagePack pack) {
        try {
            String jsonText = toJson(pack).toString();
            getPrefs(context).edit().putString(KEY_PACK_JSON, jsonText).apply();
        } catch (JSONException e) {
            // Nothing saved; the app will try again next time it is online
        }
    }

    private static VillagePack loadSavedPack(Context context) {
        String jsonText = getPrefs(context).getString(KEY_PACK_JSON, null);
        if (jsonText == null) {
            return null;
        }
        try {
            return fromJson(new JSONObject(jsonText));
        } catch (JSONException e) {
            return null;
        }
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ---------------- JSON ----------------

    private static JSONObject toJson(VillagePack pack) throws JSONException {
        JSONObject root = new JSONObject();
        root.put("village", pack.villageName);
        root.put("syncedAt", pack.syncedAt);
        root.put("lakeName", pack.lakeName);
        root.put("lakeLevel", pack.lakeLevel.name());
        root.put("lakeCheckedOn", pack.lakeCheckedOn);

        JSONArray alertArray = new JSONArray();
        for (VillageAlert alert : pack.alerts) {
            JSONObject item = new JSONObject();
            item.put("id", alert.getId());
            item.put("level", alert.getLevel().name());
            item.put("lake", alert.getLakeName());
            item.put("message", alert.getMessage());
            item.put("time", alert.getTime());
            item.put("active", alert.isActive());
            alertArray.put(item);
        }
        root.put("alerts", alertArray);

        JSONArray shelterArray = new JSONArray();
        for (Shelter shelter : pack.shelters) {
            JSONObject item = new JSONObject();
            item.put("id", shelter.getId());
            item.put("name", shelter.getName());
            item.put("village", shelter.getVillage());
            item.put("distance", shelter.getDistanceMetres());
            item.put("minutes", shelter.getWalkingMinutes());
            item.put("capacity", shelter.getCapacity());
            item.put("inside", shelter.getPeopleInside());
            item.put("facilities", new JSONArray(shelter.getFacilities()));
            item.put("steps", new JSONArray(shelter.getRouteSteps()));
            item.put("x", (double) shelter.getMapX());
            item.put("y", (double) shelter.getMapY());
            JSONArray pointArray = new JSONArray();
            for (float point : shelter.getRoutePoints()) {
                pointArray.put((double) point);
            }
            item.put("points", pointArray);
            shelterArray.put(item);
        }
        root.put("shelters", shelterArray);
        return root;
    }

    private static VillagePack fromJson(JSONObject root) throws JSONException {
        VillagePack pack = new VillagePack();
        pack.villageName = root.getString("village");
        pack.syncedAt = root.getString("syncedAt");
        pack.lakeName = root.getString("lakeName");
        pack.lakeLevel = AlertLevel.valueOf(root.getString("lakeLevel"));
        pack.lakeCheckedOn = root.getString("lakeCheckedOn");

        JSONArray alertArray = root.getJSONArray("alerts");
        for (int i = 0; i < alertArray.length(); i++) {
            JSONObject item = alertArray.getJSONObject(i);
            pack.alerts.add(new VillageAlert(item.getInt("id"), AlertLevel.valueOf(item.getString("level")),
                    item.getString("lake"), item.getString("message"), item.getString("time"),
                    item.getBoolean("active")));
        }

        JSONArray shelterArray = root.getJSONArray("shelters");
        for (int i = 0; i < shelterArray.length(); i++) {
            JSONObject item = shelterArray.getJSONObject(i);
            JSONArray pointArray = item.getJSONArray("points");
            float[] points = new float[pointArray.length()];
            for (int p = 0; p < pointArray.length(); p++) {
                points[p] = (float) pointArray.getDouble(p);
            }
            pack.shelters.add(new Shelter(item.getString("id"), item.getString("name"), item.getString("village"),
                    item.getInt("distance"), item.getInt("minutes"), item.getInt("capacity"), item.getInt("inside"),
                    toStringList(item.getJSONArray("facilities")), toStringList(item.getJSONArray("steps")),
                    (float) item.getDouble("x"), (float) item.getDouble("y"), points));
        }
        return pack;
    }

    private static List<String> toStringList(JSONArray array) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getString(i));
        }
        return list;
    }
}
