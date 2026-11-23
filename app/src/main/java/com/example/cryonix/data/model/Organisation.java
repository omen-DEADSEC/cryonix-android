package com.example.cryonix.data.model;

import java.util.List;

/** An organisation (government body, NGO or UN project) and the valleys it works in (AC-08). */
public class Organisation {

    private final String name;
    private final String type;
    private final List<String> valleys;

    public Organisation(String name, String type, List<String> valleys) {
        this.name = name;
        this.type = type;
        this.valleys = valleys;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public List<String> getValleys() { return valleys; }
}
