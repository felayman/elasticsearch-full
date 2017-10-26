package org.elasticsearch.api.demo.query;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.api.demo.XPackBaseDemo;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
/**
 * @see <a href='https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-full-text-queries.html#java-query-dsl-simple-query-string-query'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class MultiMatchQueryDemo extends XPackBaseDemo {

    private void setup(){

    }

    @Test
    public void testForClient() throws Exception {

        /**
         * @see <a href='https://www.elastic.co/guide/en/elasticsearch/reference/5.6/query-dsl-multi-match-query.html'></a>
         * MultiMatchQuery依赖于match query ,也就是其核心是基于MatchQuery构建的
         *
         */

        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("elasticsearch match query","title", "descrption");

        multiMatchQueryBuilder.analyzer("standard");
        multiMatchQueryBuilder.cutoffFrequency(0.001f);
        multiMatchQueryBuilder.field("title",20);
        multiMatchQueryBuilder.fuzziness(Fuzziness.TWO);
        multiMatchQueryBuilder.maxExpansions(100);
        multiMatchQueryBuilder.prefixLength(10);
        multiMatchQueryBuilder.tieBreaker(20);
        multiMatchQueryBuilder.type(MultiMatchQueryBuilder.Type.BEST_FIELDS);
        multiMatchQueryBuilder.boost(20);



       SearchResponse searchResponse =  client.prepareSearch()
                .setIndices("blogs")
                .setTypes("blog")
                .setQuery(multiMatchQueryBuilder)
                .execute()
                .actionGet();

    }
}
