package models;

public class User {
    private String id;
    private String email;
    private String passwordHash;
    private String name;
    private String createdAt;
    private String oauthProvider;
    private String oauthId;
    private String oauthEmail;
    private boolean emailVerified;
    private String verificationCode;
    private String verificationExpires;
    private String lastLogin;
    private boolean phoneVerified;  // Add this field at the top with other variables
    private boolean textVerified;


    // Constructors
    public User() {}
    
    public User(String email, String passwordHash, String name) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.emailVerified = false;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getOauthProvider() { return oauthProvider; }
    public void setOauthProvider(String oauthProvider) { this.oauthProvider = oauthProvider; }
    
    public String getOauthId() { return oauthId; }
    public void setOauthId(String oauthId) { this.oauthId = oauthId; }
    
    public String getOauthEmail() { return oauthEmail; }
    public void setOauthEmail(String oauthEmail) { this.oauthEmail = oauthEmail; }
    
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    
    public String getVerificationExpires() { return verificationExpires; }
    public void setVerificationExpires(String verificationExpires) { this.verificationExpires = verificationExpires; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }


    public boolean isTextVerified() {
        return textVerified;
    }

    public void setTextVerified(boolean textVerified) {
        this.textVerified = textVerified;
    }

    public void setPhoneVerified(boolean verified) {
        this.phoneVerified = verified;
    }
    // Getter - allows other classes to check if phone is verified
    public boolean isPhoneVerified() {
        return phoneVerified;
    }
}

