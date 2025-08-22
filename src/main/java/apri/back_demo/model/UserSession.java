package apri.back_demo.model;

import java.time.LocalDateTime;

public class UserSession {
    private String sessionId;
    private Long kakao_id;
    private LocalDateTime expiresAt;
    private Long apri_id;
    
    public UserSession(String sessionId, Long userId, LocalDateTime expiresAt){
        this.sessionId = sessionId;
        this.kakao_id = userId;
        this.expiresAt = expiresAt;
    }


        public UserSession(String sessionId, Long userId, LocalDateTime expiresAt,Long apriId){
        this.sessionId = sessionId;
        this.kakao_id = userId;
        this.expiresAt = expiresAt;
        this.apri_id = apriId;
    }

    public String getSessionId(){ return sessionId; }
    public Long getKakao_id(){ return kakao_id; }
    public LocalDateTime getExpiresAt(){ return expiresAt;};
    public Long getApri_id(){return apri_id;};
}
/*
 *  CREATE TABLE sessions (
        session_id VARCHAR(255) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        expires_at DATETIME NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
 */
