package org.elasticsearch.api.demo.compound;

import org.elasticsearch.api.demo.BaseDemo;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.junit.Test;

/**
 * @see <a href='https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-compound-queries.html'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class FunctionScoreQueryDemo extends BaseDemo {

    @Test
    public void testForClient() throws Exception {
        FunctionScoreQueryBuilder.FilterFunctionBuilder[] functions = {
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        QueryBuilders.matchQuery("name", "kimchy"),
                        ScoreFunctionBuilders. randomFunction("ABCDEF")),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        ScoreFunctionBuilders.exponentialDecayFunction("age", 0L, 1L))
        };
        QueryBuilder qb = QueryBuilders.functionScoreQuery(functions);
        client.prepareSearch().setQuery(qb).execute().actionGet();
    }
}
