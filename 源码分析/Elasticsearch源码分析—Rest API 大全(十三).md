
## Elasticsearch源码分析—Rest API 大全(十三)

> 版本 基于官方最新版本 [5.6](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
> 虽然官方给了许多Elasticsearch Rest API 的 文档,但是有很多只是给出那些经常使用的API,今天,我们深入挖掘隐藏在源码的那些API以及这些API的具体用法和参数分析

### Document API

 官方地址: [Document APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs.html)

 该部分API包含两类操作,一类是针对单个文档的操作,一类是针对多文档的操作


**单文档操作**

 - Index API

        索引一个文档

 - Get API

        获取一个文档

 - Delete API

        删除一个文档

- Update API

        更新一个文档

**多文档操作**

- Multi Get API

        获取多个文档

- Bulk API

        批量操作文档

- Delete By Query API

        通过查询API来删除文档

- Update By Query API

        通过查询API来更新文档

- Reindex API

        重建索引

    下面是针对上述的各个文档API进行源码级别的实例教程(即每个API的详细写法和支持哪些参数)


#### Index API

 > ![](https://www.elastic.co/guide/en/elasticsearch/reference/current/images/icons/important.png) 需要参考[类型删除](https://www.elastic.co/guide/en/elasticsearch/reference/current/removal-of-types.html)

 官方文档地址:[Index API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html)

Index API 主要是用来添加或更新一个文档的API,比如官方的例子:

~~~json
PUT twitter/tweet/1
{
    "user" : "kimchy",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "trying out Elasticsearch"
}
~~~

其中,

~~~
PUT twitter/tweet/1
~~~

是API的请求URI,而

~~~json
{
    "user" : "kimchy",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "trying out Elasticsearch"
}
~~~

是API请求的reqeust body(Elasticsearch中的大多数Rest API 都必须包含 请求URI + 请求BODY)

其中请求URI我们看到的只是官方的一种方式,源码中是支持多种写法的,下面是全部的URI写法:

~~~java
    controller.registerHandler(POST, "/{index}/{type}", this); // auto id creation
    controller.registerHandler(PUT, "/{index}/{type}/{id}", this);
    controller.registerHandler(POST, "/{index}/{type}/{id}", this);
    CreateHandler createHandler = new CreateHandler(settings);
    controller.registerHandler(PUT, "/{index}/{type}/{id}/_create", createHandler);
    controller.registerHandler(POST, "/{index}/{type}/{id}/_create", createHandler);
~~~

具体的详情处理类为: {@link org.elasticsearch.rest.action.document.RestIndexAction}

> ![](https://www.elastic.co/guide/en/elasticsearch/reference/current/images/icons/important.png) 需要注意的是,如果请求URI中不带id,则系统会自动生成一个_id.

**参数**

>  Index API支持多个参数,如果仅仅从官网上来看,提及到几个重要的参数如version,op_type,routing,_parent,timeout等,下面是所有支持的参数列表:

- index

        文档索引名称,必须指定

- type

        文档索引类型,必须指定

- id

        文档ID,可选,如果不填的话,系统会随机生成一个id

- routing

        路由,可选，如果不填,系统会以文档ID为路由计算该文件映射到哪个分片上

- parent

        父文档ID,可选,用来指定该文档与哪个文档是父子文档

- timestamp

        时间戳,可选,官方已经不建议使用

- ttl

        ttl,设置文档的生存时间,官方不建议使用

- pipeline

        pipeline,设置该文档的处理管道,可选

- timeout

        超时时间,可选设置索引文档的超时时间,默认1分钟

- refresh

        设置该文档索引后的刷新策略,有三种策略,NONE(不刷洗),IMMEDIATE(立即刷新),WAIT_UNTIL(等待集群刷新),默认不刷新

- version_type

        设置该文档的版本类型,有三种类型,INTERNAL(内部版本),EXTERNAL(外部版本),EXTERNAL_GTE(外部版本大于或等于内部版本),FORCE(强制指定一个版本)

- op_type

        操作类型,设置该文档操作的类型,有四种操作类型,INDEX(索引文档,如果文档存在则替换),CREATE(创建一个新的文档),UPDATE(更新该文档),DELETE(删除该文档)

- wait_for_active_shards

      设置等待副本分片的数量,必须满足集群中含有该参数大小的副本数量,该文档才会被成功索引

    校验所有参数的源码为:

~~~java
public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        IndexRequest indexRequest = new IndexRequest(request.param("index"), request.param("type"), request.param("id"));
        indexRequest.routing(request.param("routing"));
        indexRequest.parent(request.param("parent"));
        if (request.hasParam("timestamp")) {
            deprecationLogger.deprecated("The [timestamp] parameter of index requests is deprecated");
        }
        indexRequest.timestamp(request.param("timestamp"));
        if (request.hasParam("ttl")) {
            deprecationLogger.deprecated("The [ttl] parameter of index requests is deprecated");
            indexRequest.ttl(request.param("ttl"));
        }
        indexRequest.setPipeline(request.param("pipeline"));
        indexRequest.source(request.requiredContent(), request.getXContentType());
        indexRequest.timeout(request.paramAsTime("timeout", IndexRequest.DEFAULT_TIMEOUT));
        indexRequest.setRefreshPolicy(request.param("refresh"));
        indexRequest.version(RestActions.parseVersion(request));
        indexRequest.versionType(VersionType.fromString(request.param("version_type"), indexRequest.versionType()));
        String sOpType = request.param("op_type");
        String waitForActiveShards = request.param("wait_for_active_shards");
        if (waitForActiveShards != null) {
            indexRequest.waitForActiveShards(ActiveShardCount.parseString(waitForActiveShards));
        }
        if (sOpType != null) {
            indexRequest.opType(sOpType);
        }

        return channel ->
                client.index(indexRequest, new RestStatusToXContentListener<>(channel, r -> r.getLocation(indexRequest.routing())));
    }
~~~

#### Get API

官方文档地址:[Index API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html)

Get API 主要是用来获取一个文档的API,比如官方的例子:

~~~
GET twitter/tweet/0
~~~

源码中不仅支持GET请求,同时还支持HEAD请求,如下：

具体的详情处理类为: {@link org.elasticsearch.rest.action.document.RestGetAction}

~~~java
 controller.registerHandler(GET, "/{index}/{type}/{id}", this);
 controller.registerHandler(HEAD, "/{index}/{type}/{id}", this);
~~~

**参数**

>  GET API支持多个参数,如果仅仅从官网上来看,提及到几个重要的参数如preference,routing等,下面是所有支持的参数列表:

- index

        文档索引名称

- type

        文档索引类型

- id

        文档ID

- refresh

        是否刷新与该文档相关的分片

- routing

        设置路由参数,即决定从哪个分片上获取该文档

- parent

        设置父文档ID

- preference

        设置获取倾向参数,有两种类型,_primary(更倾向从主分片上获取该文档),_local(更倾向从当前节点上查找该文档)

- realtime

        设置该文档的获取是否是即时获取

- fields

        获取文档的哪些字段,默认所有,官方已经不建议使用

- stored_fields

        获取_source中的哪些字段(前提是这些指定的字段必须存储)

- version_type

        获取该文档的版本类型

    校验所有参数的源码为:

~~~java
public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        final GetRequest getRequest = new GetRequest(request.param("index"), request.param("type"), request.param("id"));
        getRequest.operationThreaded(true);
        getRequest.refresh(request.paramAsBoolean("refresh", getRequest.refresh()));
        getRequest.routing(request.param("routing"));  // order is important, set it after routing, so it will set the routing
        getRequest.parent(request.param("parent"));
        getRequest.preference(request.param("preference"));
        getRequest.realtime(request.paramAsBoolean("realtime", getRequest.realtime()));
        if (request.param("fields") != null) {
            throw new IllegalArgumentException("the parameter [fields] is no longer supported, " +
                "please use [stored_fields] to retrieve stored fields or [_source] to load the field from _source");
        }
        final String fieldsParam = request.param("stored_fields");
        if (fieldsParam != null) {
            final String[] fields = Strings.splitStringByCommaToArray(fieldsParam);
            if (fields != null) {
                getRequest.storedFields(fields);
            }
        }

        getRequest.version(RestActions.parseVersion(request));
        getRequest.versionType(VersionType.fromString(request.param("version_type"), getRequest.versionType()));

        getRequest.fetchSourceContext(FetchSourceContext.parseFromRestRequest(request));

        return channel -> client.get(getRequest, new RestToXContentListener<GetResponse>(channel) {
            @Override
            protected RestStatus getStatus(final GetResponse response) {
                return response.isExists() ? OK : NOT_FOUND;
            }
        });
    }
~~~

#### Delete API

官方文档地址:[Delete API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete.html)

Delete API 主要是用来删除一个文档的API,比如官方的例子:

~~~
DELETE /twitter/tweet/1
~~~

具体的详情处理类为: {@link org.elasticsearch.rest.action.document.RestDeleteAction}

删除操作比较简单,支持的参数如下:

- index
- type
- id
- routing
- parent
- timeout
- refresh
- version_type
- wait_for_active_shards

 校验所有参数的源码为:

 ~~~java
  public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
         DeleteRequest deleteRequest = new DeleteRequest(request.param("index"), request.param("type"), request.param("id"));
         deleteRequest.routing(request.param("routing"));
         deleteRequest.parent(request.param("parent")); // order is important, set it after routing, so it will set the routing
         deleteRequest.timeout(request.paramAsTime("timeout", DeleteRequest.DEFAULT_TIMEOUT));
         deleteRequest.setRefreshPolicy(request.param("refresh"));
         deleteRequest.version(RestActions.parseVersion(request));
         deleteRequest.versionType(VersionType.fromString(request.param("version_type"), deleteRequest.versionType()));

         String waitForActiveShards = request.param("wait_for_active_shards");
         if (waitForActiveShards != null) {
             deleteRequest.waitForActiveShards(ActiveShardCount.parseString(waitForActiveShards));
         }

         return channel -> client.delete(deleteRequest, new RestStatusToXContentListener<>(channel));
     }
 ~~~

