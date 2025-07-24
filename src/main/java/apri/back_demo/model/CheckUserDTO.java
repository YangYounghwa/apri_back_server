package apri.back_demo.model;

public class CheckUserDTO {
    private boolean newRegister = false;



    private Long kakao_id;
    private boolean authY = false;


    public CheckUserDTO(boolean newReg, boolean authY, Long kakao_id){
        this.newRegister = newReg;
        this.authY=authY;
        this.kakao_id=kakao_id;
    }

        public boolean isNewRegister() {
        return newRegister;
    }


    public Long getKakao_id() {
        return kakao_id;
    }


    public boolean isAuthY() {
        return authY;
    }
}
