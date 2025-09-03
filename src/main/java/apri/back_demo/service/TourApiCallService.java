package apri.back_demo.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import apri.back_demo.util.QueryParamBuilder;


// TODO : Define exceptions for this. Adjust contorller accordingly
// Exceptions should be used in Result Handler.

@Service
public class TourApiCallService {

    @Value("${gov.data.apikey}")
    String tourapiKey;
    String KORSERVICE1_URL = "https://apis.data.go.kr/B551011/KorService2/";

    String areaCode2 = "areaCode2";// 지역코드조회
    String categoryCode2 = "categoryCode2";// 서비스 분류 코드 조회
    String areaBasedList2 = "areaBasedList2";// 지역기반 관광정보 조회
    String locationBasedList2 = "locationBasedList2";// 위치기반 관광정보 조회
    String searchKeyword2 = "searchKeyword2";// 키워드 검색 조회
    String searchFestival2 = "searchFestival2";// 행사 정보 조회
    String searchStay1 = "searchStay1";// 숙박 정보 조회
    String detailCommon2 = "detailCommon2";// 공통 정보 조회
    String areaBasedSyncList1 = "areaBasedSyncList1";// 국문관광정보 동기화 목록 조회
    String detailPetTour2 = "detailPetTour2";// 국문관광 반려동물 여행정보
    String detailIntro = "detailIntro2";
    

    // String serviceKey = "serviceKey";
    // String numOfRows = "numOfRows";
    // String pageNo = "pageNo";
    // String _type = "_type";

    // String MoblieOS = "MobileOS";
    String IOS = "IOS";
    String ANDROID = "AND";
    String WEB = "WEB";
    String ETC = "ETC";

    String MobileApp = "MobileApp";

    public TourApiCallService() {

    }



    public String apiLocationBasedList(double posX, double posY, double radius){
        return apiLocationBasedList(posX, posY, radius,50,1);
    }


    public String apiLocationBasedList(double posX, double posY, double radius,int rows, int pageNo) {
        String baseUrl = KORSERVICE1_URL + locationBasedList2;

        QueryParamBuilder builder = new QueryParamBuilder(baseUrl, this.tourapiKey, 100, 1, this.ANDROID, "apricot");
        // Requirements for tourAPI
        // includes _type : "json", "MobileOS":"AND",

        builder.add("mapX", posX); // chaining is possible
        builder.add("mapY", posY);
        builder.add("radius", radius);
        builder.add("arrange", "E");// distance based arrangement
        // builder.add("contentTypeId","");
        // type of contents find this later.
        // builder.add("lintYN","Y")
        // or "N", N to check how many i can retrieve, N to retrieve only some.

        String url = builder.build();

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        headers.set("User-Agent", "Mozilla/5.0");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);



        String jsonString = response.getBody();

