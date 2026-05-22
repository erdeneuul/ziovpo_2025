# ЗИоВПО — Task 1: Project Setup

## What this project does

A Spring Boot server with:
- **JWT authentication** (access + refresh tokens)
- **Role-based access control** (ROLE_USER and ROLE_ADMIN)
- **HTTPS** (TLS via keystore)
- **PostgreSQL** database
- **CI/CD** pipeline (GitHub Actions)

---

## Step-by-step setup from scratch

### 1. Install required tools

- [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (Community edition is free)
- [PostgreSQL](https://www.postgresql.org/download/)
- [Postman](https://www.postman.com/downloads/) (for testing APIs)
- Git

---

### 2. Create the database

Open PostgreSQL (pgAdmin or terminal) and run:

```sql
CREATE DATABASE ziovpo_db;
```

---

### 3. Generate HTTPS certificate (keystore)

Run this in your terminal (Git Bash on Windows):

```bash
keytool -genkeypair \
  -alias mykey \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365 \
  -keystore src/main/resources/keystore.jks \
  -storepass changeit \
  -keypass changeit \
  -dname "CN=localhost, OU=Student, O=MFA, L=Moscow, S=Moscow, C=RU"
```

> ⚠️ Add `keystore.jks` to `.gitignore` — NEVER commit it!

---

### 4. Set environment variables

Create a file `.env` (or set them in IntelliJ Run Configuration):

```
DB_URL=jdbc:postgresql://localhost:5432/ziovpo_db
DB_USERNAME=postgres
DB_PASSWORD=yourpostgrespassword
JWT_SECRET=ThisIsAVeryLongSecretKeyThatIsAtLeast32CharactersLong!!!
KEYSTORE_PASSWORD=changeit
KEY_ALIAS=mykey
```

In IntelliJ: Run → Edit Configurations → Environment Variables → paste the above

---

### 5. Run the application

```bash
mvn spring-boot:run
```

The app starts at: **https://localhost:8443**

---

### 6. Test with Postman

#### Register a new user
```
POST https://localhost:8443/auth/register
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "SecurePass1!"
}
```
Response:
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc..."
}
```

#### Login
```
POST https://localhost:8443/auth/login
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "SecurePass1!"
}
```

#### Access a protected endpoint
```
GET https://localhost:8443/api/hello
Authorization: Bearer eyJhbGc...
```
Response: `{ "message": "Hello, alice@example.com!", "role": "ROLE_USER" }`

#### Refresh tokens
```
POST https://localhost:8443/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGc..."
}
```

#### Try the admin endpoint (should fail with USER token → 403)
```
GET https://localhost:8443/api/admin/secret
Authorization: Bearer eyJhbGc...
```

---

### 7. Add GitHub Secrets for CI/CD

In your GitHub repo → Settings → Secrets and variables → Actions → New repository secret:

| Secret name        | Value                                              |
|--------------------|----------------------------------------------------|
| `JWT_SECRET`       | Same long string as in your .env                  |
| `KEYSTORE_BASE64`  | Run: `base64 -i keystore.jks` → paste the output  |
| `KEYSTORE_PASSWORD`| `changeit` (or whatever you set)                   |
| `KEY_ALIAS`        | `mykey`                                            |

---

## Understanding what was built

### The flow when a user logs in:

```
Client                    Server                   Database
  |                          |                         |
  |-- POST /auth/login ----→ |                         |
  |   { email, password }    |-- find user by email →  |
  |                          |← user record ---------- |
  |                          |                         |
  |                          | (check BCrypt hash)     |
  |                          |                         |
  |                          | (generate access JWT)   |
  |                          | (generate refresh JWT)  |
  |                          |                         |
  |                          |-- save session -------→ |
  |                          |                         |
  |← { accessToken,          |                         |
  |    refreshToken }        |                         |
```

### The flow when a user makes an API call:

```
Client                    JwtFilter              Controller
  |                          |                       |
  |-- GET /api/hello ------→ |                       |
  |   Authorization: Bearer  |                       |
  |                          | (validate JWT         |
  |                          |  signature+expiry)    |
  |                          |                       |
  |                          | (set authentication   |
  |                          |  in SecurityContext)  |
  |                          |                       |
  |                          |--- forward request →  |
  |                          |                       | (check role)
  |← 200 "Hello, alice!" ←- |←-- return response -- |
```

---

## Project structure

```
src/main/java/ru/mfa/
├── ZiovpoApplication.java        ← entry point
├── model/
│   ├── User.java                 ← user stored in DB
│   ├── Role.java                 ← ROLE_USER, ROLE_ADMIN
│   ├── UserSession.java          ← session (refresh token) stored in DB
│   └── SessionStatus.java        ← ACTIVE, USED, REVOKED
├── repository/
│   ├── UserRepository.java       ← queries users table
│   └── UserSessionRepository.java← queries user_sessions table
├── security/
│   ├── JwtProperties.java        ← reads jwt.* from application.yml
│   ├── JwtTokenProvider.java     ← creates and validates JWT tokens
│   └── JwtAuthenticationFilter.java ← checks JWT on every request
├── service/
│   ├── AuthService.java          ← register and login logic
│   └── TokenService.java         ← token creation, refresh, revocation
├── controller/
│   ├── AuthController.java       ← POST /auth/login etc.
│   └── TestController.java       ← GET /api/hello (for testing)
├── dto/
│   └── AuthDtos.java             ← request/response objects
└── config/
    ├── SecurityConfig.java       ← security rules
    └── GlobalExceptionHandler.java ← clean error responses
```
