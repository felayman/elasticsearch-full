
> Elasticsearch版本为 5.5.0,下面是主要的核心流程,忽略异常补偿部分

### 问题来源

    Elasticsearch是如何解析集群命令,如_cat/,_cat/health等呢？

### 启动预加载各种RestHandlers

 Elasticsearch在节点启动过程中会执行如下部分代码:
 ~~~java
  if (NetworkModule.HTTP_ENABLED.get(settings)) {
                 logger.debug("initializing HTTP handlers ...");
                 actionModule.initRestHandlers(() -> clusterService.state().nodes());
             }
 ~~~
 如果elasticsearch.yml文件中配置了http.enabled参数(默认为true),则会初始化RestHandlers,即一系列可以处理rest命令的处理器,Elasticsearch添加了大量的内置命令
 处理器来处理各种需要的rest命令,如下:
 ~~~java
 registerHandler.accept(new RestMainAction(settings, restController));
         registerHandler.accept(new RestNodesInfoAction(settings, restController, settingsFilter));
         registerHandler.accept(new RestRemoteClusterInfoAction(settings, restController));
         registerHandler.accept(new RestNodesStatsAction(settings, restController));
         registerHandler.accept(new RestNodesHotThreadsAction(settings, restController));
         registerHandler.accept(new RestClusterAllocationExplainAction(settings, restController));
         registerHandler.accept(new RestClusterStatsAction(settings, restController));
         registerHandler.accept(new RestClusterStateAction(settings, restController, settingsFilter));
         registerHandler.accept(new RestClusterHealthAction(settings, restController));
         registerHandler.accept(new RestClusterUpdateSettingsAction(settings, restController));
         registerHandler.accept(new RestClusterGetSettingsAction(settings, restController, clusterSettings, settingsFilter));
         registerHandler.accept(new RestClusterRerouteAction(settings, restController, settingsFilter));
         registerHandler.accept(new RestClusterSearchShardsAction(settings, restController));
         registerHandler.accept(new RestPendingClusterTasksAction(settings, restController));
         registerHandler.accept(new RestPutRepositoryAction(settings, restController));
         registerHandler.accept(new RestGetRepositoriesAction(settings, restController, settingsFilter));
         registerHandler.accept(new RestDeleteRepositoryAction(settings, restController));
         registerHandler.accept(new RestVerifyRepositoryAction(settings, restController));
         registerHandler.accept(new RestGetSnapshotsAction(settings, restController));
         registerHandler.accept(new RestCreateSnapshotAction(settings, restController));
         registerHandler.accept(new RestRestoreSnapshotAction(settings, restController));
         registerHandler.accept(new RestDeleteSnapshotAction(settings, restController));
         registerHandler.accept(new RestSnapshotsStatusAction(settings, restController));

         registerHandler.accept(new RestGetIndicesAction(settings, restController, indexScopedSettings, settingsFilter));
         registerHandler.accept(new RestIndicesStatsAction(settings, restController));
         registerHandler.accept(new RestIndicesSegmentsAction(settings, restController));
         registerHandler.accept(new RestIndicesShardStoresAction(settings, restController));
         registerHandler.accept(new RestGetAliasesAction(settings, restController));
         registerHandler.accept(new RestIndexDeleteAliasesAction(settings, restController));
         registerHandler.accept(new RestIndexPutAliasAction(settings, restController));
         registerHandler.accept(new RestIndicesAliasesAction(settings, restController));
         registerHandler.accept(new RestCreateIndexAction(settings, restController));
         registerHandler.accept(new RestShrinkIndexAction(settings, restController));
         registerHandler.accept(new RestRolloverIndexAction(settings, restController));
         registerHandler.accept(new RestDeleteIndexAction(settings, restController));
         registerHandler.accept(new RestCloseIndexAction(settings, restController));
         registerHandler.accept(new RestOpenIndexAction(settings, restController));

         registerHandler.accept(new RestUpdateSettingsAction(settings, restController));
         registerHandler.accept(new RestGetSettingsAction(settings, restController, indexScopedSettings, settingsFilter));

         registerHandler.accept(new RestAnalyzeAction(settings, restController));
         registerHandler.accept(new RestGetIndexTemplateAction(settings, restController));
         registerHandler.accept(new RestPutIndexTemplateAction(settings, restController));
         registerHandler.accept(new RestDeleteIndexTemplateAction(settings, restController));

         registerHandler.accept(new RestPutMappingAction(settings, restController));
         registerHandler.accept(new RestGetMappingAction(settings, restController));
         registerHandler.accept(new RestGetFieldMappingAction(settings, restController));

         registerHandler.accept(new RestRefreshAction(settings, restController));
         registerHandler.accept(new RestFlushAction(settings, restController));
         registerHandler.accept(new RestSyncedFlushAction(settings, restController));
         registerHandler.accept(new RestForceMergeAction(settings, restController));
         registerHandler.accept(new RestUpgradeAction(settings, restController));
         registerHandler.accept(new RestClearIndicesCacheAction(settings, restController));

         registerHandler.accept(new RestIndexAction(settings, restController));
         registerHandler.accept(new RestGetAction(settings, restController));
         registerHandler.accept(new RestGetSourceAction(settings, restController));
         registerHandler.accept(new RestMultiGetAction(settings, restController));
         registerHandler.accept(new RestDeleteAction(settings, restController));
         registerHandler.accept(new org.elasticsearch.rest.action.document.RestCountAction(settings, restController));
         registerHandler.accept(new RestTermVectorsAction(settings, restController));
         registerHandler.accept(new RestSuggestAction(settings, restController));
         registerHandler.accept(new RestMultiTermVectorsAction(settings, restController));
         registerHandler.accept(new RestBulkAction(settings, restController));
         registerHandler.accept(new RestUpdateAction(settings, restController));

         registerHandler.accept(new RestSearchAction(settings, restController));
         registerHandler.accept(new RestSearchScrollAction(settings, restController));
         registerHandler.accept(new RestClearScrollAction(settings, restController));
         registerHandler.accept(new RestMultiSearchAction(settings, restController));

         registerHandler.accept(new RestValidateQueryAction(settings, restController));

         registerHandler.accept(new RestExplainAction(settings, restController));

         registerHandler.accept(new RestRecoveryAction(settings, restController));

         // Scripts API
         registerHandler.accept(new RestGetStoredScriptAction(settings, restController));
         registerHandler.accept(new RestPutStoredScriptAction(settings, restController));
         registerHandler.accept(new RestDeleteStoredScriptAction(settings, restController));

         registerHandler.accept(new RestFieldStatsAction(settings, restController));
         registerHandler.accept(new RestFieldCapabilitiesAction(settings, restController));

         // Tasks API
         registerHandler.accept(new RestListTasksAction(settings, restController, nodesInCluster));
         registerHandler.accept(new RestGetTaskAction(settings, restController));
         registerHandler.accept(new RestCancelTasksAction(settings, restController, nodesInCluster));

         // Ingest API
         registerHandler.accept(new RestPutPipelineAction(settings, restController));
         registerHandler.accept(new RestGetPipelineAction(settings, restController));
         registerHandler.accept(new RestDeletePipelineAction(settings, restController));
         registerHandler.accept(new RestSimulatePipelineAction(settings, restController));

         // CAT API
         registerHandler.accept(new RestAllocationAction(settings, restController));
         registerHandler.accept(new RestShardsAction(settings, restController));
         registerHandler.accept(new RestMasterAction(settings, restController));
         registerHandler.accept(new RestNodesAction(settings, restController));
         registerHandler.accept(new RestTasksAction(settings, restController, nodesInCluster));
         registerHandler.accept(new RestIndicesAction(settings, restController, indexNameExpressionResolver));
         registerHandler.accept(new RestSegmentsAction(settings, restController));
         // Fully qualified to prevent interference with rest.action.count.RestCountAction
         registerHandler.accept(new org.elasticsearch.rest.action.cat.RestCountAction(settings, restController));
         // Fully qualified to prevent interference with rest.action.indices.RestRecoveryAction
         registerHandler.accept(new org.elasticsearch.rest.action.cat.RestRecoveryAction(settings, restController));
         registerHandler.accept(new RestHealthAction(settings, restController));
         registerHandler.accept(new org.elasticsearch.rest.action.cat.RestPendingClusterTasksAction(settings, restController));
         registerHandler.accept(new RestAliasAction(settings, restController));
         registerHandler.accept(new RestThreadPoolAction(settings, restController));
         registerHandler.accept(new RestPluginsAction(settings, restController));
         registerHandler.accept(new RestFielddataAction(settings, restController));
         registerHandler.accept(new RestNodeAttrsAction(settings, restController));
         registerHandler.accept(new RestRepositoriesAction(settings, restController));
         registerHandler.accept(new RestSnapshotAction(settings, restController));
         registerHandler.accept(new RestTemplatesAction(settings, restController));
         for (ActionPlugin plugin : actionPlugins) {
             for (RestHandler handler : plugin.getRestHandlers(settings, restController, clusterSettings, indexScopedSettings,
                     settingsFilter, indexNameExpressionResolver, nodesInCluster)) {
                 registerHandler.accept(handler);
             }
         }
         registerHandler.accept(new RestCatAction(settings, restController, catActions));
 ~~~

