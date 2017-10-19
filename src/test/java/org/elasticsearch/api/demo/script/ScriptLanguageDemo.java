package org.elasticsearch.api.demo.script;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.api.demo.XPackBaseDemo;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.rescore.QueryRescoreMode;
import org.elasticsearch.search.rescore.QueryRescorerBuilder;
import org.junit.Test;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class ScriptLanguageDemo extends XPackBaseDemo {


    @Test
    public void test() throws Exception {


        SearchResponse searchResponse = client.prepareSearch()
                .setIndices("test")
                .setTypes("test")
                .setQuery(QueryBuilders.matchQuery("name","天津公安"))
                .addRescorer(new QueryRescorerBuilder(QueryBuilders.matchPhraseQuery("name","天津公安")))
                .addRescorer(new QueryRescorerBuilder(
                        QueryBuilders.functionScoreQuery(
                                ScoreFunctionBuilders.scriptFunction("doc['time'].value / 10000")
                        )
                ).windowSize(100).setScoreMode(QueryRescoreMode.Multiply))
                .setFrom(0)
                .setSize(100)
                .execute()
                .actionGet();

    }
}
