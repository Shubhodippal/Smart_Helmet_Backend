package com.example.helmet.em_contact;

import java.util.List;

import com.example.helmet.em_contact.Model.EmergencyContact;

public interface EmergencyContactDAO {
    EmergencyContact save(EmergencyContact contact);
    List<EmergencyContact> findByUid(String uid);
    EmergencyContact findById(Long id);
    EmergencyContact update(EmergencyContact contact);
    void delete(Long id);
    void deleteByUidAndEmailAndRelation(String uid, String email, String relation);
}
