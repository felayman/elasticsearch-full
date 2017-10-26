###  索引写入模型以及分析

原文总结: [Reading and Writing documents](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-replication.html#docs-replication)

### 源码分析

Elasticsearch构建请求有两种方式:

1. Restful API
2. Java Cliet API (或其他语言)

下面是两种请方式的实例(效果一样)：

**Restful API**

~~~json
POST /blog/blog/1
{
  "title":"我是一篇博客",
  "content":"十九大终于闭幕了"
}
~~~

该restful 请求最终会被RestIndexAction处理,源码如下:

~~~java
public class RestIndexAction extends BaseRestHandler {
    public RestIndexAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(POST, "/{index}/{type}", this); // auto id creation
        controller.registerHandler(PUT, "/{index}/{type}/{id}", this);
        controller.registerHandler(POST, "/{index}/{type}/{id}", this);
        CreateHandler createHandler = new CreateHandler(settings);
        controller.registerHandler(PUT, "/{index}/{type}/{id}/_create", createHandler);
        controller.registerHandler(POST, "/{index}/{type}/{id}/_create", createHandler);
    }

    final class CreateHandler extends BaseRestHandler {
        protected CreateHandler(Settings settings) {
            super(settings);
        }

        @Override
        public RestChannelConsumer prepareRequest(RestRequest request, final NodeClient client) throws IOException {
            request.params().put("op_type", "create");
            return RestIndexAction.this.prepareRequest(request, client);
        }
    }

    @Override
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

}
~~~

最终会到达如下代码处:
~~~java
 public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends
        ActionRequestBuilder<Request, Response, RequestBuilder>> void execute(final Action<Request, Response, RequestBuilder> action,
                                                                              final Request request, ActionListener<Response> listener) {
        final TransportActionNodeProxy<Request, Response> proxy = proxies.get(action);
        nodesService.execute((n, l) -> proxy.execute(n, request, l), listener);
    }
~~~

在prepareRequest()方法中,将请求参数封装成IndexRequest

**Java Cliet API **
~~~java
XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("title", "我是一篇博客")
                .field("content", "十九大终于闭幕了")
                .endObject();
        client.prepareIndex("blog", "blog", "1")
                .setSource(builder)
                .get();
~~~

该请求会到达:

~~~java
 public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends
        ActionRequestBuilder<Request, Response, RequestBuilder>> void execute(final Action<Request, Response, RequestBuilder> action,
                                                                              final Request request, ActionListener<Response> listener) {
        final TransportActionNodeProxy<Request, Response> proxy = proxies.get(action);
        nodesService.execute((n, l) -> proxy.execute(n, request, l), listener);
    }
~~~

可以看到,不管是Restful API 还是Java client API  最终都会统一到一个地方处理.

请求入口:
~~~java
public void execute(final DiscoveryNode node, final Request request, final ActionListener<Response> listener) {
        ActionRequestValidationException validationException = request.validate();
        if (validationException != null) {
            listener.onFailure(validationException);
            return;
        }
        transportService.sendRequest(node, action.name(), request, transportOptions,
            new ActionListenerResponseHandler<>(listener, action::newResponse));
    }
~~~

Elasticsearch处理客户端所有请求的入口都在这里,在调用执行真正的请求之前都会进行一个校验操作,如上面的request.validate(),我们来看看索引写入的时候会进行哪些校验,源码如下:

