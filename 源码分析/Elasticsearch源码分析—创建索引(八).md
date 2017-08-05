>  [创建索引](https://elasticsearch.cn/book/elasticsearch_definitive_guide_2.x/_creating_an_index.html),是Elasticsearch提供的基本功能,下面我看看Elasticsearch源码中是如何完成对一个索引的创建工作

### 文档地址

    https://elasticsearch.cn/book/elasticsearch_definitive_guide_2.x/_creating_an_index.html

 文档中使用通过向Elasticsearch集群中发送创建索引的请求,json如下:
 ~~~json
 PUT /my_index
 {
     "settings": { ... any settings ... },
     "mappings": {
         "type_one": { ... any mappings ... },
         "type_two": { ... any mappings ... },
         ...
     }
 }
 ~~~

OK,我们来正式看看源码中是如何处理这个请求的!

前文中我们已经看到,Elasticsearch使用各种xxxAction来处理这些REST请求,创建索引也不例外,而处理索引创建的Action为RestCreateIndexAction,源码如下:
~~~java
public class RestCreateIndexAction extends BaseRestHandler {
    public RestCreateIndexAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(RestRequest.Method.PUT, "/{index}", this);
    }

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(request.param("index"));
        if (request.hasContent()) {
            createIndexRequest.source(request.content(), request.getXContentType());
        }
        createIndexRequest.updateAllTypes(request.paramAsBoolean("update_all_types", false));
        createIndexRequest.timeout(request.paramAsTime("timeout", createIndexRequest.timeout()));
        createIndexRequest.masterNodeTimeout(request.paramAsTime("master_timeout", createIndexRequest.masterNodeTimeout()));
        createIndexRequest.waitForActiveShards(ActiveShardCount.parseString(request.param("wait_for_active_shards")));
        return channel -> client.admin().indices().create(createIndexRequest, new AcknowledgedRestListener<CreateIndexResponse>(channel) {
            @Override
            public void addCustomFields(XContentBuilder builder, CreateIndexResponse response) throws IOException {
                response.addCustomFields(builder);
            }
        });
    }
}
~~~
很清楚的看到,controller.registerHandler(RestRequest.Method.PUT, "/{index}", this)是将PUT /my_index这个请求注册到对应的controller中,prepareRequest方法则是对请求的具体处理,我们具体看一下这里的代码
整个流程大概如下:

