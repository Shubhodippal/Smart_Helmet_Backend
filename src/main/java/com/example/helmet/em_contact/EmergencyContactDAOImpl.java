package com.example.helmet.em_contact;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.helmet.em_contact.Model.EmergencyContact;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Repository
public class EmergencyContactDAOImpl implements EmergencyContactDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public EmergencyContact save(EmergencyContact contact) {
        entityManager.persist(contact);
        return contact;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<EmergencyContact> findByUid(String uid) {
        Query query = entityManager.createQuery("SELECT e FROM EmergencyContact e WHERE e.uid = :uid");
        query.setParameter("uid", uid);
        return (List<EmergencyContact>) query.getResultList();
    }

    @Override
    public EmergencyContact findById(Long id) {
        return entityManager.find(EmergencyContact.class, id);
    }

    @Override
    @Transactional
    public EmergencyContact update(EmergencyContact contact) {
        return entityManager.merge(contact);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EmergencyContact contact = findById(id);
        if (contact != null) {
            entityManager.remove(contact);
        }
    }
}