~~~java
 public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = super.validate();
        if (type == null) {
            validationException = addValidationError("type is missing", validationException);
        }
        if (source == null) {
            validationException = addValidationError("source is missing", validationException);
        }
        if (contentType == null) {
            validationException = addValidationError("content type is missing", validationException);
        }
        final long resolvedVersion = resolveVersionDefaults();
        if (opType() == OpType.CREATE) {
            if (versionType != VersionType.INTERNAL) {
                validationException = addValidationError("create operations only support internal versioning. use index instead", validationException);
                return validationException;
            }

            if (resolvedVersion != Versions.MATCH_DELETED) {
                validationException = addValidationError("create operations do not support explicit versions. use index instead", validationException);
                return validationException;
            }
        }

        if (opType() != OpType.INDEX && id == null) {
            addValidationError("an id is required for a " + opType() + " operation", validationException);
        }

        if (!versionType.validateVersionForWrites(resolvedVersion)) {
            validationException = addValidationError("illegal version value [" + resolvedVersion + "] for version type [" + versionType.name() + "]", validationException);
        }

        if (ttl != null) {
            if (ttl.millis() < 0) {
                validationException = addValidationError("ttl must not be negative", validationException);
            }
        }

        if (id != null && id.getBytes(StandardCharsets.UTF_8).length > 512) {
            validationException = addValidationError("id is too long, must be no longer than 512 bytes but was: " +
                            id.getBytes(StandardCharsets.UTF_8).length, validationException);
        }

        if (id == null && (versionType == VersionType.INTERNAL && resolvedVersion == Versions.MATCH_ANY) == false) {
            validationException = addValidationError("an id must be provided if version type or value are set", validationException);
        }
        if (versionType == VersionType.FORCE) {
            deprecationLogger.deprecated("version type FORCE is deprecated and will be removed in the next major version");
        }

        return validationException;
    }
~~~

校验了如下参数:
- type 不允许为null
- source 不允许为null
- contentType不允许为null
- 校验OpType是否合法
- 校验ttl是否合法
- 校验id长度最大为512字节
- 校验在提供version的前提下,id不允许为null


在通过上述的校验之后,Elasticsearch会将请求通过TransportService发送给指定节点,我们看下发送请求时候的参数列表:

- node 指明将该请求发送到哪个节点
- action.name() 该请求操作的名称
- transportOptions 请求选项,如超时时间(timeout),是否压缩(compress),请类型(RECOVERY,BULK,REG,STATE,PING)
- handler 请求结果的处理器


当请求到达指定节点,节点会将请交给TransportIndexAction进行处理,TransportIndexAction是TransportSingleItemBulkWriteAction的子类,即单个文档写入的抽象类,内部定义了如何处理单个文档,源码如下:
~~~java
 @Override
    protected void doExecute(Task task, final Request request, final ActionListener<Response> listener) {
        bulkAction.execute(task, toSingleItemBulkRequest(request), wrapBulkResponse(listener));
    }
~~~

