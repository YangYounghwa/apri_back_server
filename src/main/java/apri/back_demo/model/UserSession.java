package apri.back_demo.model;

import java.time.LocalDateTime;

public class UserSession {
    private String sessionId;
    private Long userId;
    private LocalDateTime expiresAt;
    
    public UserSession(String sessionId, Long userId, LocalDateTime expiresAt){
        this.sessionId = sessionId;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public String getSessionId(){ return sessionId; }
    public Long getUserId(){ return userId; }
    public LocalDateTime getExpiresAt(){ return expiresAt;};

}
/*
 *  CREATE TABLE sessions (
        session_id VARCHAR(255) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        expires_at DATETIME NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
 */
