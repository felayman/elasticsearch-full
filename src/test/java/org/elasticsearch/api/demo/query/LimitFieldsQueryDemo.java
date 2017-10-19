package org.elasticsearch.api.demo.query;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.api.demo.BaseDemo;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import java.util.Arrays;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-09-07
 */
public class LimitFieldsQueryDemo  extends BaseDemo {

    @Test
    public void name() throws Exception {

       SearchResponse searchResponse =  client.prepareSearch()
                .setIndices("test")
                .setTypes("test")
                .setFetchSource((String[]) Arrays.asList("age").toArray(),null)
               .setQuery(QueryBuilders.matchAllQuery())
                .execute()
                .actionGet();
    }
}
