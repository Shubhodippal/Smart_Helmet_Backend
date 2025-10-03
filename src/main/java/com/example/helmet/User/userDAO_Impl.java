package com.example.helmet.User;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.helmet.User.Model.User;

import jakarta.persistence.EntityManager;

@Service
@Transactional
public class userDAO_Impl implements CommandLineRunner {
    
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private EntityManager entityManager;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            entityManager.createNativeQuery("SET @@auto_increment_increment=1").executeUpdate();
        } catch (Exception e) {
            // Ignore if fails
        }
    }
    
    /**
     * Save a new user to the database
     * @param user User object to save
     * @return saved User object
     */
    public User saveUser(User user) {
        return userDAO.save(user);
    }
    
    /**
     * Get user by email
     * @param email user email
     * @return User object or null if not found
     */
    public User getUserByEmail(String email) {
        return userDAO.getUserByEmail(email);
    }
    
    /**
     * Get user by ID
     * @param id user ID
     * @return User object or null if not found
     */
    public User getUserById(Long id) {
        Optional<User> user = userDAO.findById(id);
        return user.orElse(null);
    }
    
    /**
     * Check if email exists in database
     * @param email email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        return userDAO.emailExists(email);
    }
    
    /**
     * Check if user ID exists in database
     * @param userid user ID to check
     * @return true if user ID exists, false otherwise
     */
    public boolean isUserIdExists(String userid) {
        return userDAO.isUserIdExists(userid);
    }
    
    /**
     * Update user password
     * @param email user email
     * @param newPassword new password
     * @return true if password updated successfully, false otherwise
     */
    public boolean updatePassword(String email, String newPassword) {
        int updatedRows = userDAO.updatePassword(email, newPassword);
        return updatedRows > 0;
    }
    
    /**
     * Update user's last login timestamp
     * @param email user email
     * @return true if last login updated successfully, false otherwise
     */
    public boolean updateLastLogin(String email) {
        int updatedRows = userDAO.updateLastLogin(email);
        return updatedRows > 0;
    }
    
    /**
     * Update user information
     * @param user User object with updated information
     * @return updated User object
     */
    public User updateUser(User user) {
        return userDAO.save(user);
    }
    
    /**
     * Delete user by ID
     * @param id user ID to delete
     * @return true if user deleted successfully, false otherwise
     */
    public boolean deleteUser(Long id) {
        try {
            if (userDAO.existsById(id)) {
                userDAO.deleteById(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get all users (with pagination support)
     * @return list of all users
     */
    public java.util.List<User> getAllUsers() {
        return userDAO.findAll();
    }
}
