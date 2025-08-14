package apri.back_demo.model.plans;

import graph_routing_01.Finder.model.ApriPathDTO;

public class RouteDTO {
    private Long userId;                 // required
    private String name;                 // optional
    private ApriPathDTO path; // required (from your library)

    private Double distanceMeters;       // optional
    private Integer durationSeconds;     // optional
    private Object details;              // Map<String,Object> or JSON String (optional)
    private Integer ttlDays = 30;         // default TTL

    public void validate() {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (path == null) throw new IllegalArgumentException("path (ApriPathDTO) is required");
        // If your library DTO doesn't have a validate() method, you can add your own checks here if desired.
    }

    // Getters & setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ApriPathDTO getPath() { return path; }
    public void setPath(ApriPathDTO path) { this.path = path; }

    public Double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public Object getDetails() { return details; }
    public void setDetails(Object details) { this.details = details; }

    public Integer getTtlDays() { return ttlDays; }
    public void setTtlDays(Integer ttlDays) { this.ttlDays = ttlDays; }
}