关于参数的解释,请参考上面的Get API 或 Index API.

#### Update API

官方文档地址:[Update API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html)

Delete API 主要是用来更新一个文档的API,比如官方的例子:

~~~
PUT test/type1/1
{
    "counter" : 1,
    "tags" : ["red"]
}
~~~

其中更新操作是稍微复杂的,因为更新支持脚本更新操作.我们分开来说

直接更新,其实Elasticsearch不支持直接更新,而是覆盖(即覆盖原有文档),比如我们使用下面语句索引一个文档

~~~
PUT /user/user/1
{
  "name":"felayman",
  "age":24
}
~~~

然后再使用下面的语句更新该文档,即带上id

~~~
PUT /user/user/1
{
  "name":"felayman-update"
}
~~~

我们再调用 GET API 来获取该文档,

~~~
GET /user/user/1
~~~

得到结果如下:

~~~
{
  "_index": "user",
  "_type": "user",
  "_id": "1",
  "_version": 2,
  "found": true,
  "_source": {
    "name": "felayman-update"
  }
}
~~~

因为如果我们直接更新文档的话,其实内部操作是删除原文档,再索引新文档.

我们再来看看脚本更新,如果要使用脚本更新的话,我们需要在原有的请求URI后加上_update参数来表明你是想通过脚本来更新文档,语法如下:

