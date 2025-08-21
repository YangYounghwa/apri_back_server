package apri.back_demo.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import apri.back_demo.dto.CheckUserDTO;
import apri.back_demo.exception.KakaoResponseException;
import apri.back_demo.model.KakaoUser;

@Service
public class OAuthKakaoService {


    @Autowired 
    private UserRegisteration userRegisteration;

    
    public CheckUserDTO checkUser(String ACCESS_TOKEN) throws KakaoResponseException{
        return this.checkUser(ACCESS_TOKEN,false);
    }
    
    public CheckUserDTO checkUser(String ACCESS_TOKEN,boolean test) throws KakaoResponseException{
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization","Bearer "+ACCESS_TOKEN);
        headers.set("Content-Type","application/x-www-form-urlencoded;charset=utf-8");


        //kakao_account.gender
        //kakao_account.birthday


        //name, gender, birthyear, profile
        
        // kakao_account.profile
        // kakao_account.name
        // kakao_account.email
        // kakao_account.age_range
        // kakao_account.birthday
        // kakao_account.gender

        MultiValueMap<String, String> verifBody = new LinkedMultiValueMap<>();

        verifBody.add("property_keys","[\"kakao_account.profile\",\"kakao_account.gender\",\"kakao_account.birthday\"]");
        HttpEntity<MultiValueMap<String,String>> request = new HttpEntity<>(verifBody,headers);
        System.out.println("token" +ACCESS_TOKEN);
        ResponseEntity<Map> response = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.POST,
            request, 
            Map.class
        );
        System.out.println(response.toString());
        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("id")){
            
            throw new KakaoResponseException("Kakao_response exception");
            //return null;
        }

        Object connected_at = body.get("connected_at");
        Object synched_at = body.get("synched_at");

        Map<String,Object> kakao_account = (Map<String,Object>) body.get("kakao_account");

        String name = (String) kakao_account.get("name");
        String birthyear = (String) kakao_account.get("birthyear");

        //male, female
        String gender = (String) kakao_account.get("gender");

        Map<String, Object> profile = (Map<String,Object>) kakao_account.get("profile");     
        
        String nickname = (String) profile.get("nickname");
        String thumbnail_image_url = (String) profile.get("thumbnail_image_url");


        Long id = null;

        
        if(body.get("id")==null)  id = null;
        else id = ((Number) body.get("id")).longValue();

        Integer birth_yearInt = null;
        if(birthyear != null){
            birth_yearInt = Integer.valueOf(birthyear);
        }        
        boolean newRegis=false;
        KakaoUser user = new KakaoUser(id,name,gender,birth_yearInt,thumbnail_image_url,nickname);
        if(!userRegisteration.existsByKakaoId(id)){
            userRegisteration.registerUser(user);
            newRegis = true;
        }
        CheckUserDTO cud; 
        if(id!= null){
        cud = new CheckUserDTO(newRegis,true, id);}
        else { cud = new CheckUserDTO(false, false, null);
        }
        return cud;

    }
    public int kakaoLogout(){
        
        return 1;
    }

    
}
