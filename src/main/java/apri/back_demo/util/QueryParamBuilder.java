package apri.back_demo.util;


import org.springframework.web.util.UriComponentsBuilder;

/**
 * QueryParamBuilder for TourAPI
 */
public class QueryParamBuilder {
    
    UriComponentsBuilder builder;

    /**
     * QueryParamBuilder constructor, common query components for TOURAPI
     * @param baseUrl
     * @param key
     * @param rows
     * @param pageNo
     * @param osType
     * @param app
     */
    public QueryParamBuilder(String baseUrl,String key, int rows, int pageNo,String osType,String app){
       this.builder = UriComponentsBuilder.fromUriString(baseUrl)
       .queryParam("serviceKey",key)
       .queryParam("_type", "json")
       .queryParam("numOfRows",rows)
       .queryParam("pageNo", pageNo)
       .queryParam("MobileApp",app);
            if(osType.equals("IOS") || osType.equals("AND")|| osType.equals("WEB")){
                this.builder.queryParam("MobileOS",osType);}
            else {
                    this.builder.queryParam("MobileOS","ETC"); }
    }


    public QueryParamBuilder add(String key, Object value) {
        this.builder.queryParam(key,String.valueOf(value));
        return this; //
    }

    public String build() {
        return this.builder.build(false).toUriString();
    }

}
