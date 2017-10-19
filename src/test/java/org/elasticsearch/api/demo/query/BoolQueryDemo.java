package org.elasticsearch.api.demo.query;

import org.elasticsearch.api.demo.XPackBaseDemo;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-09-07
 */
public class BoolQueryDemo extends XPackBaseDemo {

    @Test
    public void testBoolQuery() throws Exception {
        List<String> phones = Arrays.asList("18601928820","18601928821","18601928822");
        QueryBuilder qb = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("phone",phones));
        client.prepareSearch().setQuery(qb).execute().actionGet();
    }
}
