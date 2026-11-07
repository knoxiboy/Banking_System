package Projects_java.Banking_System.model;

import java.sql.Timestamp;

/**
 * POJO (Plain Old Java Object) representing a registered user.
 *
 * OOP Concept — Encapsulation: all fields are private; access is provided
 * exclusively through public getter and setter methods.
 */
public class User {

    private int       id;
    private String    fullName;
    private String    email;
    private String    password;   // stored as SHA-256 hash
    private String    role;       // "USER" or "ADMIN"
    private Timestamp createdAt;

    /** Default constructor (required for DAO mapping). */
    public User() {}

    /**
     * Convenience constructor for registration.
     * The password passed here is the PLAIN TEXT — PasswordUtil.hash() is
     * applied before it reaches the database.
     */
    public User(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email    = email;
        this.password = password;
        this.role     = "USER";
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public int       getId()        { return id; }
    public String    getFullName()  { return fullName; }
    public String    getEmail()     { return email; }
    public String    getPassword()  { return password; }
    public String    getRole()      { return role; }
    public Timestamp getCreatedAt() { return createdAt; }

    // ─── Setters ─────────────────────────────────────────────────────────────

    public void setId(int id)                   { this.id        = id; }
    public void setFullName(String fullName)     { this.fullName  = fullName; }
    public void setEmail(String email)           { this.email     = email; }
    public void setPassword(String password)     { this.password  = password; }
    public void setRole(String role)             { this.role      = role; }
    public void setCreatedAt(Timestamp createdAt){ this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + fullName + "', email='" + email + "', role='" + role + "'}";
    }
}

