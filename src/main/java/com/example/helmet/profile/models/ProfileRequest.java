package com.example.helmet.profile.models;

public class ProfileRequest {

    private String address;
    private String gender;
    private String bikeRegistration;
    private String insurance;
    private String bloodGroup;
    private String medCondition;

    // Default constructor
    public ProfileRequest() {}

    // Constructor with parameters
    public ProfileRequest(String address, String gender, String bikeRegistration,
                         String insurance, String bloodGroup, String medCondition) {
        this.address = address;
        this.gender = gender;
        this.bikeRegistration = bikeRegistration;
        this.insurance = insurance;
        this.bloodGroup = bloodGroup;
        this.medCondition = medCondition;
    }

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBikeRegistration() {
        return bikeRegistration;
    }

    public void setBikeRegistration(String bikeRegistration) {
        this.bikeRegistration = bikeRegistration;
    }

    public String getInsurance() {
        return insurance;
    }

    public void setInsurance(String insurance) {
        this.insurance = insurance;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getMedCondition() {
        return medCondition;
    }

    public void setMedCondition(String medCondition) {
        this.medCondition = medCondition;
    }
}