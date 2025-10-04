package com.example.helmet.User.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.helmet.User.Model.LoginRequest;
import com.example.helmet.User.Model.RefreshTokenRequest;
import com.example.helmet.User.Model.ResetRequest;
import com.example.helmet.User.Model.User;
import com.example.helmet.User.userDAO_Impl;
import com.example.helmet.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private userDAO_Impl userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    // Email configuration fields
    @Value("${spring.mail.username}")
    private String emailUsername;
    
    @Value("${spring.mail.password}")
    private String emailPassword;
    
    @Value("${spring.mail.host}")
    private String emailHost;
    
    @Value("${spring.mail.port}")
    private int emailPort;
    
    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean enableStartTls;
    
    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean enableAuth;
    
    // Email pattern for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );
    
    // Email session and utilities
    private Session emailSession;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4); // Reduced rounds for faster encoding
    private final Random random = new Random();
    private final Executor emailExecutor = Executors.newFixedThreadPool(3);
    
    @PostConstruct
    public void initializeEmailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", emailHost);
        props.put("mail.smtp.port", emailPort);
        props.put("mail.smtp.auth", enableAuth);
        props.put("mail.smtp.starttls.enable", enableStartTls);
        props.put("mail.smtp.starttls.required", true);
        props.put("mail.smtp.ssl.enable", false);
        props.put("mail.smtp.connectiontimeout", 60000);
        props.put("mail.smtp.timeout", 90000);
        props.put("mail.smtp.writetimeout", 30000);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.quitwait", true);
        props.put("mail.smtp.connectionpoolsize", 2);
        
        this.emailSession = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(emailUsername, emailPassword);
            }
        });
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userService.emailExists(user.getEmail())) {
            return ResponseEntity.status(409).body("Email already exists.");
        }

        String uid;
        do {
            uid = UUID.randomUUID().toString();
        } while (userService.isUserIdExists(uid));

        user.setUid(uid);
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);

        User savedUser = userService.saveUser(user);
        if (savedUser != null) {
            return ResponseEntity.ok("User saved successfully with uid: " + uid);
        } else {
            return ResponseEntity.status(500).body("Failed to save user");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.getUserByEmail(loginRequest.getEmail());

        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        
        userService.updateLastLogin(user.getEmail());

        String accessToken = jwtUtil.generateToken(user.getUid(), user.getEmail(), user.getName(), user.getPhone());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUid(), user.getEmail(), user.getName(), user.getPhone());

        Map<String, String> response = new HashMap<>();
        response.put("token", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("message", "Login successful");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetRequest request) {
        User user = userService.getUserByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            
            String encryptedPassword = passwordEncoder.encode(request.getNewPassword());
            
            boolean updated = userService.updatePassword(request.getEmail(), encryptedPassword);
            if (updated) {
                return ResponseEntity.ok("Password reset successful");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reset password");
            }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }
        
        String userId = jwtUtil.extractUserIdFromRefreshToken(refreshToken);
        String email = jwtUtil.extractEmailFromRefreshToken(refreshToken);
        String name = jwtUtil.extractNameFromRefreshToken(refreshToken);
        
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        String phone = user.getPhone();
        
        String newAccessToken = jwtUtil.generateToken(userId, email, name, phone);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, email, name, phone);
        
        Map<String, String> response = new HashMap<>();
        response.put("token", newAccessToken);
        response.put("refreshToken", newRefreshToken);
        response.put("message", "Token refreshed successfully");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String otp = request.get("otp");
            String otpToken = request.get("otpToken");
            
            if (otp == null || otp.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "OTP is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (otpToken == null || otpToken.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "OTP token is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Decode JWT token to get encoded OTP
            Claims claims = jwtUtil.extractAllClaims(otpToken);
            if (claims == null) {
                response.put("status", "error");
                response.put("message", "Invalid or expired OTP token");
                return ResponseEntity.badRequest().body(response);
            }
            
            String encodedOtp = claims.get("encodedOtp", String.class);
            if (encodedOtp == null) {
                response.put("status", "error");
                response.put("message", "Invalid token data");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verify OTP using cached password encoder
            boolean isOtpValid = this.passwordEncoder.matches(otp.trim(), encodedOtp);
            
            if (isOtpValid) {
                response.put("status", "success");
                response.put("message", "OTP verified successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Invalid OTP");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "OTP verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/otp")
    public ResponseEntity<Map<String, Object>> generateOtp(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (request == null) {
                response.put("status", "error");
                response.put("message", "Request cannot be null");
                return ResponseEntity.badRequest().body(response);
            }

            String email = request.get("email");
            String type = request.get("type");
            
            if (email == null || email.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                response.put("status", "error");
                response.put("message", "Invalid email format");
                return ResponseEntity.badRequest().body(response);
            }

            if (type == null || type.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Type is required");
                return ResponseEntity.badRequest().body(response);
            }

            email = email.trim().toLowerCase();
            type = type.trim().toLowerCase();
            
            // Initialize email session if not already done
            if (emailSession == null) {
                initializeEmailSession();
            }
            
            if ("signup".equals(type)) {
                return handleSignupOtp(email, type, response);
            } else if ("reset".equals(type)) {
                return handleResetOtp(email, type, response);
            } else {
                response.put("status", "error");
                response.put("message", "Invalid request type. Must be 'signup' or 'reset'");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "OTP generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> handleSignupOtp(String email, String type, Map<String, Object> response) {
        try {
            if (userService.emailExists(email)) {
                response.put("status", "error");
                response.put("message", "Email already registered");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            return generateAndSendOtp(email, type, response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Signup OTP generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> handleResetOtp(String email, String type, Map<String, Object> response) {
        try {
            if (!userService.emailExists(email)) {
                response.put("status", "error");
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            return generateAndSendOtp(email, type, response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Reset OTP generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> generateAndSendOtp(String email, String type, Map<String, Object> response) {
        try {
            if (emailUsername == null || emailUsername.trim().isEmpty() || 
                emailPassword == null || emailPassword.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Email service not configured");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }

            // Fast OTP generation
            String otp = generateOtpFast();
            
            // Fast OTP encoding with reduced BCrypt rounds
            String encodedOtp = passwordEncoder.encode(otp);
            
            // Create JWT token with claims
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", email);
            claims.put("encodedOtp", encodedOtp);
            claims.put("type", type);
            
            String otpToken = jwtUtil.generateTokenWithExpiration(claims, 10 * 60 * 1000); // 10 minutes
            
            // Send email asynchronously for faster response
            sendOtpEmailAsync(email, otp);
            
            response.put("status", "success");
            response.put("otpToken", otpToken);
            response.put("message", "OTP sent successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "OTP generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Optimized OTP generation
    private String generateOtpFast() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // IMPROVED: Asynchronous email sending with better error handling and retry logic
    private void sendOtpEmailAsync(String to, String otp) {
        CompletableFuture.runAsync(() -> {
            int maxRetries = 3; // Allow up to 3 attempts for reliability
            
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    sendOtpEmailFast(to, otp);
                    return; // Success - exit the retry loop
                } catch (Exception e) {
                    
                    // Determine if we should retry based on the exception type
                    boolean shouldRetry = attempt < maxRetries && isRetryableException(e);
                    
                    if (shouldRetry) {
                        try {
                            Thread.sleep(attempt * 2000L); // Progressive backoff: 2s, 4s, 6s
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }, emailExecutor);
    }
    
    // Helper method to determine if an exception is worth retrying
    private boolean isRetryableException(Exception e) {
        String message = e.getMessage().toLowerCase();
        // Retry on connection issues, timeouts, but not on authentication failures
        return message.contains("connection") || 
               message.contains("timeout") || 
               message.contains("network") ||
               message.contains("temporary") ||
               (message.contains("authentication") && message.contains("temporary"));
    }

    // IMPROVED: Reliable email sending with fresh connections and retry logic
    private void sendOtpEmailFast(String to, String otp) throws MessagingException {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email cannot be null or empty");
        }

        if (otp == null || otp.trim().isEmpty()) {
            throw new IllegalArgumentException("OTP cannot be null or empty");
        }

        if (emailSession == null) {
            initializeEmailSession();
            if (emailSession == null) {
                throw new MessagingException("Email session not initialized - check email configuration");
            }
        }
        
        // IMPROVED: Use fresh connection for each send to avoid authentication issues
        // This eliminates the AuthenticationFailedException caused by stale connections
        Transport transport = null;
        boolean connectionSuccessful = false;
        
        try {
            // Always create a fresh connection to avoid stale connection issues
            transport = emailSession.getTransport("smtp");
            
            // Add connection retry logic for better reliability
            int maxConnectionRetries = 2;
            for (int attempt = 1; attempt <= maxConnectionRetries; attempt++) {
                try {
                    transport.connect();
                    connectionSuccessful = true;
                    break;
                } catch (MessagingException e) {
                    if (attempt == maxConnectionRetries) {
                        throw e; // Last attempt failed
                    }
                    // Wait briefly before retry
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new MessagingException("Connection interrupted", ie);
                    }
                }
            }
            
            if (!connectionSuccessful) {
                throw new MessagingException("Failed to establish SMTP connection after retries");
            }
            
            // Create message with improved headers
            MimeMessage message = new MimeMessage(emailSession);
            try {
                message.setFrom(new InternetAddress(emailUsername, "Smart Helmet"));
            } catch (java.io.UnsupportedEncodingException e) {
                message.setFrom(new InternetAddress(emailUsername));
            }
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Smart Helmet OTP");
            
            // Clear, professional message content
            String messageContent = "Your Smart Helmet verification code: " + otp + 
                                  "\n\nThis code is valid for 10 minutes. Please do not share it with anyone." +
                                  "\n\nIf you didn't request this code, please ignore this email.";
            message.setText(messageContent);
            
            // Set appropriate headers for deliverability
            message.setHeader("X-Priority", "1");
            message.setHeader("X-Mailer", "Smart Helmet");
            
            // Send the message
            transport.sendMessage(message, message.getAllRecipients());
            
        } finally {
            // IMPORTANT: Always close the connection to prevent stale connections
            if (transport != null && transport.isConnected()) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    // Log but don't throw - the main operation may have succeeded
                    //System.err.println("Warning: Error closing SMTP transport: " + e.getMessage());
                }
            }
        }
    }
    
}
