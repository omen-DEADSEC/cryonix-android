package com.example.cryonix.data;

import com.example.cryonix.common.UserRole;
import com.example.cryonix.data.model.AccountRequest;
import com.example.cryonix.data.model.Alert;
import com.example.cryonix.data.model.AlertLevel;
import com.example.cryonix.data.model.Lake;
import com.example.cryonix.data.model.NetworkNode;
import com.example.cryonix.data.model.Shelter;
import com.example.cryonix.data.model.StaffNotice;
import com.example.cryonix.data.model.AuditEntry;
import com.example.cryonix.data.model.FloodScenario;
import com.example.cryonix.data.model.SettlementArrival;
import com.example.cryonix.data.model.Organisation;
import com.example.cryonix.data.model.RegisteredPhone;
import com.example.cryonix.data.model.StaffAccount;
import com.example.cryonix.data.model.Village;
import com.example.cryonix.data.model.VillageTimeMargin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * DEMO DATA ONLY.
 * The backend (Django API) is not ready yet, so the app keeps sample data in memory here.
 * Changes are lost when the app is closed. Later every method here will call the API instead.
 *
 * Demo staff accounts (password for all: Cryonix@123):
 *   admin@cryonix.pk    - System administrator
 *   officer@gbdma.gov.pk - Authority officer
 *   staff@akah.org      - Organisation staff
 * Demo one-time code for phone sign-up: 123456
 */
public class FakeDatabase {

    public static final String DEMO_PASSWORD = "Cryonix@123";
    public static final String DEMO_ONE_TIME_CODE = "123456";

    // EV-02 time budget (team targets, to be confirmed with the supervisor)
    public static final int DETECTION_MINUTES = 5;     // sensor or satellite sees the outburst
    public static final int ALERT_MINUTES = 3;         // alert reaches the village
    public static final int GET_READY_MINUTES = 10;    // family gets ready to leave

    private static final List<StaffAccount> staffAccounts = new ArrayList<>();
    private static final List<AccountRequest> accountRequests = new ArrayList<>();
    private static final List<Organisation> organisations = new ArrayList<>();
    private static final List<AuditEntry> auditEntries = new ArrayList<>();
    private static final List<Village> villages = new ArrayList<>();
    private static final HashMap<String, Integer> villagePopulation = new HashMap<>();
    private static final List<Lake> lakes = new ArrayList<>();
    private static final List<NetworkNode> nodes = new ArrayList<>();
    private static final List<Alert> alerts = new ArrayList<>();
    private static final List<StaffNotice> notices = new ArrayList<>();
    private static final List<Shelter> shelters = new ArrayList<>();
    private static final HashMap<String, double[]> villageLocation = new HashMap<>();
    private static final List<RegisteredPhone> registeredPhones = new ArrayList<>();
    private static int nextAlertId = 1;

    // Fill the sample data once, when the class is first used
    static {
        staffAccounts.add(new StaffAccount("Ayesha Khan", "admin@cryonix.pk", DEMO_PASSWORD,
                "+92 300 1111111", UserRole.SYSTEM_ADMIN, "Cryonix project team"));
        staffAccounts.add(new StaffAccount("Imran Ali", "officer@gbdma.gov.pk", DEMO_PASSWORD,
                "+92 300 2222222", UserRole.AUTHORITY_OFFICER, "GB-DMA"));
        staffAccounts.add(new StaffAccount("Sana Baig", "staff@akah.org", DEMO_PASSWORD,
                "+92 300 3333333", UserRole.ORGANISATION_STAFF, "AKAH"));

        accountRequests.add(new AccountRequest("Bilal Ahmed", "bilal@pdma.kp.gov.pk",
                "PDMA Khyber Pakhtunkhwa", UserRole.AUTHORITY_OFFICER, "District focal person for Chitral"));
        accountRequests.add(new AccountRequest("Nadia Shah", "nadia@undp.org",
                "UNDP GLOF-II", UserRole.ORGANISATION_STAFF, "Manages shelters in Ghizer"));

        organisations.add(new Organisation("GB-DMA", "Government",
                new ArrayList<>(Arrays.asList("Ishkoman", "Gupis", "Yasin", "Phander"))));
        organisations.add(new Organisation("PDMA Khyber Pakhtunkhwa", "Government",
                new ArrayList<>(Arrays.asList("Booni", "Reshun", "Golen"))));
        organisations.add(new Organisation("AKAH", "NGO",
                new ArrayList<>(Arrays.asList("Ishkoman", "Booni"))));

        villages.add(new Village("Badswat", "Ishkoman", "Ghizer"));
        villages.add(new Village("Imit", "Ishkoman", "Ghizer"));
        villages.add(new Village("Chatorkhand", "Ishkoman", "Ghizer"));
        villages.add(new Village("Gupis", "Gupis", "Ghizer"));
        villages.add(new Village("Khalti", "Gupis", "Ghizer"));
        villages.add(new Village("Phander", "Phander", "Ghizer"));
        villages.add(new Village("Booni", "Booni", "Chitral"));
        villages.add(new Village("Reshun", "Reshun", "Chitral"));
        villages.add(new Village("Golen", "Golen", "Chitral"));
        villages.add(new Village("Brep", "Yarkhun", "Chitral"));

        // Sample population per village (used for "people at risk")
        villagePopulation.put("Badswat", 1200);
        villagePopulation.put("Imit", 2600);
        villagePopulation.put("Chatorkhand", 5400);
        villagePopulation.put("Gupis", 4800);
        villagePopulation.put("Khalti", 1700);
        villagePopulation.put("Phander", 2100);
        villagePopulation.put("Booni", 6300);
        villagePopulation.put("Reshun", 2400);
        villagePopulation.put("Golen", 1900);
        villagePopulation.put("Brep", 2200);

        // Rough village positions (latitude, longitude), sample values for "find my village by location"
        villageLocation.put("Badswat", new double[]{36.53, 73.82});
        villageLocation.put("Imit", new double[]{36.63, 73.73});
        villageLocation.put("Chatorkhand", new double[]{36.38, 73.97});
        villageLocation.put("Gupis", new double[]{36.23, 73.45});
        villageLocation.put("Khalti", new double[]{36.22, 73.38});
        villageLocation.put("Phander", new double[]{36.17, 72.95});
        villageLocation.put("Booni", new double[]{36.27, 72.25});
        villageLocation.put("Reshun", new double[]{36.13, 72.12});
        villageLocation.put("Golen", new double[]{35.88, 71.83});
        villageLocation.put("Brep", new double[]{36.55, 72.90});

        addSampleLakes();
        addSampleNodes();
        addSampleAlerts();
        addSampleNotices();
        addSampleShelters();
        addShelterManagementDetails();

        addAuditEntry("System", "Demo data loaded");
    }

