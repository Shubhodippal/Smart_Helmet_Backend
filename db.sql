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
    uid VARCHAR(255) NOT NULL,
    emergency_contact_email VARCHAR(255),
    emergency_contact_relation VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_at DATETIME,
    FOREIGN KEY (uid) REFERENCES users(uid)
);

CREATE TABLE profile(
    id INT AUTO_INCREMENT PRIMARY KEY,
    uid VARCHAR(255) NOT NULL UNIQUE,
    address TEXT,
    gender VARCHAR(50),
    bike_registration VARCHAR(255),
    insurance VARCHAR(255),
    blood_group VARCHAR(10),
    med_condition TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (uid) REFERENCES users(uid)
);
