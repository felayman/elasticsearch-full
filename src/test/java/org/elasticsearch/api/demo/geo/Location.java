package org.elasticsearch.api.demo.geo;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-04
 */
public class Location {


    //纬度
    private double  lat;

    //经度
    private Double lon;

    public Location() {
    }

    public Location(double lat, Double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

}
