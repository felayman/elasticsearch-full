package org.elasticsearch.api.demo;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.junit.Test;

import java.util.List;

/**
 * 测试分词器
 * @see <a href='https://www.elastic.co/guide/cn/elasticsearch/guide/current/standard-tokenizer.html'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-05
 */
public class AnalyzeDemo extends XPackBaseDemo {

    @Test
    public void testTokenizer() throws Exception {
        AnalyzeRequest analyzeRequest = new AnalyzeRequest();
        analyzeRequest.text("My œsophagus caused a débâcle");
        /**
         * whitespace （空白字符）分词器按空白字符 —— 空格、tabs、换行符等等进行简单拆分
         * letter 分词器 ，采用另外一种策略，按照任何非字符进行拆分
         * standard 分词器使用 Unicode 文本分割算法
         */
        analyzeRequest.addTokenFilter("standard");
        analyzeRequest.addCharFilter("asciifolding");
        ActionFuture<AnalyzeResponse> analyzeResponseActionFuture =  client.admin().indices().analyze(analyzeRequest);
        List<AnalyzeResponse.AnalyzeToken> analyzeTokens =  analyzeResponseActionFuture.actionGet().getTokens();
        for (AnalyzeResponse.AnalyzeToken analyzeToken : analyzeTokens){
            System.out.println(analyzeToken.getTerm());
        }
    }
}
