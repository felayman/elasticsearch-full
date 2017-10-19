package org.elasticsearch.api.demo.compound;

import org.elasticsearch.api.demo.BaseDemo;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

/**
 * @see <a href='https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-compound-queries.html'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class BoolQueryDemo extends BaseDemo {

    @Test
    public void testForClient() throws Exception {
        QueryBuilder qb = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("content", "test1"))
                .must(QueryBuilders.termQuery("content", "test4"))
                .mustNot(QueryBuilders.termQuery("content", "test2"))
                .should(QueryBuilders.termQuery("content", "test3"))
                .filter(QueryBuilders.termQuery("content", "test5"));
        client.prepareSearch().setQuery(qb).execute().actionGet();
    }
}
