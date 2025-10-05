package com.example.helmet.profile.dao;

import com.example.helmet.profile.models.Profile;

public interface ProfileDAO {
    Profile save(Profile profile);
    Profile findByUid(String uid);
    Profile findById(Long id);
    Profile update(Profile profile);
    void delete(Long id);
    boolean existsByUid(String uid);
}