        /*
         * ObjectMapper mapper = new ObjectMapper();
         * JsonNode root = mapper.readTree(jsonString);
         * 
         * String resultCode =
         * root.path("response").path("header").path("resultCode").asText();
         * String name0 =
         * root.path("response").path("body").path("items").path("item").get(0).path(
         * "name").asText();
         */
        return jsonString;
    }

    /**
     * 
     * @param areaCode
     * @return json string, : code, name, rnum
     */
    public String apiAreaCode(Long areaCode) {

        String baseUrl = KORSERVICE1_URL + this.areaCode2;
        QueryParamBuilder builder = new QueryParamBuilder(baseUrl, this.tourapiKey, 50, 1, this.ANDROID, "apricot");
        // Requirements for tourAPI
        // includes _type : "json", "MobileOS":"AND",

        builder.add("areaCode", areaCode); // chaining is possible

        String url = builder.build();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        headers.set("User-Agent", "Mozilla/5.0");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        return response.getBody();
    }

    public String apiCategoryCode(Long contentTypeId){
        return this.apiCategoryCode(contentTypeId,null,null,null);
    }

    /**
     * 
     * @param contentTypeId
     * @param cat1
     * @param cat2
     * @param cat3
     * @return   json ,      code, name, rnum
     */
    public String apiCategoryCode(Long contentTypeId, String cat1, String cat2, String cat3) {

        String baseUrl = KORSERVICE1_URL + this.categoryCode2;
        QueryParamBuilder builder = new QueryParamBuilder(baseUrl, this.tourapiKey, 100, 1, this.ANDROID, "apricot");
        // Requirements for tourAPI
        // includes _type : "json", "MobileOS":"AND",
        if(contentTypeId != null)
            builder.add("contentTypeId",contentTypeId);
        if(cat1 != null)
        {
            builder.add("cat1",cat1);
            if(cat2 != null){
                builder.add("cat2",cat2);// only works if cat1, exists
                {
                    if(cat3 != null){
                        builder.add("cat3",cat3); // only works if cat1,2 exists
                    }
                }
            }

        }

        
        String url = builder.build();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        headers.set("User-Agent", "Mozilla/5.0");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        return response.getBody();
    }

    public String apiAreaBasedList(Long contentTypeId, String cat1, String cat2, String cat3
    ,String areaCode, String sigunguCode, String IDongRegnCd, String IDongSignguCd,String lclsSystm1,String lclsSystm2,String lclsSystm3) {

        String baseUrl = KORSERVICE1_URL + this.areaBasedList2;
        QueryParamBuilder builder = new QueryParamBuilder(baseUrl, this.tourapiKey, 50, 1, this.ANDROID, "apricot");
        // Requirements for tourAPI
        // includes _type : "json", "MobileOS":"AND",

        builder.add("contentTypeId",contentTypeId);
        if(cat1 != null)
        {
            builder.add("cat1",cat1);
            if(cat2 != null){
                builder.add("cat2",cat2);// only works if cat1, exists
                {
                    if(cat3 != null){
                        builder.add("cat3",cat3); // only works if cat1,2 exists
                    }
                }
            }
        }
        if(areaCode != null) builder.add("areaCode",areaCode);
        if(sigunguCode != null) builder.add("sigunguCode",sigunguCode);


        String url = builder.build();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        headers.set("User-Agent", "Mozilla/5.0");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        return response.getBody();
    }

    public String apiSearchKeyword(String keyword, String cat1, String cat2, String cat3
    ,String areaCode, String sigunguCode, String IDongRegnCd, String IDongSignguCd,String lclsSystm1,String lclsSystm2,String lclsSystm3){


        return apiSearchKeyword(keyword,cat1,cat2,cat3,areaCode,sigunguCode,IDongRegnCd,IDongSignguCd,lclsSystm1,lclsSystm2,lclsSystm3,100,1);
    }

    public String apiSearchKeyword(String keyword, String cat1, String cat2, String cat3, String areaCode, String sigunguCode, String IDongRegnCd, String IDongSignguCd){

        return apiSearchKeyword(keyword,cat1,cat2,cat3,areaCode,sigunguCode, IDongRegnCd,IDongSignguCd,null,null,null);
    }
    
    public String apiSearchKeyword(String keyword, String cat1, String cat2, String cat3
    ,String areaCode, String sigunguCode, String IDongRegnCd, String IDongSignguCd,String lclsSystm1,String lclsSystm2,String lclsSystm3,int rows, int pageNo) {

        String baseUrl = KORSERVICE1_URL + this.searchKeyword2;
        QueryParamBuilder builder = new QueryParamBuilder(baseUrl, this.tourapiKey, rows, pageNo, this.ANDROID, "apricot");
        // Requirements for tourAPI
        // includes _type : "json", "MobileOS":"AND",

        builder.add("keyword",keyword);//MUST
        if(cat1 != null)
        {
            builder.add("cat1",cat1);
            if(cat2 != null){
                builder.add("cat2",cat2);// only works if cat1, exists
                {
                    if(cat3 != null){
                        builder.add("cat3",cat3); // only works if cat1,2 exists
                    }
                }
            }
        }
        if(areaCode != null) builder.add("areaCode",areaCode);
        if(sigunguCode != null) builder.add("sigunguCode",sigunguCode);


        String url = builder.build();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        headers.set("User-Agent", "Mozilla/5.0");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        return response.getBody();
    }


    public String apiSearchFestival(String eventStartDate, String eventEndDate, String cat1, String cat2, String cat3
    ,String areaCode, String sigunguCode, String IDongRegnCd, String IDongSignguCd,String lclsSystm1,String lclsSystm2,String lclsSystm3) {

        String baseUrl = KORSERVICE1_URL + this.searchFestival2;
        QueryParamBuilder builder = new QueryParamBuilder(baseUrl, this.tourapiKey, 50, 1, this.ANDROID, "apricot");
        // Requirements for tourAPI
        // includes _type : "json", "MobileOS":"AND",

        builder.add("eventStartDate",eventStartDate);//MUST
        if(eventEndDate != null) builder.add("eventEndDate",eventEndDate);
        if(areaCode != null) builder.add("areaCode",areaCode);
        if(cat1 != null)
        {
            builder.add("cat1",cat1);
            if(cat2 != null){
                builder.add("cat2",cat2);// only works if cat1, exists
                {
                    if(cat3 != null){
                        builder.add("cat3",cat3); // only works if cat1,2 exists
                    }
                }
            }
        }
        if(areaCode != null) builder.add("areaCode",areaCode);
        if(sigunguCode != null) builder.add("sigunguCode",sigunguCode);


        String url = builder.build();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        headers.set("User-Agent", "Mozilla/5.0");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        return response.getBody();
    }


    public String apiDetailCommon(String contentId) {

        String baseUrl = KORSERVICE1_URL + this.detailCommon2;
        QueryParamBuilder builder = new QueryParamBuilder(baseUrl, this.tourapiKey, 50, 1, this.ANDROID, "apricot");
        // Requirements for tourAPI
        // includes _type : "json", "MobileOS":"AND",

        builder.add("contentId",contentId);//MUST
    
        String url = builder.build();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        headers.set("User-Agent", "Mozilla/5.0");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        return response.getBody();
    }

        public String apiDetailIntro(String contentId,String contentTypeId) {

        String baseUrl = KORSERVICE1_URL + this.detailIntro;
        QueryParamBuilder builder = new QueryParamBuilder(baseUrl, this.tourapiKey, 50, 1, this.ANDROID, "apricot");
        // Requirements for tourAPI
        // includes _type : "json", "MobileOS":"AND",

        builder.add("contentId",contentId);//MUST
        builder.add("contentTypeId",contentTypeId);//MUST
    
        String url = builder.build();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        headers.set("User-Agent", "Mozilla/5.0");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        return response.getBody();
    }

    public String apiDetailPetTour(String contentId) {

        String baseUrl = KORSERVICE1_URL + this.detailPetTour2;
        QueryParamBuilder builder = new QueryParamBuilder(baseUrl, this.tourapiKey, 50, 1, this.ANDROID, "apricot");

        builder.add("contentId",contentId);//MUST
        String url = builder.build();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "Mozilla/5.0");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        return response.getBody();
    }


}