可以知道,Elasticsearch内部会将单个文档的写入也使用Bulk的方式来处理,即将单个文档的请求封装成Bulk的行为来处理,请求转移到TransportBulkAction类处理,源码如下：
~~~java
 if (bulkRequest.hasIndexRequestsWithPipelines()) {
            if (clusterService.localNode().isIngestNode()) {
                processBulkIndexIngestRequest(task, bulkRequest, listener);
            } else {
                ingestForwarder.forwardIngestRequest(BulkAction.INSTANCE, bulkRequest, listener);
            }
            return;
        }

        final long startTime = relativeTime();
        final AtomicArray<BulkItemResponse> responses = new AtomicArray<>(bulkRequest.requests.size());

        if (needToCheck()) {
            // Attempt to create all the indices that we're going to need during the bulk before we start.
            // Step 1: collect all the indices in the request
            final Set<String> indices = bulkRequest.requests.stream()
                .map(DocWriteRequest::index)
                .collect(Collectors.toSet());
            /* Step 2: filter that to indices that don't exist and we can create. At the same time build a map of indices we can't create
             * that we'll use when we try to run the requests. */
            final Map<String, IndexNotFoundException> indicesThatCannotBeCreated = new HashMap<>();
            Set<String> autoCreateIndices = new HashSet<>();
            ClusterState state = clusterService.state();
            for (String index : indices) {
                boolean shouldAutoCreate;
                try {
                    shouldAutoCreate = shouldAutoCreate(index, state);
                } catch (IndexNotFoundException e) {
                    shouldAutoCreate = false;
                    indicesThatCannotBeCreated.put(index, e);
                }
                if (shouldAutoCreate) {
                    autoCreateIndices.add(index);
                }
            }
            // Step 3: create all the indices that are missing, if there are any missing. start the bulk after all the creates come back.
            if (autoCreateIndices.isEmpty()) {
                executeBulk(task, bulkRequest, startTime, listener, responses, indicesThatCannotBeCreated);
            } else {
                final AtomicInteger counter = new AtomicInteger(autoCreateIndices.size());
                for (String index : autoCreateIndices) {
                    createIndex(index, bulkRequest.timeout(), new ActionListener<CreateIndexResponse>() {
                        @Override
                        public void onResponse(CreateIndexResponse result) {
                            if (counter.decrementAndGet() == 0) {
                                executeBulk(task, bulkRequest, startTime, listener, responses, indicesThatCannotBeCreated);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (!(ExceptionsHelper.unwrapCause(e) instanceof ResourceAlreadyExistsException)) {
                                // fail all requests involving this index, if create didn't work
                                for (int i = 0; i < bulkRequest.requests.size(); i++) {
                                    DocWriteRequest request = bulkRequest.requests.get(i);
                                    if (request != null && setResponseFailureIfIndexMatches(responses, i, request, index, e)) {
                                        bulkRequest.requests.set(i, null);
                                    }
                                }
                            }
                            if (counter.decrementAndGet() == 0) {
                                executeBulk(task, bulkRequest, startTime, ActionListener.wrap(listener::onResponse, inner -> {
                                    inner.addSuppressed(e);
                                    listener.onFailure(inner);
                                }), responses, indicesThatCannotBeCreated);
                            }
                        }
                    });
                }
            }
        } else {
            executeBulk(task, bulkRequest, startTime, listener, responses, emptyMap());
        }
    }
~~~

其大概处理流程如下 :

1. 判断bulk请是否需要加锁
2. 获取所有需要处理的索引名称 对应上面的Step 1
3. 过滤出bulk操作中不存在的索引,对应上面的Step 2
4. 调用shouldAutoCreate(String index, ClusterState state),根据当前集群状态,判断当前索引是否需要自动创建
5. 如果bulk操作的所有索引都不需要创建索引,则直接执行批量请求,否则会先创建索引
6. 创建索引的具体实现交给TransportCreateIndexAction来完成,而TransportCreateIndexAction是TransportMasterNodeAction的子类,在TransportMasterNodeAction中完成了索引创建,这里也说明创建索引操作都会在master节点上完成
7. master节点会针对所有请求会包装成一个Task,随后将该task以及请求,调用masterOperation()方法交给TransportCreateIndexAction来完成创建索引前的工作

最终在TransportCreateIndexAction的masterOperation()方法中,会调用MetaDataCreateIndexService来完成具体的索引创建,源码如下：
~~~java
protected void masterOperation(final CreateIndexRequest request, final ClusterState state, final ActionListener<CreateIndexResponse> listener) {
        String cause = request.cause();
        if (cause.length() == 0) {
            cause = "api";
        }

        final String indexName = indexNameExpressionResolver.resolveDateMathExpression(request.index());
        final CreateIndexClusterStateUpdateRequest updateRequest = new CreateIndexClusterStateUpdateRequest(request, cause, indexName, request.index(), request.updateAllTypes())
                .ackTimeout(request.timeout()).masterNodeTimeout(request.masterNodeTimeout())
                .settings(request.settings()).mappings(request.mappings())
                .aliases(request.aliases()).customs(request.customs())
                .waitForActiveShards(request.waitForActiveShards());

        createIndexService.createIndex(updateRequest, ActionListener.wrap(response ->
            listener.onResponse(new CreateIndexResponse(response.isAcknowledged(), response.isShardsAcked())),
            listener::onFailure));
    }
