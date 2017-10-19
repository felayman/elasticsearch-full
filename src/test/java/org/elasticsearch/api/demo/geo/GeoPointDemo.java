package org.elasticsearch.api.demo.geo;

import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.api.demo.XPackBaseDemo;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 地理坐标点
 * @see <a href='https://www.elastic.co/guide/cn/elasticsearch/guide/current/lat-lon-formats.html'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-04
 */
public class GeoPointDemo extends XPackBaseDemo {

    @Before
    public void init(){
    }

    @After
    public void clean(){

    }

    @Test
    public void testGeoBoundingBoxQueryForClient() throws Exception {

        QueryBuilder queryBuilder =
                QueryBuilders.geoBoundingBoxQuery("location")
                        .setCorners(40.124125,113.493763,39.816239,117.237612);



       SearchResponse searchResponse = client.prepareSearch()
                .setIndices("attractions")
                .setTypes("restaurant")
               .setPostFilter(queryBuilder)
                .execute()
                .actionGet();

        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit :  searchHits){
            System.out.println(searchHit.getSource());
        }
    }


    @Test
    public void testGeoBoundingBoxQueryForElasticsearchTemplate() throws Exception {

        elasticsearchTemplate.createIndex("universities");
        elasticsearchTemplate.putMapping(GeoBoundingBox.class);

        List<IndexQuery> indexQueryList = new ArrayList<>();
        GeoBoundingBox geoBoundingBox = new GeoBoundingBox("中国传媒大学",new Location(39.918054,116.56387));
        IndexQuery indexQuery  = new IndexQuery();
        indexQuery.setId(UUID.randomUUID().toString());
        indexQuery.setIndexName("universities");
        indexQuery.setType("university");
        indexQuery.setSource(JSON.toJSONString(geoBoundingBox));
        indexQuery.setObject(geoBoundingBox);
        indexQueryList.add(indexQuery);

        GeoBoundingBox geoBoundingBox1 = new GeoBoundingBox("北京邮电大学",new Location(39.967366,116.364695));
        IndexQuery indexQuery1  = new IndexQuery();
        indexQuery1.setId(UUID.randomUUID().toString());
        indexQuery1.setIndexName("universities");
        indexQuery1.setType("university");
        indexQuery1.setSource(JSON.toJSONString(geoBoundingBox1));
        indexQuery1.setObject(geoBoundingBox1);
        indexQueryList.add(indexQuery1);

        GeoBoundingBox geoBoundingBox2 = new GeoBoundingBox("北京航空航天大学",new Location(39.986069,116.35347));
        IndexQuery indexQuery2  = new IndexQuery();
        indexQuery2.setId(UUID.randomUUID().toString());
        indexQuery2.setIndexName("universities");
        indexQuery2.setType("university");
        indexQuery2.setSource(JSON.toJSONString(geoBoundingBox2));
        indexQuery2.setObject(geoBoundingBox2);
        indexQueryList.add(indexQuery2);

        elasticsearchTemplate.bulkIndex(indexQueryList);

        QueryBuilder queryBuilder =
                QueryBuilders.geoBoundingBoxQuery("location")
                        .setCorners(40.124125,113.493763,39.816239,117.237612);

        SearchQuery searchQuery = new NativeSearchQuery(queryBuilder);

        List<GeoBoundingBox> geoBoundingBoxes = elasticsearchTemplate.queryForList(searchQuery,GeoBoundingBox.class);
        System.out.println(JSON.toJSONString(geoBoundingBox));



    }

}