    // Sample lakes. Names and numbers are sample data, not real measurements.
    private static void addSampleLakes() {
        Lake badswat = new Lake("GL-101", "Badswat upper lake", "Ishkoman", "Ghizer", 18.4, 6.2, 4120,
                "20 Nov 2026", new ArrayList<>(Arrays.asList("Badswat", "Imit", "Chatorkhand")), AlertLevel.WATCH);
        Lake imit = new Lake("GL-102", "Imit glacier lake", "Ishkoman", "Ghizer", 9.1, 1.4, 4380,
                "20 Nov 2026", new ArrayList<>(Arrays.asList("Imit", "Chatorkhand")), AlertLevel.ADVISORY);
        Lake khalti = new Lake("GL-201", "Khalti side lake", "Gupis", "Ghizer", 5.6, 0.3, 3950,
                "19 Nov 2026", new ArrayList<>(Arrays.asList("Khalti", "Gupis")), AlertLevel.BASELINE);
        Lake phander = new Lake("GL-301", "Phander moraine lake", "Phander", "Ghizer", 7.2, -0.5, 4210,
                "19 Nov 2026", new ArrayList<>(Arrays.asList("Phander")), AlertLevel.BASELINE);
        Lake booni = new Lake("GL-401", "Booni Gol lake", "Booni", "Chitral", 4.8, 0.9, 4460,
                "21 Nov 2026", new ArrayList<>(Arrays.asList("Booni")), AlertLevel.BASELINE);
        Lake reshun = new Lake("GL-501", "Reshun glacier lake", "Reshun", "Chitral", 11.3, 4.8, 4300,
                "21 Nov 2026", new ArrayList<>(Arrays.asList("Reshun", "Booni")), AlertLevel.ADVISORY);
        Lake golen = new Lake("GL-601", "Golen Gol upper lake", "Golen", "Chitral", 14.9, 9.7, 4520,
                "22 Nov 2026", new ArrayList<>(Arrays.asList("Golen")), AlertLevel.WARNING);
        Lake brep = new Lake("GL-701", "Brep supraglacial lake", "Yarkhun", "Chitral", 6.5, 3.1, 4610,
                "22 Nov 2026", new ArrayList<>(Arrays.asList("Brep")), AlertLevel.WATCH);

        golen.setFlag("Lake area grew 9.7% in 30 days and a crack was seen in the moraine dam.", 87);
        reshun.setFlag("Lake area grew 4.8% in 30 days after warm days.", 72);

        addLakeDetails(golen, 35.97, 71.92, "Golen glacier, retreating about 20 m a year",
                "Steep ice cliff above the lake", "Moraine dam with a new crack on the east side");
        addLakeDetails(badswat, 36.60, 73.90, "Badswat glacier, surged in 2018",
                "Loose rock slope on the north side", "Moraine dam, seepage seen in summer");
        addLakeDetails(brep, 36.62, 72.84, "Chiantar glacier tongue",
                "Gentle slopes", "Ice-cored moraine, may melt");
        addLakeDetails(imit, 36.70, 73.66, "Small hanging glacier",
                "Medium slope, some rock fall", "Bedrock dam, stable");
        addLakeDetails(reshun, 36.20, 72.05, "Reshun glacier, thinning",
                "Steep moraine walls", "Moraine dam, low freeboard");
        addLakeDetails(khalti, 36.28, 73.32, "No glacier contact",
                "Gentle slopes", "Landslide dam, old and stable");
        addLakeDetails(phander, 36.12, 72.90, "Small cirque glacier",
                "Medium slope", "Moraine dam, stable");
        addLakeDetails(booni, 36.34, 72.20, "Small cirque glacier",
                "Gentle slopes", "Bedrock dam, stable");

        lakes.add(golen);
        lakes.add(badswat);
        lakes.add(brep);
        lakes.add(imit);
        lakes.add(reshun);
        lakes.add(khalti);
        lakes.add(phander);
        lakes.add(booni);
    }

    /**
     * Sample area history for 12 months, made from today's area and the 30-day change.
     * The summer months are a little bigger (melt season), so the chart looks real.
     * TODO: real values come from the lake model (M1) runs on Sentinel images.
     */
    private static void addLakeDetails(Lake lake, double latitude, double longitude,
                                       String glacierNote, String slopeNote, String damNote) {
        String[] months = {"Dec", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov"};
        double[] season = {0.90, 0.88, 0.87, 0.88, 0.91, 0.95, 1.00, 1.04, 1.03, 0.98, 0.95, 1.00};
        double now = lake.getAreaHectares();
        double lastMonth = now / (1 + lake.getAreaChangePercent() / 100.0);

        List<String> monthList = new ArrayList<>(Arrays.asList(months));
        List<Double> areaList = new ArrayList<>();
        for (int i = 0; i < months.length; i++) {
            double value;
            if (i == months.length - 1) {
                value = now;
            } else if (i == months.length - 2) {
                value = lastMonth;
            } else {
                value = lastMonth * season[i];
            }
            areaList.add(Math.round(value * 10) / 10.0);
        }
        lake.setHistory(monthList, areaList, "21 Oct 2026");
        lake.setNotes(glacierNote, slopeNote, damNote);
        lake.setPosition(latitude, longitude);
    }

    private static void addSampleNodes() {
        // id, village, valley, missed reports, last heard, battery %, signal dBm, latitude, longitude
        nodes.add(new NetworkNode("N-01", "Badswat", "Ishkoman", 0, "1 min ago", 92, -88, 36.53, 73.82));
        nodes.add(new NetworkNode("N-02", "Imit", "Ishkoman", 0, "2 min ago", 88, -97, 36.63, 73.73));
        nodes.add(new NetworkNode("N-03", "Chatorkhand", "Ishkoman", 0, "1 min ago", 95, -84, 36.38, 73.97));
        nodes.add(new NetworkNode("N-04", "Gupis", "Gupis", 0, "3 min ago", 81, -92, 36.23, 73.45));
        nodes.add(new NetworkNode("N-05", "Khalti", "Gupis", 1, "17 min ago", 77, -104, 36.22, 73.38));
        nodes.add(new NetworkNode("N-06", "Phander", "Phander", 0, "4 min ago", 69, -101, 36.17, 72.95));
        nodes.add(new NetworkNode("N-07", "Booni", "Booni", 0, "1 min ago", 90, -86, 36.27, 72.25));
        nodes.add(new NetworkNode("N-08", "Reshun", "Reshun", 12, "3 hours ago", 12, -116, 36.13, 72.12));
        nodes.add(new NetworkNode("N-09", "Golen", "Golen", 0, "2 min ago", 84, -95, 35.88, 71.83));
        nodes.add(new NetworkNode("N-10", "Brep", "Yarkhun", 3, "52 min ago", 40, -108, 36.55, 72.90));
    }

    private static void addSampleAlerts() {
        long now = System.currentTimeMillis();
        Lake badswat = findLake("GL-101");
        Alert watchAlert = new Alert(nextAlertId++, badswat.getId(), badswat.getName(), badswat.getValley(),
                AlertLevel.WATCH, new ArrayList<>(badswat.getDownstreamVillages()),
                new ArrayList<>(Arrays.asList(Alert.CHANNEL_LORA, Alert.CHANNEL_APP, Alert.CHANNEL_CELL, Alert.CHANNEL_VOICE)),
                "WATCH: Badswat upper lake is rising. Keep your bag ready and listen to the mosque speaker.",
                "Imran Ali", "22 Nov 2026, 18:40", now - 40 * 60 * 1000);
        alerts.add(watchAlert);

        Lake booni = findLake("GL-401");
        Alert oldAlert = new Alert(nextAlertId++, booni.getId(), booni.getName(), booni.getValley(),
                AlertLevel.ADVISORY, new ArrayList<>(booni.getDownstreamVillages()),
                new ArrayList<>(Arrays.asList(Alert.CHANNEL_LORA, Alert.CHANNEL_APP)),
                "ADVISORY: Booni Gol lake is being watched after heavy rain. No action needed now.",
                "Imran Ali", "18 Nov 2026, 09:15", now - 4L * 24 * 60 * 60 * 1000);
        oldAlert.close("Rain stopped and the lake went back to normal.", "Imran Ali", "18 Nov 2026, 15:40");
        alerts.add(oldAlert);
    }

    /**
     * Two shelters per village on higher ground. Badswat's upper community hall is full,
     * so the "shelter full, go to another" case can be tested.
     */
    private static void addSampleShelters() {
        int number = 1;
        for (Village village : villages) {
            String name = village.getName();
            boolean isBadswat = name.equals("Badswat");

            List<String> schoolSteps = new ArrayList<>(Arrays.asList(
                    "Leave by the side of your house away from the river.",
                    "Walk up the jeep road past the mosque.",
                    "Do not cross the bridge or walk along the river bank.",
                    "Turn left at the water tank and go up to " + name + " high school."));
            List<String> hallSteps = new ArrayList<>(Arrays.asList(
                    "Leave by the side of your house away from the river.",
                    "Take the footpath behind the shops up the hill.",
                    "Keep the fields on your right; do not go down to the stream.",
                    "The community hall is at the top of the path."));

            shelters.add(new Shelter("S-" + (number++), name + " high school", name, 650, 13, 300,
                    isBadswat ? 120 : 40,
                    new ArrayList<>(Arrays.asList("Drinking water", "Toilets", "First aid", "Blankets")),
                    schoolSteps, 0.30f, 0.18f,
                    new float[]{0.50f, 0.52f, 0.44f, 0.40f, 0.36f, 0.30f, 0.30f, 0.18f}));

            shelters.add(new Shelter("S-" + (number++), name + " upper community hall", name, 900, 18, 150,
                    isBadswat ? 150 : 25,
                    new ArrayList<>(Arrays.asList("Drinking water", "Toilets", "Women's area")),
                    hallSteps, 0.74f, 0.14f,
                    new float[]{0.50f, 0.52f, 0.58f, 0.42f, 0.66f, 0.30f, 0.74f, 0.14f}));
        }
    }

    /** Floor area, contact and first counter reading for every sample shelter. */
    private static void addShelterManagementDetails() {
        String time = "23 Nov 2026, 00:30";
        for (Shelter shelter : shelters) {
            boolean isSchool = shelter.getName().endsWith("high school");
            int floorArea = (int) Math.round(shelter.getCapacity() * Shelter.SQUARE_METRES_PER_PERSON) + 2;
            shelter.setDetails(shelter.getName(), shelter.getVillage(), isSchool ? "School" : "Community hall",
                    floorArea, isSchool ? 45 : 80, isSchool ? "Head teacher" : "Hall caretaker", "+92 300 0000000");
            int inside = shelter.getPeopleInside();
            shelter.setCount(inside + inside / 10, inside / 10, time);
        }
    }

    private static void addSampleNotices() {
        notices.add(new StaffNotice(StaffNotice.TYPE_LAKE_FLAGGED, "Golen Gol upper lake flagged",
                "Area +9.7% in 30 days. Needs review.", "22 Nov 2026, 21:05", "GL-601", "Golen"));
        notices.add(new StaffNotice(StaffNotice.TYPE_NODE_SILENT, "Node N-10 (Brep) is silent",
                "No message for 52 minutes. Battery 40%.", "22 Nov 2026, 20:30", "N-10", "Yarkhun"));
        notices.add(new StaffNotice(StaffNotice.TYPE_LAKE_FLAGGED, "Reshun glacier lake flagged",
                "Area +4.8% in 30 days. Needs review.", "22 Nov 2026, 19:10", "GL-501", "Reshun"));
        notices.add(new StaffNotice(StaffNotice.TYPE_ALERT_SENT, "Watch alert sent for Badswat",
                "Sent to 3 villages by Imran Ali.", "22 Nov 2026, 18:40", "1", "Ishkoman"));
        notices.add(new StaffNotice(StaffNotice.TYPE_NODE_SILENT, "Node N-08 (Reshun) is silent",
                "No message for 3 hours. Battery 12%.", "22 Nov 2026, 18:00", "N-08", "Reshun"));
    }

    // ---------- Staff accounts ----------

    public static List<StaffAccount> getStaffAccounts() {
        return staffAccounts;
    }

    /** Finds a staff account by email (not case sensitive). Returns null if not found. */
    public static StaffAccount findStaffByEmail(String email) {
        for (StaffAccount account : staffAccounts) {
            if (account.getEmail().equalsIgnoreCase(email.trim())) {
                return account;
            }
        }
        return null;
    }

    // ---------- Account requests ----------

    public static List<AccountRequest> getAccountRequests() {
        return accountRequests;
    }

    /** Approve: create the staff account with the chosen role and remove the request. */
    public static void approveRequest(AccountRequest request, UserRole role, String adminName) {
        staffAccounts.add(new StaffAccount(request.getFullName(), request.getEmail(), DEMO_PASSWORD,
                "", role, request.getOrganisation()));
        accountRequests.remove(request);
        addAuditEntry(adminName, "Approved account of " + request.getEmail() + " as " + role.getDisplayName());
    }

    public static void rejectRequest(AccountRequest request, String adminName) {
        accountRequests.remove(request);
        addAuditEntry(adminName, "Rejected account request of " + request.getEmail());
    }

    // ---------- Organisations ----------

    public static List<Organisation> getOrganisations() {
        return organisations;
    }

    public static List<String> getOrganisationNames() {
        List<String> names = new ArrayList<>();
        for (Organisation organisation : organisations) {
            names.add(organisation.getName());
        }
        return names;
    }

    public static void addOrganisation(Organisation organisation, String adminName) {
        organisations.add(organisation);
        addAuditEntry(adminName, "Added organisation " + organisation.getName());
    }

    // ---------- Villages ----------

    public static List<Village> getVillages() {
        return villages;
    }

    /** All valley names (without repeats), used when linking organisations to valleys. */
    public static List<String> getValleyNames() {
        List<String> valleys = new ArrayList<>();
        for (Village village : villages) {
            if (!valleys.contains(village.getValley())) {
                valleys.add(village.getValley());
            }
        }
        return valleys;
    }

    // ---------- Audit log ----------

    /** Newest entry first. */
    public static List<AuditEntry> getAuditEntries() {
        return auditEntries;
    }

    public static void addAuditEntry(String userName, String action) {
        String time = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).format(new Date());
        auditEntries.add(0, new AuditEntry(time, userName, action));
    }

    // ---------- Who sees which valleys ----------

    /**
     * Organisation staff only see the valleys of their own organisation.
     * Admins and officers see everything (returns null = no filter).
     */
    public static List<String> getVisibleValleys(UserRole role, String email) {
        if (role != UserRole.ORGANISATION_STAFF) {
            return null;
        }
        StaffAccount account = findStaffByEmail(email);
        if (account == null) {
            return new ArrayList<>();
        }
        for (Organisation organisation : organisations) {
            if (organisation.getName().equals(account.getOrganisation())) {
                return organisation.getValleys();
            }
        }
        return new ArrayList<>();
    }

    private static boolean canSee(List<String> visibleValleys, String valley) {
        return visibleValleys == null || visibleValleys.contains(valley);
    }

    public static Village findVillage(String name) {
        for (Village village : villages) {
            if (village.getName().equals(name)) {
                return village;
            }
        }
        return null;
    }

    public static int getPopulation(String villageName) {
        Integer population = villagePopulation.get(villageName);
        return population == null ? 0 : population;
    }

    // ---------- Lakes ----------

    public static List<Lake> getLakesFor(UserRole role, String email) {
        List<String> visibleValleys = getVisibleValleys(role, email);
        List<Lake> result = new ArrayList<>();
        for (Lake lake : lakes) {
            if (canSee(visibleValleys, lake.getValley())) {
                result.add(lake);
            }
        }
        return result;
    }

