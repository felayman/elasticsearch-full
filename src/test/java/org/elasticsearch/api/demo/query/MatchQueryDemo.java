package org.elasticsearch.api.demo.query;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.api.demo.XPackBaseDemo;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

/**
 * @see <a href='https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-full-text-queries.html#java-query-dsl-simple-query-string-query'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class MatchQueryDemo  extends XPackBaseDemo {

    @Test
    public void testForClient() throws Exception {
        QueryBuilder qb = QueryBuilders.matchQuery("title","quack dog")
                .boost(100.00f)
                .fuzziness(Fuzziness.ONE)
                .prefixLength(0)
//                .operator(Operator.AND)
                ;

        SearchResponse response = client.prepareSearch()
                .setIndices("my_index")
                .setTypes("my_type")
                .setQuery(qb)
                .execute()
                .actionGet();

        println(response);
    }
}
