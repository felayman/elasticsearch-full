
> Elasticsearch版本为 5.5.0,下面是主要的核心流程,忽略异常补偿部分

### Elasticsearch支持创建索引的rest命令有如下几种格式
- curl - XPOST  'http://localhost:9200/{index}/{type}'
- curl - XPUT  'http://localhost:9200/{index}/{type}/{id}'
- curl - XPOST  'http://localhost:9200/{index}/{type}/{id}'
- curl - XPOST  'http://localhost:9200/{index}/{type}/{id}'
- curl - XPOST  'http://localhost:9200/{index}/{type}/{id}/_create'
- curl - XPUT  'http://localhost:9200/{index}/{type}/{id}/_create'
- curl - XPOST  'http://localhost:9200/{index}/{type}/{id}/_create'

源码说明如下:
~~~java
public RestIndexAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(POST, "/{index}/{type}", this); // auto id creation
        controller.registerHandler(PUT, "/{index}/{type}/{id}", this);
        controller.registerHandler(POST, "/{index}/{type}/{id}", this);
        CreateHandler createHandler = new CreateHandler(settings);
        controller.registerHandler(PUT, "/{index}/{type}/{id}/_create", createHandler);
        controller.registerHandler(POST, "/{index}/{type}/{id}/_create", createHandler);
    }
~~~

**说明:**
 > 上面的所有创建索引的rest命令,都必须有一个request body,比如如果我们直接执行 curl -XPOST 'http://localhost:9200/twitter/tweet/',则会返回如下的错误信息:

 ~~~json
{"error":{"root_cause":[{"type":"parse_exception","reason":"request body is required"}],"type":"parse_exception","reason":"request body is required"},"status":400}
 ~~~

那么问题来了,我们在使用rest命令来创建索引的时候,Elasticsearch内部是如何处理这些参数和校验呢?我们先来看看源码:

~~~java
public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        IndexRequest indexRequest = new IndexRequest(request.param("index"), request.param("type"), request.param("id"));
        indexRequest.routing(request.param("routing"));
        indexRequest.parent(request.param("parent")); // order is important, set it after routing, so it will set the routing
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

整个流程大概如下:

1. 获取请求中的的index,type,id,并构建一个IndexRequest,用于创建索引的请求
2. 获取请求中的routing参数
3. 获取请求中的parent参数
4. 判断请求中是否有timestamp参数
5. 判断请中是否有ttl参数
6. 获取请中的pipeline参数
7. 设置request body,这个步骤同时校验了接收的rest请求中是否包含request body.
8. 获取请求中的timeout参数,如果有则设置,没有使用默认的超时时间
9. 获取请中的refresh参数
10. 设置请求对应的版本
 11. 获取请求中的op_type参数
 12. 获取请求中的wait_for_active_shards参数
 13. 获取请中的op_type参数
 14.

**说明:**
> Elasticsearch在接受到rest请求后会处理以下参数:index,type,id,pipeline,routing,parent,pretty,format,error_trace,human,filter_path,timestamp