### 我们以RestCatAction为例,主要处理localhost:9200/_cat命令,我们看看处理方式

 1.预加载RestCatAction

 ~~~java
 registerHandler.accept(new RestCatAction(settings, restController, catActions));
 ~~~

 因为Elasticsearch5.x版本后使用java8来进行开发,出现了大量Lambda表达式,请参考相关java8特性,这里使用用到了Consumer接口来判断输入的对象是否符合某个条件

 2.创建RestCatAction实例

 ~~~java
  private static final String CAT = "=^.^=";
  private static final String CAT_NL = CAT + "\n";
  private final String HELP;

  @Inject
      public RestCatAction(Settings settings, RestController controller, List<AbstractCatAction> catActions) {
          super(settings);
          controller.registerHandler(GET, "/_cat", this);
          StringBuilder sb = new StringBuilder();
          sb.append(CAT_NL);
          for (AbstractCatAction catAction : catActions) {
              catAction.documentation(sb);
          }
          HELP = sb.toString();
      }
 ~~~

 3.将拼接成的rest路径放入到RestController中,即构建出出M-V-C模式,

    ~~~java
    String[] strings = path.split(SEPARATOR);
            if (strings.length == 0) {
                if (rootValue != null) {
                    throw new IllegalArgumentException("Path [/] already has a value [" + rootValue + "]");
                }
                rootValue = value;
                return;
            }
            int index = 0;
            // supports initial delimiter.
            if (strings.length > 0 && strings[0].isEmpty()) {
                index = 1;
            }
            root.insert(strings, index, value)
    ~~~

 这样一来,我们就能以REST风格来访问相对应的rest命令,这里root.insert(strings, index, value)相当于模拟出/_cat路径格式

 4.缓存输出结果,RestCatAction将结果缓存在private final String HELP中,然后在访问的时候,将该结果返回给视图

 ~~~java
  @Override
     public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
         return channel -> channel.sendResponse(new BytesRestResponse(RestStatus.OK, HELP));
     }
 ~~~
