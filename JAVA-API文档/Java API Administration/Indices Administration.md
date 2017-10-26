## 索引管理(Indices Administration)

    官方文档:[Indices Administration](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-admin-indices.html)


为了使用Indices Java API,你需要调用AdminClient的indices()方法

~~~java
IndicesAdminClient indicesAdminClient = client.admin().indices();
~~~

> ![](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/images/icons/note.png)
> 在剩下的教程中,我们将会使用client.admin().

### 创建索引(Create Index)

使用IndicesAdminClient,你可以创建一个没有mapping的默认设置的索引

~~~java
client.admin().indices().prepareCreate("twitter").get();
~~~

### 索引设置(Index Settings)

每个索引的创建都可以指定一个设置来关联该索引

~~~java
client.admin().indices().prepareCreate("twitter")
        .setSettings(Settings.builder()             （1）
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        )
        .get();                                                                （2）
~~~

1. 为这个索引设置setting
2. 执行请求等待结果返回

### 设置Mapping(Put Mapping)

设置mapping api允许你在创建一个新的索引的时候设置mapping

~~~java
client.admin().indices().prepareCreate("twitter")   (1)
        .addMapping("tweet", "{\n" +                                (2)
                "    \"tweet\": {\n" +
                "      \"properties\": {\n" +
                "        \"message\": {\n" +
                "          \"type\": \"string\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }")
        .get();
~~~

1. 创建一个名称为"twitter"的索引
2. 添加一个"tweet"的mapping类型

设置mapping api也允许你在一个已经创建的索引上设置一个mapping

~~~java
client.admin().indices().preparePutMapping("twitter")   (1)
        .setType("user")                                                                    (2)
        .setSource("{\n" +                                                                 (3)
                "  \"properties\": {\n" +
                "    \"name\": {\n" +
                "      \"type\": \"string\"\n" +
                "    }\n" +
                "  }\n" +
                "}")
        .get();

// You can also provide the type in the source document
client.admin().indices().preparePutMapping("twitter")
        .setType("user")
        .setSource("{\n" +
                "    \"user\":{\n" +                                                       (4)
                "        \"properties\": {\n" +
                "            \"name\": {\n" +
                "                \"type\": \"string\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}")
        .get();
~~~

1. 在一个已经存在的索引上"twitter"设置一个mapping
2. 添加一个名称为"user" 的mapping
3. "user"mapping有预定义的字段类型
4. mapping的类型也可以在source中提供

你也可以利用同样的API来更新一个已经存在的mapping

~~~java
client.admin().indices().preparePutMapping("twitter")           (1)
        .setType("user")                                                                            (2)
        .setSource("{\n" +                                                                        (3)
                "  \"properties\": {\n" +
                "    \"user_name\": {\n" +
                "      \"type\": \"string\"\n" +
                "    }\n" +
                "  }\n" +
                "}")
        .get();
~~~

1. 在一个已经存在的索引上"twitter"设置一个mapping
2. 更新名称为"user"的mapping
3. "user"mapping新增了一个"user_name"的字段

### 刷新(Refresh)

refresh api 允许明确的刷新某个或多个索引

~~~java
client.admin().indices().prepareRefresh().get();    (1)
client.admin().indices()
        .prepareRefresh("twitter")                                      (2)
        .get();
client.admin().indices()
        .prepareRefresh("twitter", "company")               (3)
        .get();
~~~

1. 刷新所有索引
2. 刷新一个索引
3. 刷新多个索引

### 获取设置(Get Settings)

 get settings api 允许你从重新获取某个或多个索引的setting信息

 ~~~java
 GetSettingsResponse response = client.admin().indices()                                                         (1)
         .prepareGetSettings("company", "employee").get();
 for (ObjectObjectCursor<String, Settings> cursor : response.getIndexToSettings()) {    (2)
     String index = cursor.key;                                                                                                                 (3)
     Settings settings = cursor.value;                                                                                                     (4)
     Integer shards = settings.getAsInt("index.number_of_shards", null);                                 (5)
     Integer replicas = settings.getAsInt("index.number_of_replicas", null);                            (6)
 }
 ~~~

 1. 从名称为"company","employee"的索引中获取settings
 2. 遍历结果
 3. 索引名称
 4. 给定索引的setting信息
 5. 索引的主分片数量
 6. 索引的副本分片的数量

 ### 更新索引设置(Update Indices Settings)

 你可以通过如下的方式来更改索引的settings信息

 ~~~java
 client.admin().indices().prepareUpdateSettings("twitter")      (1)
         .setSettings(Settings.builder()                                                    (2)
                 .put("index.number_of_replicas", 0)
         )
         .get();
 ~~~

 1. 更新索引
 2. 设置settings信息


 ## 源码小窥

