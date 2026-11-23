package com.example.cryonix.data.model;

/**
 * A phone number that gets alert calls and SMS for ONE village (MN-01).
 * Works for any phone, also a simple phone without internet.
 */
public class RegisteredPhone {

    private final String number;
    private final String village;
    private final String ownerName;     // "Me" or the name of the family member
    private final String addedByPhone;  // the app user who added it
    private final String addedTime;

    public RegisteredPhone(String number, String village, String ownerName, String addedByPhone, String addedTime) {
        this.number = number;
        this.village = village;
        this.ownerName = ownerName;
        this.addedByPhone = addedByPhone;
        this.addedTime = addedTime;
    }

    public String getNumber() { return number; }
    public String getVillage() { return village; }
    public String getOwnerName() { return ownerName; }
    public String getAddedByPhone() { return addedByPhone; }
    public String getAddedTime() { return addedTime; }
}
