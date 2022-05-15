package cn.studymachine.easyesdemo.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.common.geo.GeoPoint;


/**
 * The type Location dto.
 *
 * @author wukun
 */
@NoArgsConstructor
@Data
public class LocationDTO {
    private Double lat;
    private Double lon;

    public GeoPoint toGeoPoint() {
        return new GeoPoint(lat, lon);
    }

    public String toGeoPointStr() {
        return lat + "," + lon;
    }

}
