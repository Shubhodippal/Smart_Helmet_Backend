package com.example.helmet.em_contact.Model;

public class EmergencyContactRequest {
    private String emergencyContactEmail;
    private String emergencyContactRelation;

    public EmergencyContactRequest() {}

    public EmergencyContactRequest(String emergencyContactEmail, String emergencyContactRelation) {
        this.emergencyContactEmail = emergencyContactEmail;
        this.emergencyContactRelation = emergencyContactRelation;
    }

    public String getEmergencyContactEmail() {
        return emergencyContactEmail;
    }

    public void setEmergencyContactEmail(String emergencyContactEmail) {
        this.emergencyContactEmail = emergencyContactEmail;
    }

    public String getEmergencyContactRelation() {
        return emergencyContactRelation;
    }

    public void setEmergencyContactRelation(String emergencyContactRelation) {
        this.emergencyContactRelation = emergencyContactRelation;
    }
}