CREATE TABLE users(
    id INT AUTO_INCREMENT PRIMARY KEY, 
    uid VARCHAR(255) NOT NULL UNIQUE, 
    name VARCHAR(255) NOT NULL, 
    email VARCHAR(255) UNIQUE NOT NULL, 
    phone VARCHAR(20), 
    password VARCHAR(255) NOT NULL, 
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, 
    last_login DATETIME
);

CREATE TABLE emergency_contacts(
    id INT AUTO_INCREMENT PRIMARY KEY,
    uid VARCHAR(255) NOT NULL UNIQUE,
    emergency_contact_1_email VARCHAR(255),
    emergency_contact_1_relation VARCHAR(255),
    emergency_contact_2_email VARCHAR(255),
    emergency_contact_2_relation VARCHAR(255),
    emergency_contact_3_email VARCHAR(255),
    emergency_contact_3_relation VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_at DATETIME,
    FOREIGN KEY (uid) REFERENCES users(uid)
);
