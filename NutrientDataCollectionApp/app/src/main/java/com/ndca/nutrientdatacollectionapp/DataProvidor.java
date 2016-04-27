package com.ndca.nutrientdatacollectionapp;

/**
 * Created by polar_cyclone12 on 4/25/2016.
 */
public class DataProvidor {

    private String name;
    private String id;
    private String location;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public DataProvidor(String name, String id, String location){
        this.name = name;
        this.id = id;
        this.location = location;
    }
}
