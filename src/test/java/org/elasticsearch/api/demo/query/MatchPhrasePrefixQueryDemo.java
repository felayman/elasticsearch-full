package org.elasticsearch.api.demo.query;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.api.demo.XPackBaseDemo;
import org.elasticsearch.index.query.MatchPhrasePrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-11
 */
public class MatchPhrasePrefixQueryDemo extends XPackBaseDemo {

    @Test
    public void test() throws Exception {
        String key = "this is a";
        MatchPhrasePrefixQueryBuilder matchPhrasePrefixQueryBuilder = QueryBuilders.matchPhrasePrefixQuery("title",key);

        matchPhrasePrefixQueryBuilder.boost(10);
        matchPhrasePrefixQueryBuilder.analyzer("standard");
        matchPhrasePrefixQueryBuilder.slop(2);
        matchPhrasePrefixQueryBuilder.maxExpansions(100);

        SearchResponse searchResponse = client.prepareSearch()
                .setIndices("my_index")
                .setTypes("my_type")
                .setQuery(matchPhrasePrefixQueryBuilder)
                .execute()
                .actionGet();
    }

}
