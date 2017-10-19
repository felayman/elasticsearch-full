package org.elasticsearch.api.demo.geo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-10-19
 */
@Document(indexName = "universities",type = "university")
public class GeoBoundingBox {

    @Id
    private String id;

    @Field(type = FieldType.text)
    private String name;

    @GeoPointField
    private Location location;

    public GeoBoundingBox() {
    }

    public GeoBoundingBox(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}