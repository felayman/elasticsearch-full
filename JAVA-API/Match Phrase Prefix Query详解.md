## Match Phrase Prefix Query

>  国内对于Elasticsearch深入的人并不多,或者大多数大牛不屑于分享关于Elasticsearch的知识,这里讲讲 Elasticsearch中的Constant Score Query


### 关于

- 官方文档:[官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-match-query-phrase-prefix.html)
- ApacheCN : [中文文档](http://cwiki.apachecn.org/pages/viewpage.action?pageId=4882555)


### 概念

    match_phrase_prefix与match_phrase相同,但是它多了一个特性,就是它允许在文本的最后一个词项(term)上的前缀匹配,如果
    是一个单词,比如a,它会匹配文档字段所有以a开头的文档,如果是一个短语,比如 "this is ma" ,则它会先进行match_phrase查询,找出
    所有包含短语"this is"的的文档,然后在这些匹配的文档中找出所有以"ma"为前缀的文档.

### 语法

~~~
POST /my_index/my_type/_search
{
  "query": {
    "match_phrase_prefix": {
      "title": {
        "query": "this is r",
          "analyzer": "standard",
          "max_expansions": 10,
          "slop":2,
          "boost":100
      }
    }
  }
}
~~~

**参数说明**

- analyzer   指定何种分析器来对该短语进行分词处理
- max_expansions 控制最大的返回结果
- boost 用于设置该查询的权重
- slop 允许短语间的词项(term)间隔


一般来说,match_phrase_prefix可以实现比较粗糙的自动建议(Suggest).


### Java API

~~~
@Test
    public void test() throws Exception {
        String key = "this is a";
        MatchPhrasePrefixQueryBuilder matchPhrasePrefixQueryBuilder = QueryBuilders.matchPhrasePrefixQuery("title",key);

        matchPhrasePrefixQueryBuilder.boost(10);
        matchPhrasePrefixQueryBuilder.analyzer("standard");
        matchPhrasePrefixQueryBuilder.slop(2);
        matchPhrasePrefixQueryBuilder.maxExpansions(100);

        SearchResponse searchResponse = client.prepareSearch()
                .setIndices("my_index")
                .setTypes("my_type")
                .setQuery(matchPhrasePrefixQueryBuilder)
                .execute()
                .actionGet();
        System.out.println(ResponseUtil.parse(searchResponse));
    }

~~~

更多关于Java API,请参考:[MatchPhrasePrefixQueryDemo](https://github.com/felayman/elasticsearch-java-api/blob/master/src/test/java/org/visualchina/elasticsearch/api/demo/query/MatchPhrasePrefixQueryDemo.java)

## 参考
- [Match Phrase Query](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-match-query-phrase.html)
- [Common Terms Query](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-full-text-queries.html#java-query-dsl-common-terms-query)

