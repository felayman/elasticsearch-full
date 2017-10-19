package org.elasticsearch.api.demo.query;

import org.elasticsearch.api.demo.BaseDemo;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

/**
 * @see <a href='https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-full-text-queries.html#java-query-dsl-simple-query-string-query'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class SimpleQueryStringQueryDemo extends BaseDemo {

    @Test
    public void testForClient() throws Exception {
        QueryBuilder qb = QueryBuilders.simpleQueryStringQuery("+kimchy -elasticsearch");
        client.prepareSearch().setQuery(qb).execute().actionGet();
    }
}