结果格式为:
~~~java
=^.^=
/_cat/allocation
/_cat/shards
/_cat/shards/{index}
/_cat/master
/_cat/nodes
/_cat/tasks
/_cat/indices
/_cat/indices/{index}
/_cat/segments
/_cat/segments/{index}
/_cat/count
/_cat/count/{index}
/_cat/recovery
/_cat/recovery/{index}
/_cat/health
/_cat/pending_tasks
/_cat/aliases
/_cat/aliases/{alias}
/_cat/thread_pool
/_cat/thread_pool/{thread_pools}
/_cat/plugins
/_cat/fielddata
/_cat/fielddata/{fields}
/_cat/nodeattrs
/_cat/repositories
/_cat/snapshots/{repository}
/_cat/templates
~~~

由于http://localhost:9200/_cat是获取 _cat下的命令列表,即AbstractCatAction的子类,返回的内容是静态不变的,所以这里只是用了一个final的字符串来缓存返回结果,如果是动态的结果,则处理过程比较麻烦,后面的内容会详细介绍


### 动态结果

    比如http://localhost:9200/_cat/health 命令的返回结果在不同的时间返回结果一般是不同的,这个时候Elasticsearch是如何处理的呢?
    这个时候就会利用集群内部的API来获取当前集群的信息,而不是利用静态字段缓存,而是在每次调用的时候,通过集群内部API来返回结果.

~~~java
@Override
    public RestChannelConsumer doCatRequest(final RestRequest request, final NodeClient client) {
        ClusterHealthRequest clusterHealthRequest = new ClusterHealthRequest();

        return channel -> client.admin().cluster().health(clusterHealthRequest, new RestResponseListener<ClusterHealthResponse>(channel) {
            @Override
            public RestResponse buildResponse(final ClusterHealthResponse health) throws Exception {
                return RestTable.buildResponse(buildTable(health, request), channel);
            }
        });
    }
~~~

然后构建一个类似表单的table来构建返回结果的视图：
~~~java
private Table buildTable(final ClusterHealthResponse health, final RestRequest request) {
        Table t = getTableWithHeader(request);
        t.startRow();
        t.addCell(health.getClusterName());
        t.addCell(health.getStatus().name().toLowerCase(Locale.ROOT));
        t.addCell(health.getNumberOfNodes());
        t.addCell(health.getNumberOfDataNodes());
        t.addCell(health.getActiveShards());
        t.addCell(health.getActivePrimaryShards());
        t.addCell(health.getRelocatingShards());
        t.addCell(health.getInitializingShards());
        t.addCell(health.getUnassignedShards());
        t.addCell(health.getNumberOfPendingTasks());
        t.addCell(health.getTaskMaxWaitingTime().millis() == 0 ? "-" : health.getTaskMaxWaitingTime());
        t.addCell(String.format(Locale.ROOT, "%1.1f%%", health.getActiveShardsPercent()));
        t.endRow();
        return t;
    }
~~~
返回结果如下：
~~~
1499791133 00:38:53 elasticsearch yellow 1 1 6 6 0 0 6 0 - 50.0%
~~~

这样我们就差不多能知道每个返回参数的意思了。