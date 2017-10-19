package org.elasticsearch.api.demo.query;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.api.demo.XPackBaseDemo;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-11
 */
public class MatchPhraseQueryDemo extends XPackBaseDemo {


    @Test
    public void test() throws Exception {
        String key = "this is a";
        MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery("title",key);

        matchPhraseQueryBuilder.boost(10);
        matchPhraseQueryBuilder.analyzer("standard");
        matchPhraseQueryBuilder.slop(2);

           SearchResponse searchResponse = client.prepareSearch()
                    .setIndices("my_index")
                    .setTypes("my_type")
                    .setQuery(matchPhraseQueryBuilder)
                    .execute()
                    .actionGet();
    }


}
