# Profile API Integration Guide

## Overview
The Profile API provides CRUD operations for user profiles with JWT authentication. All endpoints require a valid Bearer token in the Authorization header.

## Base URL
```
http://localhost:8084/api/profile
```

## Authentication
All endpoints require JWT authentication via Authorization header:
```
Authorization: Bearer <access_token>
```

---

## 1. Get Profile

### Endpoint
```
GET /api/profile/me
```

### Method
GET

### Input Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| Authorization | Header | Yes | Bearer token for authentication |

### Output Parameters
**Success Response (200 OK):**
```json
{
  "id": 1,
  "uid": "user123",
  "address": "123 Main St, City, State",
  "gender": "Male",
  "bikeRegistration": "KA01AB1234",
  "insurance": "Policy123",
  "bloodGroup": "O+",
  "medCondition": "None",
  "createdAt": "2025-10-05T12:00:00",
  "modifiedAt": "2025-10-05T12:00:00"
}
```

**Error Responses:**
- **401 Unauthorized:** `"Unauthorized: Invalid token"`
- **404 Not Found:** `"Profile not found"`

---

## 2. Create Profile

### Endpoint
```
POST /api/profile/create
```

### Method
POST

### Input Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| Authorization | Header | Yes | Bearer token for authentication |
| address | Body | No | User's address |
| gender | Body | No | User's gender |
| bikeRegistration | Body | No | Bike registration number |
| insurance | Body | No | Insurance policy details |
| bloodGroup | Body | No | Blood group (e.g., O+, A-, B+) |
| medCondition | Body | No | Medical conditions |

### Request Body Example
```json
{
  "address": "123 Main St, City, State",
  "gender": "Male",
  "bikeRegistration": "KA01AB1234",
  "insurance": "Policy123",
  "bloodGroup": "O+",
  "medCondition": "None"
}
```

### Output Parameters
**Success Response (201 Created):**
```json
{
  "id": 1,
  "uid": "user123",
  "address": "123 Main St, City, State",
  "gender": "Male",
  "bikeRegistration": "KA01AB1234",
  "insurance": "Policy123",
  "bloodGroup": "O+",
  "medCondition": "None",
  "createdAt": "2025-10-05T12:00:00",
  "modifiedAt": "2025-10-05T12:00:00"
}
```

**Error Responses:**
- **400 Bad Request:** `"Profile already exists for this user"`
- **401 Unauthorized:** `"Unauthorized: Invalid token"`

---

## 3. Update Profile

### Endpoint
```
PUT /api/profile/update
```

### Method
PUT

### Input Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| Authorization | Header | Yes | Bearer token for authentication |
| address | Body | No | Updated address |
| gender | Body | No | Updated gender |
| bikeRegistration | Body | No | Updated bike registration |
| insurance | Body | No | Updated insurance details |
| bloodGroup | Body | No | Updated blood group |
| medCondition | Body | No | Updated medical conditions |

### Request Body Example
```json
{
  "address": "456 New St, City, State",
  "gender": "Male",
  "bikeRegistration": "KA01AB5678",
  "insurance": "Policy456",
  "bloodGroup": "O+",
  "medCondition": "Allergies"
}
```

### Output Parameters
**Success Response (200 OK):**
```json
{
  "id": 1,
  "uid": "user123",
  "address": "456 New St, City, State",
  "gender": "Male",
  "bikeRegistration": "KA01AB5678",
  "insurance": "Policy456",
  "bloodGroup": "O+",
  "medCondition": "Allergies",
  "createdAt": "2025-10-05T12:00:00",
  "modifiedAt": "2025-10-05T12:30:00"
}
```

**Error Responses:**
- **401 Unauthorized:** `"Unauthorized: Invalid token"`
- **404 Not Found:** `"Profile not found"`

---

## 4. Delete Profile

### Endpoint
```
DELETE /api/profile/delete
```

### Method
DELETE

### Input Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| Authorization | Header | Yes | Bearer token for authentication |

### Output Parameters
**Success Response (200 OK):**
```json
"Profile deleted successfully"
```

**Error Responses:**
- **401 Unauthorized:** `"Unauthorized: Invalid token"`
- **404 Not Found:** `"Profile not found"`

---

## Common Error Codes

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 404 | Not Found |

## Notes

- All profile operations are user-specific (based on JWT token)
- Users can only access/modify their own profiles
- Profile creation is restricted to one profile per user
- Timestamps are automatically managed by the system
- All string fields are optional and can be null/empty