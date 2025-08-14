package apri.back_demo.model.plans;

public class LocationDTO {
    private Long userId;            // required
    private String sourceName;      // e.g., "Kakao" (optional)
    private String externalId;      // provider place id (optional, but great for dedupe)
    private String name;            // optional light cache
    private String address;         // optional light cache
    private Double lon;             // optional if policy forbids storing coords
    private Double lat;             // optional
    private Object details;         // Map<String,Object> or JSON String (optional)
    private Integer ttlDays = 7;    // default TTL

    public void validate() {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if ((lon != null) ^ (lat != null)) {
            throw new IllegalArgumentException("lon and lat must be both null or both non-null");
        }
    }

    // Getters & setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Object getDetails() { return details; }
    public void setDetails(Object details) { this.details = details; }

    public Integer getTtlDays() { return ttlDays; }
    public void setTtlDays(Integer ttlDays) { this.ttlDays = ttlDays; }
}