~~~

其中createIndex()源码如下:
~~~java
 public void createIndex(final CreateIndexClusterStateUpdateRequest request,
                            final ActionListener<CreateIndexClusterStateUpdateResponse> listener) {
        onlyCreateIndex(request, ActionListener.wrap(response -> {
            if (response.isAcknowledged()) {
                activeShardsObserver.waitForActiveShards(request.index(), request.waitForActiveShards(), request.ackTimeout(),
                    shardsAcked -> {
                        if (shardsAcked == false) {
                            logger.debug("[{}] index created, but the operation timed out while waiting for " +
                                             "enough shards to be started.", request.index());
                        }
                        listener.onResponse(new CreateIndexClusterStateUpdateResponse(response.isAcknowledged(), shardsAcked));
                    }, listener::onFailure);
            } else {
                listener.onResponse(new CreateIndexClusterStateUpdateResponse(false, false));
            }
        }, listener::onFailure));
    }
~~~

   其中onlyCreateIndex()源码如下:
   ~~~java
   private void onlyCreateIndex(final CreateIndexClusterStateUpdateRequest request,
                                    final ActionListener<ClusterStateUpdateResponse> listener) {
           Settings.Builder updatedSettingsBuilder = Settings.builder();
           updatedSettingsBuilder.put(request.settings()).normalizePrefix(IndexMetaData.INDEX_SETTING_PREFIX);
           indexScopedSettings.validate(updatedSettingsBuilder);
           request.settings(updatedSettingsBuilder.build());

           clusterService.submitStateUpdateTask("create-index [" + request.index() + "], cause [" + request.cause() + "]",
                   new AckedClusterStateUpdateTask<ClusterStateUpdateResponse>(Priority.URGENT, request,
                       wrapPreservingContext(listener, threadPool.getThreadContext())) {

                       @Override
                       protected ClusterStateUpdateResponse newResponse(boolean acknowledged) {
                           return new ClusterStateUpdateResponse(acknowledged);
                       }

                       @Override
                       public ClusterState execute(ClusterState currentState) throws Exception {
                           Index createdIndex = null;
                           String removalExtraInfo = null;
                           IndexRemovalReason removalReason = IndexRemovalReason.FAILURE;
                           try {
                               validate(request, currentState);

                               for (Alias alias : request.aliases()) {
                                   aliasValidator.validateAlias(alias, request.index(), currentState.metaData());
                               }

                               // we only find a template when its an API call (a new index)
                               // find templates, highest order are better matching
                               List<IndexTemplateMetaData> templates = findTemplates(request, currentState);

                               Map<String, Custom> customs = new HashMap<>();

                               // add the request mapping
                               Map<String, Map<String, Object>> mappings = new HashMap<>();

                               Map<String, AliasMetaData> templatesAliases = new HashMap<>();

                               List<String> templateNames = new ArrayList<>();

                               for (Map.Entry<String, String> entry : request.mappings().entrySet()) {
                                   mappings.put(entry.getKey(), MapperService.parseMapping(xContentRegistry, entry.getValue()));
                               }

                               for (Map.Entry<String, Custom> entry : request.customs().entrySet()) {
                                   customs.put(entry.getKey(), entry.getValue());
                               }

                               // apply templates, merging the mappings into the request mapping if exists
                               for (IndexTemplateMetaData template : templates) {
                                   templateNames.add(template.getName());
                                   for (ObjectObjectCursor<String, CompressedXContent> cursor : template.mappings()) {
                                       String mappingString = cursor.value.string();
                                       if (mappings.containsKey(cursor.key)) {
                                           XContentHelper.mergeDefaults(mappings.get(cursor.key),
                                                   MapperService.parseMapping(xContentRegistry, mappingString));
                                       } else {
                                           mappings.put(cursor.key,
                                               MapperService.parseMapping(xContentRegistry, mappingString));
                                       }
                                   }
                                   // handle custom
                                   for (ObjectObjectCursor<String, Custom> cursor : template.customs()) {
                                       String type = cursor.key;
                                       IndexMetaData.Custom custom = cursor.value;
                                       IndexMetaData.Custom existing = customs.get(type);
                                       if (existing == null) {
                                           customs.put(type, custom);
                                       } else {
                                           IndexMetaData.Custom merged = existing.mergeWith(custom);
                                           customs.put(type, merged);
                                       }
                                   }
                                   //handle aliases
                                   for (ObjectObjectCursor<String, AliasMetaData> cursor : template.aliases()) {
                                       AliasMetaData aliasMetaData = cursor.value;
                                       //if an alias with same name came with the create index request itself,
                                       // ignore this one taken from the index template
                                       if (request.aliases().contains(new Alias(aliasMetaData.alias()))) {
                                           continue;
                                       }
                                       //if an alias with same name was already processed, ignore this one
                                       if (templatesAliases.containsKey(cursor.key)) {
                                           continue;
                                       }

                                       //Allow templatesAliases to be templated by replacing a token with the name of the index that we are applying it to
                                       if (aliasMetaData.alias().contains("{index}")) {
                                           String templatedAlias = aliasMetaData.alias().replace("{index}", request.index());
                                           aliasMetaData = AliasMetaData.newAliasMetaData(aliasMetaData, templatedAlias);
                                       }

                                       aliasValidator.validateAliasMetaData(aliasMetaData, request.index(), currentState.metaData());
                                       templatesAliases.put(aliasMetaData.alias(), aliasMetaData);
                                   }
                               }
                               Settings.Builder indexSettingsBuilder = Settings.builder();
                               // apply templates, here, in reverse order, since first ones are better matching
                               for (int i = templates.size() - 1; i >= 0; i--) {
                                   indexSettingsBuilder.put(templates.get(i).settings());
                               }
                               // now, put the request settings, so they override templates
                               indexSettingsBuilder.put(request.settings());
                               if (indexSettingsBuilder.get(SETTING_NUMBER_OF_SHARDS) == null) {
                                   indexSettingsBuilder.put(SETTING_NUMBER_OF_SHARDS, settings.getAsInt(SETTING_NUMBER_OF_SHARDS, 5));
                               }
                               if (indexSettingsBuilder.get(SETTING_NUMBER_OF_REPLICAS) == null) {
                                   indexSettingsBuilder.put(SETTING_NUMBER_OF_REPLICAS, settings.getAsInt(SETTING_NUMBER_OF_REPLICAS, 1));
                               }
                               if (settings.get(SETTING_AUTO_EXPAND_REPLICAS) != null && indexSettingsBuilder.get(SETTING_AUTO_EXPAND_REPLICAS) == null) {
                                   indexSettingsBuilder.put(SETTING_AUTO_EXPAND_REPLICAS, settings.get(SETTING_AUTO_EXPAND_REPLICAS));
                               }

                               if (indexSettingsBuilder.get(SETTING_VERSION_CREATED) == null) {
                                   DiscoveryNodes nodes = currentState.nodes();
                                   final Version createdVersion = Version.min(Version.CURRENT, nodes.getSmallestNonClientNodeVersion());
                                   indexSettingsBuilder.put(SETTING_VERSION_CREATED, createdVersion);
                               }

                               if (indexSettingsBuilder.get(SETTING_CREATION_DATE) == null) {
                                   indexSettingsBuilder.put(SETTING_CREATION_DATE, new DateTime(DateTimeZone.UTC).getMillis());
                               }
                               indexSettingsBuilder.put(IndexMetaData.SETTING_INDEX_PROVIDED_NAME, request.getProvidedName());
                               indexSettingsBuilder.put(SETTING_INDEX_UUID, UUIDs.randomBase64UUID());
                               final Index shrinkFromIndex = request.shrinkFrom();
                               int routingNumShards = IndexMetaData.INDEX_NUMBER_OF_SHARDS_SETTING.get(indexSettingsBuilder.build());;
                               if (shrinkFromIndex != null) {
                                   prepareShrinkIndexSettings(currentState, mappings.keySet(), indexSettingsBuilder, shrinkFromIndex,
                                       request.index());
                                   IndexMetaData sourceMetaData = currentState.metaData().getIndexSafe(shrinkFromIndex);
                                   routingNumShards = sourceMetaData.getRoutingNumShards();
                               }

                               Settings actualIndexSettings = indexSettingsBuilder.build();
                               IndexMetaData.Builder tmpImdBuilder = IndexMetaData.builder(request.index())
                                   .setRoutingNumShards(routingNumShards);
                               // Set up everything, now locally create the index to see that things are ok, and apply
                               final IndexMetaData tmpImd = tmpImdBuilder.settings(actualIndexSettings).build();
                               ActiveShardCount waitForActiveShards = request.waitForActiveShards();
                               if (waitForActiveShards == ActiveShardCount.DEFAULT) {
                                   waitForActiveShards = tmpImd.getWaitForActiveShards();
                               }
                               if (waitForActiveShards.validate(tmpImd.getNumberOfReplicas()) == false) {
                                   throw new IllegalArgumentException("invalid wait_for_active_shards[" + request.waitForActiveShards() +
                                                                      "]: cannot be greater than number of shard copies [" +
                                                                      (tmpImd.getNumberOfReplicas() + 1) + "]");
                               }
                               // create the index here (on the master) to validate it can be created, as well as adding the mapping
                               final IndexService indexService = indicesService.createIndex(tmpImd, Collections.emptyList());
                               createdIndex = indexService.index();
                               // now add the mappings
                               MapperService mapperService = indexService.mapperService();
                               try {
                                   mapperService.merge(mappings, MergeReason.MAPPING_UPDATE, request.updateAllTypes());
                               } catch (Exception e) {
                                   removalExtraInfo = "failed on parsing default mapping/mappings on index creation";
                                   throw e;
                               }

                               // the context is only used for validation so it's fine to pass fake values for the shard id and the current
                               // timestamp
                               final QueryShardContext queryShardContext = indexService.newQueryShardContext(0, null, () -> 0L);
                               for (Alias alias : request.aliases()) {
                                   if (Strings.hasLength(alias.filter())) {
                                       aliasValidator.validateAliasFilter(alias.name(), alias.filter(), queryShardContext, xContentRegistry);
                                   }
                               }
                               for (AliasMetaData aliasMetaData : templatesAliases.values()) {
                                   if (aliasMetaData.filter() != null) {
                                       aliasValidator.validateAliasFilter(aliasMetaData.alias(), aliasMetaData.filter().uncompressed(),
                                               queryShardContext, xContentRegistry);
                                   }
                               }

                               // now, update the mappings with the actual source
                               Map<String, MappingMetaData> mappingsMetaData = new HashMap<>();
                               for (DocumentMapper mapper : mapperService.docMappers(true)) {
                                   MappingMetaData mappingMd = new MappingMetaData(mapper);
                                   mappingsMetaData.put(mapper.type(), mappingMd);
                               }

                               final IndexMetaData.Builder indexMetaDataBuilder = IndexMetaData.builder(request.index())
                                   .settings(actualIndexSettings)
                                   .setRoutingNumShards(routingNumShards);
                               for (MappingMetaData mappingMd : mappingsMetaData.values()) {
                                   indexMetaDataBuilder.putMapping(mappingMd);
                               }

                               for (AliasMetaData aliasMetaData : templatesAliases.values()) {
                                   indexMetaDataBuilder.putAlias(aliasMetaData);
                               }
                               for (Alias alias : request.aliases()) {
                                   AliasMetaData aliasMetaData = AliasMetaData.builder(alias.name()).filter(alias.filter())
                                           .indexRouting(alias.indexRouting()).searchRouting(alias.searchRouting()).build();
                                   indexMetaDataBuilder.putAlias(aliasMetaData);
                               }

                               for (Map.Entry<String, Custom> customEntry : customs.entrySet()) {
                                   indexMetaDataBuilder.putCustom(customEntry.getKey(), customEntry.getValue());
                               }

                               indexMetaDataBuilder.state(request.state());

                               final IndexMetaData indexMetaData;
                               try {
                                   indexMetaData = indexMetaDataBuilder.build();
                               } catch (Exception e) {
                                   removalExtraInfo = "failed to build index metadata";
                                   throw e;
                               }

                               indexService.getIndexEventListener().beforeIndexAddedToCluster(indexMetaData.getIndex(),
                                       indexMetaData.getSettings());

                               MetaData newMetaData = MetaData.builder(currentState.metaData())
                                       .put(indexMetaData, false)
                                       .build();

                               String maybeShadowIndicator = IndexMetaData.isIndexUsingShadowReplicas(indexMetaData.getSettings()) ? "s" : "";
                               logger.info("[{}] creating index, cause [{}], templates {}, shards [{}]/[{}{}], mappings {}",
                                       request.index(), request.cause(), templateNames, indexMetaData.getNumberOfShards(),
                                       indexMetaData.getNumberOfReplicas(), maybeShadowIndicator, mappings.keySet());

                               ClusterBlocks.Builder blocks = ClusterBlocks.builder().blocks(currentState.blocks());
                               if (!request.blocks().isEmpty()) {
                                   for (ClusterBlock block : request.blocks()) {
                                       blocks.addIndexBlock(request.index(), block);
                                   }
                               }
                               blocks.updateBlocks(indexMetaData);

                               ClusterState updatedState = ClusterState.builder(currentState).blocks(blocks).metaData(newMetaData).build();

                               if (request.state() == State.OPEN) {
                                   RoutingTable.Builder routingTableBuilder = RoutingTable.builder(updatedState.routingTable())
                                           .addAsNew(updatedState.metaData().index(request.index()));
                                   updatedState = allocationService.reroute(
                                           ClusterState.builder(updatedState).routingTable(routingTableBuilder.build()).build(),
                                           "index [" + request.index() + "] created");
                               }
                               removalExtraInfo = "cleaning up after validating index on master";
                               removalReason = IndexRemovalReason.NO_LONGER_ASSIGNED;
                               return updatedState;
                           } finally {
                               if (createdIndex != null) {
                                   // Index was already partially created - need to clean up
                                   indicesService.removeIndex(createdIndex, removalReason, removalExtraInfo);
                               }
                           }
                       }

                       @Override
                       public void onFailure(String source, Exception e) {
                           if (e instanceof ResourceAlreadyExistsException) {
                               logger.trace((Supplier<?>) () -> new ParameterizedMessage("[{}] failed to create", request.index()), e);
                           } else {
                               logger.debug((Supplier<?>) () -> new ParameterizedMessage("[{}] failed to create", request.index()), e);
                           }
                           super.onFailure(source, e);
                       }
                   });
       }
   ~~~

   其中内部通过indexService.index()来创建索引,源码如下:



   这些方法调整的大概流程如下:

   1. 校验请参数是否合法
   2. 查看集群是否已经有该名称的索引别名
   3. 查找集群是否配置过该索引的模板(template)
   4. 开始检查集群配置参数,没有就设置默认值,比如:number_of_shards,number_of_replicas,auto_expand_replicas,index.version.created,index.creation_date,index.provided_name,
   5.

### 参考

- [Reading and Writing documents（读写文档）](http://cwiki.apachecn.org/pages/viewpage.action?pageId=10028500)
- [Reading and Writing documents] (https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-replication.html#docs-replication)
- [elasticsearch源码分析之服务端（四）](http://blog.csdn.net/thomas0yang/article/details/52253165)