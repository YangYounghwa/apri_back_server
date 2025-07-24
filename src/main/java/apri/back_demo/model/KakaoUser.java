package apri.back_demo.model;

import java.time.LocalDateTime;




public class KakaoUser {
    private Long apriId=null;
    private Long kakao_user_id;
    private String name;
    private String gender;//male or Female
    private Integer birth_year;

    private LocalDateTime registration_timestamp=null;

    private String thumbnail_url;//TEXT
    private String nickname;

    //empty constructor
    public KakaoUser(){}
    
    public KakaoUser(Long kakao_user_id, String name,String gender,Integer birth_year,String thumbnail_url,String nickname){
        this.kakao_user_id=kakao_user_id;
        this.name=name;
        this.gender=gender;
        this.birth_year=birth_year;
        this.thumbnail_url=thumbnail_url;
        this.nickname=nickname;
    }
    
    public Long getApriId() {
        return apriId;
    }
    public void setApriId(Long apriId) {
        this.apriId = apriId;
    }
    public Long getKakao_user_id() {
        return kakao_user_id;
    }
    public void setKakao_user_id(Long kakao_user_id) {
        this.kakao_user_id = kakao_user_id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
        public Integer getBirth_year() {
        return birth_year;
    }
    public void setBirth_year(Integer birth_year) {
        this.birth_year = birth_year;
    }

    public LocalDateTime getRegistration_timestamp() {
        return registration_timestamp;
    }
    public void setRegistration_timestamp(LocalDateTime registration_timestamp) {
        this.registration_timestamp = registration_timestamp;
    }
    public String getThumbnail_url() {
        return thumbnail_url;
    }
    public void setThumbnail_url(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    

}
