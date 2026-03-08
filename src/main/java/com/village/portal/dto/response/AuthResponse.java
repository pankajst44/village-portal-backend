package com.village.portal.dto.response;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private String username;
    private String role;
    private String fullName;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, long expiresIn,
                        String username, String role, String fullName) {
        this.accessToken  = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn    = expiresIn;
        this.username     = username;
        this.role         = role;
        this.fullName     = fullName;
    }

    public String getAccessToken()  { return accessToken; }
    public void setAccessToken(String v) { this.accessToken = v; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String v) { this.refreshToken = v; }

    public String getTokenType()    { return tokenType; }
    public long getExpiresIn()      { return expiresIn; }

    public String getUsername()     { return username; }
    public void setUsername(String v) { this.username = v; }

    public String getRole()         { return role; }
    public void setRole(String v)   { this.role = v; }

    public String getFullName()     { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
}
