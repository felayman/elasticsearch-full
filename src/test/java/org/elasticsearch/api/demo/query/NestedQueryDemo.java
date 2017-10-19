package org.elasticsearch.api.demo.query;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.api.demo.BaseDemo;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-02
 */
public class NestedQueryDemo extends BaseDemo {

    @Test
    public void test() throws Exception {
        NestedQueryBuilder nestedQueryBuilder = QueryBuilders
                .nestedQuery(
                        "keywords",
                        QueryBuilders.termQuery("keywords.keyword","北京"), ScoreMode.None);
        SearchQuery searchQuery = new NativeSearchQuery(nestedQueryBuilder);
    }
}
