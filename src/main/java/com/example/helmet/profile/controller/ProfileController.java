package com.example.helmet.profile.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.helmet.profile.dao.ProfileDAO;
import com.example.helmet.profile.models.Profile;
import com.example.helmet.profile.models.ProfileRequest;
import com.example.helmet.util.JwtUtil;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileDAO profileDAO;

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

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@RequestHeader("Authorization") String token) {
        try {
            String uid = validateTokenAndGetUid(token);
            Profile profile = profileDAO.findByUid(uid);

            if (profile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profile not found");
            }

            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNewProfile(@RequestHeader("Authorization") String token, @RequestBody ProfileRequest request) {
        try {
            String uid = validateTokenAndGetUid(token);

            // Check if profile already exists
            if (profileDAO.existsByUid(uid)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Profile already exists for this user");
            }

            Profile profile = new Profile(uid, request.getAddress(), request.getGender(),
                                        request.getBikeRegistration(), request.getInsurance(),
                                        request.getBloodGroup(), request.getMedCondition());

            Profile savedProfile = profileDAO.save(profile);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateMyProfile(@RequestHeader("Authorization") String token, @RequestBody ProfileRequest request) {
        try {
            String uid = validateTokenAndGetUid(token);
            Profile existingProfile = profileDAO.findByUid(uid);

            if (existingProfile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profile not found");
            }

            existingProfile.setAddress(request.getAddress());
            existingProfile.setGender(request.getGender());
            existingProfile.setBikeRegistration(request.getBikeRegistration());
            existingProfile.setInsurance(request.getInsurance());
            existingProfile.setBloodGroup(request.getBloodGroup());
            existingProfile.setMedCondition(request.getMedCondition());

            Profile updatedProfile = profileDAO.update(existingProfile);
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMyProfile(@RequestHeader("Authorization") String token) {
        try {
            String uid = validateTokenAndGetUid(token);
            Profile profile = profileDAO.findByUid(uid);

            if (profile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profile not found");
            }

            profileDAO.delete(profile.getId());
            return ResponseEntity.ok("Profile deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        }
    }
}
