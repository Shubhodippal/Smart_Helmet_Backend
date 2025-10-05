package com.example.helmet.profile.dao;

import org.springframework.stereotype.Repository;

import com.example.helmet.profile.models.Profile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Repository
public class ProfileDAOImpl implements ProfileDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Profile save(Profile profile) {
        entityManager.persist(profile);
        return profile;
    }

    @Override
    public Profile findByUid(String uid) {
        try {
            Query query = entityManager.createQuery("SELECT p FROM Profile p WHERE p.uid = :uid");
            query.setParameter("uid", uid);
            return (Profile) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Profile findById(Long id) {
        return entityManager.find(Profile.class, id);
    }

    @Override
    @Transactional
    public Profile update(Profile profile) {
        return entityManager.merge(profile);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Profile profile = findById(id);
        if (profile != null) {
            entityManager.remove(profile);
        }
    }

    @Override
    public boolean existsByUid(String uid) {
        try {
            Query query = entityManager.createQuery("SELECT COUNT(p) FROM Profile p WHERE p.uid = :uid");
            query.setParameter("uid", uid);
            Long count = (Long) query.getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }
}