~~~
POST test/type1/1/_update
{
    "script" : {
        "source": "ctx._source.counter += params.count",
        "lang": "painless",
        "params" : {
            "count" : 4
        }
    }
}
~~~

由于脚本章节内容比较多,这里不赘述,请参考: [Script](https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-scripting.html)


#### Multi Get API


官方文档地址:[Multi Get API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html)

Multi Get API 主要是用来获取一个或多个文档的API,比如官方的例子:

~~~
GET _mget
{
    "docs" : [
        {
            "_index" : "test",
            "_type" : "type",
            "_id" : "1"
        },
        {
            "_index" : "test",
            "_type" : "type",
            "_id" : "2"
        }
    ]
}
~~~

官方的例子列举出了_mget大部分的用法,这里罗列下源码中支持的所有的写法:

~~~java
controller.registerHandler(GET, "/_mget", this);
controller.registerHandler(POST, "/_mget", this);
controller.registerHandler(GET, "/{index}/_mget", this);
controller.registerHandler(POST, "/{index}/_mget", this);
controller.registerHandler(GET, "/{index}/{type}/_mget", this);
controller.registerHandler(POST, "/{index}/{type}/_mget", this);
~~~

即支持从全部索引中批量获取文档,也支持从某个索引或类型下批量获取文档

#### Bulk API
#### Delete By Query API
#### Update By Query API
#### Reindex API



### 参考

- [Document APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs.html)
- [Search APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/search.html)
- [Indices APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices.html)
- [cat APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat.html)
- [Cluster APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/cluster.html)
-


