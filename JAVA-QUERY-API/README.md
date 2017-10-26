 # elasticsearch-java-api

 查看官方JAVA-API中文文档,请参考: [JAVA-API 中文文档](https://felayman.gitbooks.io/elastic-5-0-java-api/content/)

关于各种查询以及java-api的代码,请参考:  [Elasticsearch-java-api](https://github.com/felayman/elasticsearch-java-api),如果想进行补充,欢迎PR.

## 例如match_all查询

###  使用client

~~~java
   QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
   SearchResponse searchResponse = client.prepareSearch()
                  .setIndices("indexName")
                  .setTypes("typeName")
                  .setQuery(queryBuilder)
                  .execute()
                  .actionGet();
~~~

### 使用elasticsearchTemplate

~~~java
 QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
 SearchQuery searchQuery  = new NativeSearchQuery(
                QueryBuilders.matchAllQuery()
        );
 elasticsearchTemplate.queryForList(searchQuery, XXXXX.class);
~~~

###  使用 RestClient

~~~java
 String method = "POST";
 String endpoint = "/index/type/_search";
 HttpEntity entity = new NStringEntity("{\n" +
         "  \"query\": {\n" +
         "    \"match_all\": {}\n" +
         "  }\n" +
         "}", ContentType.APPLICATION_JSON);

 Response response = restClient.performRequest(method,endpoint,Collections.<String, String>emptyMap(),entity);
 System.out.println(EntityUtils.toString(response.getEntity()));
~~~

## 下载

    git clone git@github.com:felayman/elasticsearch-java-api.git


## 参考

- [spring-data-elasticsearch](https://github.com/spring-projects/spring-data-elasticsearch)
- [Java REST Client [5.5]](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/_example_requests.html)


## License

      This software is licensed under the Apache License, version 2 ("ALv2"), quoted below.

      Copyright https://github.com/elastic/elasticsearch

      Licensed under the Apache License, Version 2.0 (the "License"); you may not
      use this file except in compliance with the License. You may obtain a copy of
      the License at

          http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
      WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
      License for the specific language governing permissions and limitations under
      the License.