IndicesAdminClient接口是用来管理索引相关的操作,有如下方法:

**ActionFuture<IndicesExistsResponse> exists(IndicesExistsRequest request);**

判断索引是否存在

**IndicesExistsRequestBuilder prepareExists(String... indices);**

判断多个索引是否存在

**ActionFuture<TypesExistsResponse> typesExists(TypesExistsRequest request);**

判断类型是否妇女在

** ActionFuture<IndicesStatsResponse> stats(IndicesStatsRequest request);**

获取索引相关统计信息

** ActionFuture<RecoveryResponse> recoveries(RecoveryRequest request);**

恢复索引

**ActionFuture<IndicesSegmentResponse> segments(IndicesSegmentsRequest request);**

获取索引的段信息

**  ActionFuture<IndicesShardStoresResponse> shardStores(IndicesShardStoresRequest request);**

获取索引的分片存储信息

**ActionFuture<CreateIndexResponse> create(CreateIndexRequest request);**

创建索引

**ActionFuture<DeleteIndexResponse> delete(DeleteIndexRequest request);**

删除索引

**ActionFuture<CloseIndexResponse> close(CloseIndexRequest request);**

关闭索引

**ActionFuture<OpenIndexResponse> open(OpenIndexRequest request);**

打开索引

**ActionFuture<RefreshResponse> refresh(RefreshRequest request);**

刷新索引

**ActionFuture<FlushResponse> flush(FlushRequest request);**

清空索引缓冲区

**ActionFuture<ForceMergeResponse> forceMerge(ForceMergeRequest request);**

强制合并

**ActionFuture<UpgradeResponse> upgrade(UpgradeRequest request);**

索引升级

**ActionFuture<GetMappingsResponse> getMappings(GetMappingsRequest request);**

获取某个索引下的全部mapping信息

**ActionFuture<GetFieldMappingsResponse> getFieldMappings(GetFieldMappingsRequest request);**

获取某个字段的mapping信息

**ActionFuture<PutMappingResponse> putMapping(PutMappingRequest request);**

设置mapping

**ActionFuture<IndicesAliasesResponse> aliases(IndicesAliasesRequest request);**

设置索引的别名

**ActionFuture<IndicesAliasesResponse> aliases(IndicesAliasesRequest request);**

获取索引别名信息

**ActionFuture<AliasesExistResponse> aliasesExist(GetAliasesRequest request);**

判断某个别名是否存在

**ActionFuture<GetIndexResponse> getIndex(GetIndexRequest request);**

获取指定索引的meta信息

**ActionFuture<ClearIndicesCacheResponse> clearCache(ClearIndicesCacheRequest request);**

清空缓存

**ActionFuture<UpdateSettingsResponse> updateSettings(UpdateSettingsRequest request);**

更新settings信息

**ActionFuture<AnalyzeResponse> analyze(AnalyzeRequest request);**

使用指定分析器来分析文本

**ActionFuture<PutIndexTemplateResponse> putTemplate(PutIndexTemplateRequest request);**

设置模板

**ActionFuture<DeleteIndexTemplateResponse> deleteTemplate(DeleteIndexTemplateRequest request);**

删除索引模板

**ActionFuture<GetIndexTemplatesResponse> getTemplates(GetIndexTemplatesRequest request);**

获取模板信息

**ActionFuture<ValidateQueryResponse> validateQuery(ValidateQueryRequest request);**

校验查询请求

**ActionFuture<GetSettingsResponse> getSettings(GetSettingsRequest request);**

获取索引的settings信息

**ActionFuture<ShrinkResponse> shrinkIndex(ShrinkRequest request);**

收缩索引的分片数

**ActionFuture<RolloverResponse> rolloversIndex(RolloverRequest request);**

索引分割

