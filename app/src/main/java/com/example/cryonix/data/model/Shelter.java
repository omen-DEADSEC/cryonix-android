package com.example.cryonix.data.model;

import java.util.List;

/**
 * A shelter (safe point) for one village, with the approved walking route to it.
 * DEMO: the route will come from the evacuation plan (Evacuation Planning epic, M4 model).
 * mapX / mapY and routePoints are positions on the simple village map (0.0 to 1.0).
 */
public class Shelter {

    private final String id;
    private String name;
    private String village;
    private final int distanceMetres;
    private final int walkingMinutes;
    private int capacity;
    private int peopleInside;
    private final List<String> facilities;
    private final List<String> routeSteps;
    private final float mapX;
    private final float mapY;
    private final float[] routePoints;   // x1, y1, x2, y2, ... from the village to the shelter

    // ----- Shelter management (SH-01, SH-02); not needed in the community app -----
    /** Sphere standard for cold places: 4.5 square metres of covered floor per person. */
    public static final double SQUARE_METRES_PER_PERSON = 4.5;
    /** A shelter less than this height above the river is treated as inside the flood zone (demo rule). */
    public static final int SAFE_HEIGHT_METRES = 20;

    private String type = "School";
    private int floorAreaSquareMetres;
    private int heightAboveRiverMetres = 60;
    private String contactName = "";
    private String contactPhone = "";
    private int peopleCameIn;
    private int peopleWentOut;
    private String lastCountTime = "";

    public Shelter(String id, String name, String village, int distanceMetres, int walkingMinutes,
                   int capacity, int peopleInside, List<String> facilities, List<String> routeSteps,
                   float mapX, float mapY, float[] routePoints) {
        this.id = id;
        this.name = name;
        this.village = village;
        this.distanceMetres = distanceMetres;
        this.walkingMinutes = walkingMinutes;
        this.capacity = capacity;
        this.peopleInside = peopleInside;
        this.facilities = facilities;
        this.routeSteps = routeSteps;
        this.mapX = mapX;
        this.mapY = mapY;
        this.routePoints = routePoints;
    }

    public int getFreePlaces() {
        return Math.max(0, capacity - peopleInside);
    }

    public boolean isFull() {
        return getFreePlaces() == 0;
    }

    /** Capacity is worked out from the floor area (SH-01). */
    public static int capacityFromFloorArea(int floorAreaSquareMetres) {
        return (int) Math.floor(floorAreaSquareMetres / SQUARE_METRES_PER_PERSON);
    }

    public void setDetails(String name, String village, String type, int floorAreaSquareMetres,
                           int heightAboveRiverMetres, String contactName, String contactPhone) {
        this.name = name;
        this.village = village;
        this.type = type;
        this.floorAreaSquareMetres = floorAreaSquareMetres;
        this.heightAboveRiverMetres = heightAboveRiverMetres;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.capacity = capacityFromFloorArea(floorAreaSquareMetres);
    }

    public boolean isInFloodZone() {
        return heightAboveRiverMetres < SAFE_HEIGHT_METRES;
    }

    /** New reading from the camera people counter at the door (SH-02). */
    public void setCount(int cameIn, int wentOut, String time) {
        this.peopleCameIn = cameIn;
        this.peopleWentOut = wentOut;
        this.peopleInside = Math.max(0, cameIn - wentOut);
        this.lastCountTime = time;
    }

    /** How full, in percent (can go over 100). */
    public int getPercentFull() {
        return capacity == 0 ? 0 : peopleInside * 100 / capacity;
    }

    public String getType() { return type; }
    public int getFloorAreaSquareMetres() { return floorAreaSquareMetres; }
    public int getHeightAboveRiverMetres() { return heightAboveRiverMetres; }
    public String getContactName() { return contactName; }
    public String getContactPhone() { return contactPhone; }
    public int getPeopleCameIn() { return peopleCameIn; }
    public int getPeopleWentOut() { return peopleWentOut; }
    public String getLastCountTime() { return lastCountTime; }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getVillage() { return village; }
    public int getDistanceMetres() { return distanceMetres; }
    public int getWalkingMinutes() { return walkingMinutes; }
    public int getCapacity() { return capacity; }
    public int getPeopleInside() { return peopleInside; }
    public List<String> getFacilities() { return facilities; }
    public List<String> getRouteSteps() { return routeSteps; }
    public float getMapX() { return mapX; }
    public float getMapY() { return mapY; }
    public float[] getRoutePoints() { return routePoints; }
}
