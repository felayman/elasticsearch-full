package org.elasticsearch.api.demo.function;

import org.elasticsearch.api.demo.BaseDemo;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FiltersFunctionScoreQuery;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.junit.Test;

import java.util.Arrays;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-10-30
 */
public class FunctionsDemo extends BaseDemo{

    @Test
    public void test() throws Exception {
        FunctionScoreQueryBuilder.FilterFunctionBuilder [] filterFunctionBuilders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[]
                {
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(ScoreFunctionBuilders.scriptFunction("doc['pubTime'].value*0.00000000001")),
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(ScoreFunctionBuilders.scriptFunction("doc['opinionValue'].value*0.1")),
            };

        QueryBuilders.functionScoreQuery(
                QueryBuilders.queryStringQuery("北京").defaultField("FIELD").field("titleZh^3.0"),
                filterFunctionBuilders
        ).boostMode(CombineFunction.MULTIPLY).scoreMode(FiltersFunctionScoreQuery.ScoreMode.AVG);
    }
}
