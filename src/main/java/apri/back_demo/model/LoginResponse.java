package apri.back_demo.model;


// DTO
//not used.
// data object to transfer

/** DTO
 * LoginController
 */
public class LoginResponse {
    private String status;
    private Long userId;
    private String sessionId;
    private boolean newRegister;

    public boolean isNewRegister() {
        return newRegister;
    }
    public void setNewRegister(boolean newRegister) {
        this.newRegister = newRegister;
    }
    public LoginResponse(String status,Long userId, String sessionId){
        this.status = status;
        this.userId = userId;
        this.sessionId = sessionId;
    }
    public String getStatus(){return status;}
    public Long getUserId() {return userId;}
    public String getSessionId(){return sessionId;}
    
}
