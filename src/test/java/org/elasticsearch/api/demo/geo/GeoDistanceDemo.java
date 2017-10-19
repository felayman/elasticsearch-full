package org.elasticsearch.api.demo.geo;

import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.api.demo.XPackBaseDemo;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-04
 */
public class GeoDistanceDemo extends XPackBaseDemo {

    @Test
    public void test() throws Exception {
        QueryBuilder queryBuilder =
                QueryBuilders.geoDistanceQuery("location")
                .distance(10, DistanceUnit.KILOMETERS)
                .geoDistance(GeoDistance.ARC)
                .point(39.998813,116.317489);


        SortBuilder sortBuilder = SortBuilders.geoDistanceSort("location",39.998813,116.317489)
                .order(SortOrder.DESC)
                .unit(DistanceUnit.KILOMETERS)
                .geoDistance(GeoDistance.PLANE);

        SearchResponse searchResponse = client.prepareSearch()
                .setIndices("attractions")
                .setTypes("restaurant")
                .setPostFilter(queryBuilder)
                .addSort(sortBuilder)
                .execute()
                .actionGet();

        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit :  searchHits){
            System.out.println(searchHit.getSource());
        }
    }

    @Test
    public void testForElasticsearchTemplate() throws Exception {
        QueryBuilder queryBuilder =
                QueryBuilders.geoDistanceQuery("location")
                        .distance(10, DistanceUnit.KILOMETERS)
                        .geoDistance(GeoDistance.ARC)
                        .point(39.998813,116.317489);


        SortBuilder sortBuilder = SortBuilders.geoDistanceSort("location",39.998813,116.317489)
                .order(SortOrder.DESC)
                .unit(DistanceUnit.KILOMETERS)
                .geoDistance(GeoDistance.PLANE);


        List<SortBuilder> sortBuilders = new ArrayList<>();
        sortBuilders.add(sortBuilder);

        SearchQuery searchQuery = new NativeSearchQuery(queryBuilder,null, sortBuilders);



        List<GeoBoundingBox> geoBoundingBoxList = elasticsearchTemplate.queryForList(searchQuery, GeoBoundingBox.class);

        System.out.println(JSON.toJSONString(geoBoundingBoxList));


    }

}
