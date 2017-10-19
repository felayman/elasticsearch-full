package org.elasticsearch.api.demo.query;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.api.demo.XPackBaseDemo;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

/**
 * @see <a href='https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-term-level-queries.html'></a>
 * @see <a href='https://www.elastic.co/guide/en/elasticsearch/reference/5.5/query-dsl-fuzzy-query.html'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class FuzzyQueryDemo extends XPackBaseDemo {


    @Test
    public void testForClient() throws Exception {
        /**
         * fuzzyQuery基于编辑距离(Levenshtein)来进行相似搜索,比如搜索kimzhy,可以搜索出kinzhy(编辑距离为1)
         * 为了进行测试说明,前创建一个索引,插入几条数据ka,kab,kib,ba,我们的搜索源为ki
         * 了解更多关于编辑距离(Levenshtein)的概念,请参考:<a href='http://www.cnblogs.com/biyeymyhjob/archive/2012/09/28/2707343.html'></a>
         * 了解更多编辑距离的算法,请参考:<a href='http://blog.csdn.net/ironrabbit/article/details/18736185'></a>
         *  ki — ka 编辑距离为1
         *  ki — kab 编辑距离为2
         *  ki — kbia 编辑距离为3
         *  ki — kib 编辑距离为1
         *  所以当我们设置编辑距离(ES中使用fuzziness参数来控制)为0的时候,没有结果
         *  所以当我们设置编辑距离(ES中使用fuzziness参数来控制)为1的时候,会出现结果ka,kib
         *  所以当我们设置编辑距离(ES中使用fuzziness参数来控制)为2的时候,会出现结果ka,kib,kab
         *  所以当我们设置编辑距离(ES中使用fuzziness参数来控制)为3的时候,会出现结果ka,kib,kab,kbaa(很遗憾,ES本身最多只支持到2,因此不会出现此结果)
         */
        QueryBuilder qb = QueryBuilders.fuzzyQuery("username","ki")
//                .fuzziness(Fuzziness.ZERO);  没有结果
//                .fuzziness(Fuzziness.ONE);  会出现结果ka,kib
//                .fuzziness(Fuzziness.TWO);会出现结果ka,kib,kab
                .fuzziness(Fuzziness.AUTO) ////会出现结果ka,kib,kab,但是这里以java-api的方式好像不太好使,原因未定
                .prefixLength(0)
                .boost(1)
                .maxExpansions(100);
        SearchResponse response = client.prepareSearch()
                .setIndices("index")
                .setTypes("type")
                .setQuery(qb)
                .execute()
                .actionGet();
       println(response);
    }
}
