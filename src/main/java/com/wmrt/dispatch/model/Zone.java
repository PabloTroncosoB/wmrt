package com.wmrt.dispatch.model;

/**
 * Represents a geographic dispatch zone.
 */
public class Zone {

    private Long id;
    private String name;
    private String region;
    private String coverageDescription;

    public Zone() {}

    public Zone(Long id, String name, String region, String coverageDescription) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.coverageDescription = coverageDescription;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCoverageDescription() { return coverageDescription; }
    public void setCoverageDescription(String coverageDescription) {
        this.coverageDescription = coverageDescription;
    }

    @Override
    public String toString() {
        return "Zone{id=" + id + ", name='" + name + "', region='" + region + "'}";
    }
}
