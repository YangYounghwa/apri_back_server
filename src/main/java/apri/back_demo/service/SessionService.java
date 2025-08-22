package apri.back_demo.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import apri.back_demo.exception.NoSessionFoundException;
import apri.back_demo.model.UserSession;



/**
 *  Checks sessionId, 
 */
@Service
public class SessionService {

    @Autowired
    private JdbcTemplate jdbc;


    /**
     * 
     * @param userId
     * @param apriId
     * @return
     */
    public UserSession createSession(Long userId,Long apriId) {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        jdbc.update("""
                INSERT INTO sessions (session_id, user_id, expires_at,apri_id)
                VALUES (?,?,?,?)

                    """, sessionId, userId, Timestamp.valueOf(expiresAt),apriId);

        UserSession session = new UserSession(sessionId, userId, expiresAt,apriId);
        return session;
    }


    /**
     * validates session UUID
     * @param authString
     * @return
     * @throws NoSessionFoundException
     */
    public UserSession validateSession(String authString) throws NoSessionFoundException {

        if (authString == null || !authString.startsWith("Session ")) {
            System.out.println(authString + " Missing or malformed session header");
            throw new NoSessionFoundException("Missing or malformed session header");
        }


        String sessionId = authString.substring(8).trim();
        try {
            UserSession session = jdbc.queryForObject("""
                    SELECT session_id, user_id, expires_at, apri_id
                    FROM sessions
                    WHERE session_id = ? AND expires_at > NOW()
                        """, (rs, rowNum) -> new UserSession(
                    rs.getString("session_id"),
                    rs.getLong("user_id"),
                    rs.getTimestamp("expires_at").toLocalDateTime(),
                    rs.getLong("apri_id")), sessionId);
            
                    
            LocalDateTime expire1 = LocalDateTime.now().plusHours(1);
            LocalDateTime expire2 =session.getExpiresAt();
            LocalDateTime laterExpire = expire1.isAfter(expire2) ? expire1 : expire2;

            jdbc.update("""
                        UPDATE sessions
                        SET expires_at = ?
                        WHERE session_id = ?
                    """, Timestamp.valueOf(laterExpire), sessionId);

            return session;
        } catch (DataAccessException e) {
            System.err.println(sessionId+"No Session Found!");
            throw new NoSessionFoundException("No Session Found");
        } 
       /* //EmptyResultDataAccessException inherits DataAccessException 
       catch (EmptyResultDataAccessException e){
            throw new NoSessionFoundException(sessionId);
        } */

    }

    public void deleteSession(String authString) throws NoSessionFoundException {
        if (authString == null || !authString.startsWith("Session ")) {
            throw new NoSessionFoundException("Missing or malformed session header");
        }

        String sessionId = authString.substring(8).trim();

        int deleted = jdbc.update("DELETE FROM sessions WHERE session_id = ?", sessionId);

        if (deleted == 0) {
            throw new NoSessionFoundException("Session not found or already deleted");
        }
    }

    @Scheduled(cron = "0 0 * * * *") // every hour
    public void cleanExpiredSessions() {
        int count = jdbc.update("DELETE FROM sessions WHERE expires_at < NOW()");
        System.out.println("Deleted " + count + " expired sessions.");
    }



}