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
public class ConstantScoreQueryDemo extends BaseDemo {

    @Test
    public void testForClient() throws Exception {
        QueryBuilder qb = QueryBuilders.constantScoreQuery(
                QueryBuilders.termQuery("name","kimchy")
        ).boost(2.0f);
        client.prepareSearch().setQuery(qb).execute().actionGet();
    }
}