    public static Lake findLake(String lakeId) {
        for (Lake lake : lakes) {
            if (lake.getId().equals(lakeId)) {
                return lake;
            }
        }
        return null;
    }

    /** People living in villages below lakes on Watch or Warning (each village counted once). */
    public static int getPeopleAtRisk(List<Lake> lakeList) {
        List<String> countedVillages = new ArrayList<>();
        int total = 0;
        for (Lake lake : lakeList) {
            if (!lake.getLevel().isSerious()) {
                continue;
            }
            for (String villageName : lake.getDownstreamVillages()) {
                if (!countedVillages.contains(villageName)) {
                    countedVillages.add(villageName);
                    total += getPopulation(villageName);
                }
            }
        }
        return total;
    }

    /**
     * Officer review of a flagged lake (LM review, used from the app by DR-02).
     * newLevel is null when the officer keeps the level.
     */
    public static void reviewLake(Lake lake, String decision, AlertLevel newLevel, String note, String reviewerName) {
        AlertLevel oldLevel = lake.getLevel();
        if (newLevel != null) {
            lake.setLevel(newLevel);
        }
        lake.clearFlag();
        String time = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).format(new Date());
        lake.setLastReview(decision, note, reviewerName, time);
        String action = "Reviewed " + lake.getName() + ": " + decision;
        if (newLevel != null && newLevel != oldLevel) {
            action += " (" + oldLevel.getLabel() + " to " + newLevel.getLabel() + ")";
        }
        addAuditEntry(reviewerName, action);
    }

    // ---------- Network nodes ----------

    public static List<NetworkNode> getSilentNodesFor(UserRole role, String email) {
        List<String> visibleValleys = getVisibleValleys(role, email);
        List<NetworkNode> result = new ArrayList<>();
        for (NetworkNode node : nodes) {
            if (!node.isOnline() && canSee(visibleValleys, node.getValley())) {
                result.add(node);
            }
        }
        return result;
    }

    public static int countNodesFor(UserRole role, String email) {
        List<String> visibleValleys = getVisibleValleys(role, email);
        int count = 0;
        for (NetworkNode node : nodes) {
            if (canSee(visibleValleys, node.getValley())) {
                count++;
            }
        }
        return count;
    }

    public static NetworkNode findNode(String nodeId) {
        for (NetworkNode node : nodes) {
            if (node.getId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }

    // ---------- Alerts ----------

    /** Newest first. */
    public static List<Alert> getAlertsFor(UserRole role, String email) {
        List<String> visibleValleys = getVisibleValleys(role, email);
        List<Alert> result = new ArrayList<>();
        for (Alert alert : alerts) {
            if (canSee(visibleValleys, alert.getValley())) {
                result.add(alert);
            }
        }
        return result;
    }

    public static int countActiveAlerts(List<Alert> alertList) {
        int count = 0;
        for (Alert alert : alertList) {
            if (alert.isActive()) count++;
        }
        return count;
    }

    public static Alert findAlert(int alertId) {
        for (Alert alert : alerts) {
            if (alert.getId() == alertId) {
                return alert;
            }
        }
        return null;
    }

    public static Alert sendAlert(Lake lake, AlertLevel level, List<String> villageNames,
                                  List<String> channels, String message, String senderName) {
        String time = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).format(new Date());
        Alert alert = new Alert(nextAlertId++, lake.getId(), lake.getName(), lake.getValley(), level,
                villageNames, channels, message, senderName, time, System.currentTimeMillis());
        alerts.add(0, alert);

        addAuditEntry(senderName, "Sent " + level.getLabel() + " alert for " + lake.getName()
                + " to " + villageNames.size() + " villages");
        notices.add(0, new StaffNotice(StaffNotice.TYPE_ALERT_SENT,
                level.getLabel() + " alert sent for " + lake.getName(),
                "Sent to " + villageNames.size() + " villages by " + senderName + ".",
                time, String.valueOf(alert.getId()), lake.getValley()));
        return alert;
    }

    public static void cancelAlert(Alert alert, String reason, String userName) {
        String time = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).format(new Date());
        alert.close(reason, userName, time);
        addAuditEntry(userName, "Cancelled " + alert.getLevel().getLabel() + " alert for "
                + alert.getLakeName() + ": " + reason);
    }

    // ---------- Staff notifications (DR-04) ----------

    public static List<StaffNotice> getNoticesFor(UserRole role, String email) {
        List<String> visibleValleys = getVisibleValleys(role, email);
        List<StaffNotice> result = new ArrayList<>();
        for (StaffNotice notice : notices) {
            if (canSee(visibleValleys, notice.getValley())) {
                result.add(notice);
            }
        }
        return result;
    }

    public static int countUnread(List<StaffNotice> noticeList) {
        int count = 0;
        for (StaffNotice notice : noticeList) {
            if (!notice.isRead()) count++;
        }
        return count;
    }

    // ---------- Community app (Community Mobile App epic) ----------

    /** Shelters of one village, nearest first. */
    public static List<Shelter> getSheltersForVillage(String villageName) {
        List<Shelter> result = new ArrayList<>();
        for (Shelter shelter : shelters) {
            if (shelter.getVillage().equals(villageName)) {
                result.add(shelter);
            }
        }
        // simple sort by distance (only a few items, so a bubble sort is fine)
        for (int i = 0; i < result.size(); i++) {
            for (int j = 0; j < result.size() - 1 - i; j++) {
                if (result.get(j).getDistanceMetres() > result.get(j + 1).getDistanceMetres()) {
                    Shelter temp = result.get(j);
                    result.set(j, result.get(j + 1));
                    result.set(j + 1, temp);
                }
            }
        }
        return result;
    }

    /** Alerts sent to this village, newest first. */
    public static List<Alert> getAlertsForVillage(String villageName) {
        List<Alert> result = new ArrayList<>();
        for (Alert alert : alerts) {
            if (alert.getVillages().contains(villageName)) {
                result.add(alert);
            }
        }
        return result;
    }

    /** The lake above the village; if there are several, the most serious one. */
    public static Lake getLakeAboveVillage(String villageName) {
        Lake chosen = null;
        for (Lake lake : lakes) {
            if (!lake.getDownstreamVillages().contains(villageName)) {
                continue;
            }
            if (chosen == null || lake.getLevel().ordinal() > chosen.getLevel().ordinal()) {
                chosen = lake;
            }
        }
        return chosen;
    }

    /**
     * Nearest village to a GPS position (CA-01 "choose by location").
     * Returns null when no village is closer than maxKm.
     */
    public static String findNearestVillage(double latitude, double longitude, double maxKm) {
        String nearestName = null;
        double nearestKm = maxKm;
        for (Village village : villages) {
            double[] position = villageLocation.get(village.getName());
            if (position == null) {
                continue;
            }
            // Good enough for short distances: 1 degree of latitude is about 111 km
            double dLat = (position[0] - latitude) * 111.0;
            double dLon = (position[1] - longitude) * 111.0 * Math.cos(Math.toRadians(latitude));
            double km = Math.sqrt(dLat * dLat + dLon * dLon);
            if (km < nearestKm) {
                nearestKm = km;
                nearestName = village.getName();
            }
        }
        return nearestName;
    }

    // ---------- Flood scenarios (FS-01, FS-03) ----------

    /**
     * Small, medium and worst case for a lake.
     * DEMO numbers: lake volume = area x 15 m mean depth; small drains 20%, medium 50%, worst 100%.
     * The flood front moves about 12, 18 or 25 km per hour; the range is -20% / +20%.
     * TODO: read the real results of the flood model (M2) from the API.
     */
    public static List<FloodScenario> getFloodScenarios(Lake lake) {
        String[] types = {FloodScenario.TYPE_SMALL, FloodScenario.TYPE_MEDIUM, FloodScenario.TYPE_WORST};
        double[] drainedPart = {0.2, 0.5, 1.0};
        double[] speedKmPerHour = {12, 18, 25};

        // Lakes with steep ice or moraine above them are run as debris flows
        boolean isDebrisFlow = lake.getDamNote().toLowerCase().contains("moraine")
                || lake.getSlopeNote().toLowerCase().contains("steep");
        String modelName = isDebrisFlow ? "r.avaflow 4 (debris flow)" : "HEC-RAS 2D (water flood)";
        double fullVolumeMillion = lake.getAreaHectares() * 10000 * 15 / 1000000.0;

        List<FloodScenario> result = new ArrayList<>();
        for (int s = 0; s < types.length; s++) {
            List<SettlementArrival> arrivals = new ArrayList<>();
            List<String> villageNames = lake.getDownstreamVillages();
            for (int v = 0; v < villageNames.size(); v++) {
                double distanceKm = 6 + v * 6.5;
                double minutes = distanceKm / speedKmPerHour[s] * 60;
                arrivals.add(new SettlementArrival(villageNames.get(v), distanceKm,
                        (int) Math.round(minutes * 0.8), (int) Math.round(minutes * 1.2),
                        getPopulation(villageNames.get(v))));
            }
            double release = Math.round(fullVolumeMillion * drainedPart[s] * 100) / 100.0;
            int peakFlow = (int) Math.round(release * 1000000 / 1800.0);   // most water leaves in about 30 minutes
            result.add(new FloodScenario(lake.getId(), types[s], release, peakFlow, modelName, "15 Nov 2026", arrivals));
        }
        return result;
    }

    // ---------- Evacuation time margin (EV-02) ----------

    /**
     * For the worst-case scenario: margin of each house =
     * earliest flood arrival - (detection + alert + get ready + walking time of that house).
     * DEMO: one house per 7 people; walking times are spread between 4 and 30 minutes.
     * TODO: real walking times come from the routing model (M4) for every building.
     */
    public static List<VillageTimeMargin> getTimeMargins(Lake lake) {
        List<FloodScenario> scenarios = getFloodScenarios(lake);
        FloodScenario worstCase = scenarios.get(scenarios.size() - 1);

        List<VillageTimeMargin> result = new ArrayList<>();
        for (SettlementArrival arrival : worstCase.getArrivals()) {
            int houseCount = Math.max(10, arrival.getPopulation() / 7);
            houseCount = Math.min(houseCount, 400);   // keep the drawing readable
            List<Integer> margins = new ArrayList<>();
            for (int h = 0; h < houseCount; h++) {
                // spread of walking times that repeats the same way every time (no random)
                int walkMinutes = 4 + (h * 37 + arrival.getVillage().length() * 11) % 27;
                int fixedMinutes = DETECTION_MINUTES + ALERT_MINUTES + GET_READY_MINUTES;
                margins.add(arrival.getEarliestMinutes() - fixedMinutes - walkMinutes);
            }
            result.add(new VillageTimeMargin(arrival.getVillage(), arrival.getEarliestMinutes(), margins));
        }
        return result;
    }

    // ---------- Shelter management (SH-01, SH-02) ----------

    /** Shelters a staff user may manage: all for admins, own valleys for organisation staff. */
    public static List<Shelter> getSheltersForStaff(UserRole role, String email) {
        List<String> visibleValleys = getVisibleValleys(role, email);
        List<Shelter> result = new ArrayList<>();
        for (Shelter shelter : shelters) {
            Village village = findVillage(shelter.getVillage());
            String valley = village == null ? "" : village.getValley();
            if (canSee(visibleValleys, valley)) {
                result.add(shelter);
            }
        }
        return result;
    }

    /** Villages a staff user may add shelters to. */
    public static List<String> getVillageNamesForStaff(UserRole role, String email) {
        List<String> visibleValleys = getVisibleValleys(role, email);
        List<String> result = new ArrayList<>();
        for (Village village : villages) {
            if (canSee(visibleValleys, village.getValley())) {
                result.add(village.getName());
            }
        }
        return result;
    }

    public static Shelter findShelter(String shelterId) {
        for (Shelter shelter : shelters) {
            if (shelter.getId().equals(shelterId)) {
                return shelter;
            }
        }
        return null;
    }

    /** New shelter with a default route on the village map (the plan adds the real route later). */
    public static Shelter addShelter(String userName) {
        String id = "S-" + (shelters.size() + 1);
        List<String> steps = new ArrayList<>(Arrays.asList(
                "Leave by the side of your house away from the river.",
                "Follow the volunteers up the hill to the shelter."));
        Shelter shelter = new Shelter(id, "", "", 500, 10, 0, 0,
                new ArrayList<>(Arrays.asList("Drinking water")), steps, 0.52f, 0.22f,
                new float[]{0.50f, 0.52f, 0.52f, 0.36f, 0.52f, 0.22f});
        shelters.add(shelter);
        return shelter;
    }

    public static void removeShelterIfEmpty(Shelter shelter) {
        if (shelter.getName().isEmpty()) {
            shelters.remove(shelter);
        }
    }

    /**
     * DEMO of the camera people counter: a few people come in and a few leave at each reading.
     * TODO: replace with GET /api/shelters/{id}/count fed by the counter at the door.
     */
    public static void readNewCounts(List<Shelter> shelterList) {
        String time = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.ENGLISH).format(new Date());
        long tick = System.currentTimeMillis() / 5000;
        for (Shelter shelter : shelterList) {
            int seed = (int) ((tick + shelter.getId().hashCode()) % 7);
            int cameIn = shelter.getPeopleCameIn() + seed;
            int wentOut = shelter.getPeopleWentOut() + (seed == 6 ? 2 : 0);
            shelter.setCount(cameIn, wentOut, time);
        }
    }

    // ---------- All clear (AL-05) ----------

    /** People living in these villages (AL-01 preview). */
    public static int countPeople(List<String> villageNames) {
        return Alert.countPeople(villageNames, villagePopulation);
    }

    /**
     * Sends an All clear to the SAME villages on the SAME channels as the first alert,
     * and closes the first alert with time and user.
     */
    public static Alert sendAllClear(Alert firstAlert, String message, String userName) {
        Lake lake = findLake(firstAlert.getLakeId());
        Alert allClear = sendAlert(lake, AlertLevel.ALL_CLEAR, new ArrayList<>(firstAlert.getVillages()),
                new ArrayList<>(firstAlert.getChannels()), message, userName);
        String time = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).format(new Date());
        firstAlert.close("All clear sent (alert " + allClear.getId() + ")", userName, time);
        addAuditEntry(userName, "Closed " + firstAlert.getLevel().getLabel() + " alert for "
                + firstAlert.getLakeName() + " with an All clear");
        return allClear;
    }

    // ---------- Network health (LR-05) ----------

    public static List<NetworkNode> getNodesFor(UserRole role, String email) {
        List<String> visibleValleys = getVisibleValleys(role, email);
        List<NetworkNode> result = new ArrayList<>();
        for (NetworkNode node : nodes) {
            if (canSee(visibleValleys, node.getValley())) {
                result.add(node);
            }
        }
        return result;
    }

    /** Text a village node sends to phones near it (LR-06), e.g. "WATCH|Keep your bag ready". */
    public static String getSampleNodeBroadcast(String villageName) {
        for (Alert alert : alerts) {
            if (alert.isActive() && alert.getVillages().contains(villageName)) {
                return alert.getLevel().name() + "|" + alert.getMessage();
            }
        }
        return "BASELINE|No alert for " + villageName + ". This is a test message from the village node.";
    }

    // ---------- Alert call numbers (MN-01) ----------

    /** Pakistani mobile number: 03XXXXXXXXX or +923XXXXXXXXX (spaces and dashes removed first). */
    public static String cleanPhoneNumber(String typedNumber) {
        String clean = typedNumber.replace(" ", "").replace("-", "");
        if (clean.matches("03[0-9]{9}") || clean.matches("\\+923[0-9]{9}")) {
            return clean;
        }
        return null;
    }

    /**
     * Links a confirmed number to one village. A number can be linked to only one village,
     * so an old link is replaced.
     */
    public static void registerPhone(String number, String village, String ownerName, String addedByPhone) {
        removePhoneQuietly(number);
        String time = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).format(new Date());
        registeredPhones.add(new RegisteredPhone(number, village, ownerName, addedByPhone, time));
        addAuditEntry(addedByPhone, "Registered " + number + " for alert calls in " + village);
    }

    /** Numbers this app user manages: their own and the ones they added for family. */
    public static List<RegisteredPhone> getPhonesManagedBy(String userPhone) {
        List<RegisteredPhone> result = new ArrayList<>();
        for (RegisteredPhone phone : registeredPhones) {
            if (phone.getAddedByPhone().equals(userPhone) || phone.getNumber().equals(userPhone)) {
                result.add(phone);
            }
        }
        return result;
    }

    public static boolean isPhoneRegistered(String number) {
        for (RegisteredPhone phone : registeredPhones) {
            if (phone.getNumber().equals(number)) {
                return true;
            }
        }
        return false;
    }

    /** The person can remove a number at any time; no more calls or SMS go to it. */
    public static void removePhone(String number, String byPhone) {
        removePhoneQuietly(number);
        addAuditEntry(byPhone, "Removed " + number + " from alert calls");
    }

    private static void removePhoneQuietly(String number) {
        for (int i = registeredPhones.size() - 1; i >= 0; i--) {
            if (registeredPhones.get(i).getNumber().equals(number)) {
                registeredPhones.remove(i);
            }
        }
    }

    /** Count of numbers per village (used for the alert preview later). */
    public static int countPhonesInVillage(String village) {
        int count = 0;
        for (RegisteredPhone phone : registeredPhones) {
            if (phone.getVillage().equals(village)) count++;
        }
        return count;
    }
}
