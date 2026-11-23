package com.example.cryonix.data.model;

/** A village in one of the pilot valleys. */
public class Village {

    private final String name;
    private final String valley;
    private final String district;

    public Village(String name, String valley, String district) {
        this.name = name;
        this.valley = valley;
        this.district = district;
    }

    public String getName() { return name; }
    public String getValley() { return valley; }
    public String getDistrict() { return district; }

    /** Text shown under the village name, for example "Ishkoman valley, Ghizer". */
    public String getAreaText() {
        return valley + " valley, " + district;
    }
}
