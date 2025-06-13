# 🔐 Secure HTTP App - Clojure

A secure HTTP application built in **Clojure** to demonstrate real-world web security techniques including:

- JWT-based session handling
- Bcrypt password hashing
- HMAC payload signing
- Protected file downloads
- Signed expiring download URLs
- Basic CSRF and rate-limiting support (optional)

---

## 🚀 Features

| Feature                        | Description                                                                 |
|-------------------------------|-----------------------------------------------------------------------------|
| 🔒 Signup/Login               | Uses **bcrypt** for password hashing and **JWT** (HS256) for session auth  |
| 📁 File Protection            | Only accessible with valid JWT + HMAC                                      |
| ✍️ HMAC Signing               | Sign and verify filenames for integrity                                    |
| 🕐 Signed URLs (expiring)     | Generate file download links with expiry + HMAC check                      |
| 📜 Swagger Docs               | Live API docs via Compojure + Swagger UI                                   |

---

## 🛠️ Tech Stack

- **Clojure**
- [Compojure-API](https://github.com/metosin/compojure-api)
- [buddy-auth](https://funcool.github.io/buddy-auth/latest/)
- [buddy-hashers](https://funcool.github.io/buddy-hashers/latest/)
- **PostgreSQL** (for user DB)
- Ring, clj-time, clojure.java.io, HMAC (SHA256)

---

## 📦 API Endpoints Summary

| Endpoint                       | Description                                                                                                           |
|-------------------------------|------------------------------------------------------------------------------------------------------------------------|
| `POST /signup`                | Registers a new user by hashing the password (bcrypt) and storing it in postgres DB; returns a JWT with 5 min expiry.  |
| `POST /login`                 | Authenticates a user by checking bcrypt-hashed password; returns a new JWT with 5 min expiry.                          |
| `GET /file`                   | List files (JWT protected)                                                                                             |
| `GET /file/:filename`         | Download file with JWT (signature + expiry + user) + HMAC validation for filename                                      |
| `GET /file/signed-url/:filename` | Returns Signed download link with expiry (10 seconds).                                                              |
| `GET /file/signed/:filename?hmac=...&expires=...`  | Download file via just HMAC (no session) if link not expired                                      |
| `POST /sign-payload`          | Generate HMAC for filename                                                                                             |
| `POST /verify-signed`         | Validate HMAC for filename                                                                                             |

---

## ⚙️ Running the App Locally

1. **Clone the repo:**
   ```bash
   git clone https://github.com/your-username/secure-http-app.git
   cd secure-http-app
   ```
   
2. **Set up the database (PostgreSQL):**
    - DB name: http_app
    - User: postgres, Password: postgres
    - Create users table using:
    ```sql
    create table users (
      id         serial not null,
      username   varchar primary key,
      password   varchar,
      created_at timestamp not null default current_timestamp
    );
    ```
3. **Run the server:**
    ```
    lein run 
    ```

4. **Access Swagger docs at:**
   http://localhost:3000/swagger-ui/index.html

