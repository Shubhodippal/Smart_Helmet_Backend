package com.example.helmet.em_contact;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.helmet.User.Model.User;
import com.example.helmet.User.userDAO_Impl;
import com.example.helmet.em_contact.Model.EmergencyContact;
import com.example.helmet.em_contact.Model.EmergencyContactRequest;
import com.example.helmet.util.JwtUtil;

@RestController
@RequestMapping("/api/emergency-contacts")
public class EmergencyContactController {

    @Autowired
    private EmergencyContactDAO emergencyContactDAO;

    @Autowired
    private userDAO_Impl userService;

    @Autowired
    private JwtUtil jwtUtil;

    private String validateTokenAndGetUid(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token");
        }
        String jwtToken = token.substring(7);
        if (!jwtUtil.validateToken(jwtToken)) {
            throw new RuntimeException("Invalid or expired token");
        }
        return jwtUtil.extractUserId(jwtToken);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getEmergencyContacts(@RequestHeader("Authorization") String token) {
        try {
            String uid = validateTokenAndGetUid(token);
            List<EmergencyContact> contacts = emergencyContactDAO.findByUid(uid);
            
            Map<String, Object> response = new LinkedHashMap<>();
            int count = 1;
            for (EmergencyContact contact : contacts) {
                User user = userService.getUserByEmail(contact.getEmergencyContactEmail());
                Map<String, Object> contactData = new LinkedHashMap<>();
                contactData.put("id", contact.getId());
                contactData.put("uid", user != null ? user.getUid() : null);
                contactData.put("name", user != null ? user.getName() : null);
                contactData.put("email", contact.getEmergencyContactEmail());
                contactData.put("relation", contact.getEmergencyContactRelation());
                contactData.put("phone", user != null ? user.getPhone() : null);
                
                response.put("contact" + count, contactData);
                count++;
            }
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addEmergencyContact(@RequestHeader("Authorization") String token, @RequestBody EmergencyContactRequest request) {
        try {
            String uid = validateTokenAndGetUid(token);
            
            // Check if user already has 3 emergency contacts
            List<EmergencyContact> existingContacts = emergencyContactDAO.findByUid(uid);
            if (existingContacts.size() >= 3) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Maximum 3 emergency contacts allowed per user");
            }
            
            // Check for duplicate emergency contact email
            boolean isDuplicate = existingContacts.stream()
                .anyMatch(contact -> contact.getEmergencyContactEmail().equals(request.getEmergencyContactEmail()));
            if (isDuplicate) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Emergency contact with this email already exists");
            }
            
            EmergencyContact contact = new EmergencyContact(uid, request.getEmergencyContactEmail(), request.getEmergencyContactRelation());
            emergencyContactDAO.save(contact);
            return ResponseEntity.status(HttpStatus.CREATED).body("Emergency contact added successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEmergencyContact(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody EmergencyContactRequest request) {
        try {
            String uid = validateTokenAndGetUid(token);
            EmergencyContact contact = emergencyContactDAO.findById(id);
            if (contact == null || !contact.getUid().equals(uid)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Emergency contact not found or not owned by user");
            }
            contact.setEmergencyContactEmail(request.getEmergencyContactEmail());
            contact.setEmergencyContactRelation(request.getEmergencyContactRelation());
            EmergencyContact updated = emergencyContactDAO.update(contact);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteEmergencyContact(@RequestHeader("Authorization") String token, @RequestBody EmergencyContactRequest request) {
        try {
            String uid = validateTokenAndGetUid(token);
            
            // Check if user has more than one emergency contact
            List<EmergencyContact> existingContacts = emergencyContactDAO.findByUid(uid);
            if (existingContacts.size() <= 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot delete the last emergency contact. At least one emergency contact must be maintained.");
            }
            
            emergencyContactDAO.deleteByUidAndEmailAndRelation(uid, request.getEmergencyContactEmail(), request.getEmergencyContactRelation());
            return ResponseEntity.ok("Emergency contact deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        }
    }
}