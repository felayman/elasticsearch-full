##  Elasticsearch各个Service作用说明

> 看了不少Elasticsearch的源码,代码结构多且复杂,调用链巨长务必,其中各个xxxService更是很多,这里就对这些xxxService做一个总结

### 服务加载

Elasticsearch在启动的过程中会加载许多内置的服务以及模块,源码如下:
~~~java
 modules.add(b -> {
                    b.bind(NodeService.class).toInstance(nodeService);
                    b.bind(NamedXContentRegistry.class).toInstance(xContentRegistry);
                    b.bind(PluginsService.class).toInstance(pluginsService);
                    b.bind(Client.class).toInstance(client);
                    b.bind(NodeClient.class).toInstance(client);
                    b.bind(Environment.class).toInstance(this.environment);
                    b.bind(ThreadPool.class).toInstance(threadPool);
                    b.bind(NodeEnvironment.class).toInstance(nodeEnvironment);
                    b.bind(TribeService.class).toInstance(tribeService);
                    b.bind(ResourceWatcherService.class).toInstance(resourceWatcherService);
                    b.bind(CircuitBreakerService.class).toInstance(circuitBreakerService);
                    b.bind(BigArrays.class).toInstance(bigArrays);
                    b.bind(ScriptService.class).toInstance(scriptModule.getScriptService());
                    b.bind(AnalysisRegistry.class).toInstance(analysisModule.getAnalysisRegistry());
                    b.bind(IngestService.class).toInstance(ingestService);
                    b.bind(NamedWriteableRegistry.class).toInstance(namedWriteableRegistry);
                    b.bind(MetaDataUpgrader.class).toInstance(metaDataUpgrader);
                    b.bind(MetaStateService.class).toInstance(metaStateService);
                    b.bind(IndicesService.class).toInstance(indicesService);
                    b.bind(SearchService.class).toInstance(newSearchService(clusterService, indicesService,
                        threadPool, scriptModule.getScriptService(), bigArrays, searchModule.getFetchPhase()));
                    b.bind(SearchTransportService.class).toInstance(searchTransportService);
                    b.bind(SearchPhaseController.class).toInstance(new SearchPhaseController(settings, bigArrays,
                            scriptModule.getScriptService()));
                    b.bind(Transport.class).toInstance(transport);
                    b.bind(TransportService.class).toInstance(transportService);
                    b.bind(NetworkService.class).toInstance(networkService);
                    b.bind(UpdateHelper.class).toInstance(new UpdateHelper(settings, scriptModule.getScriptService()));
                    b.bind(MetaDataIndexUpgradeService.class).toInstance(new MetaDataIndexUpgradeService(settings, xContentRegistry,
                        indicesModule.getMapperRegistry(), settingsModule.getIndexScopedSettings(), indexMetaDataUpgraders));
                    b.bind(ClusterInfoService.class).toInstance(clusterInfoService);
                    b.bind(Discovery.class).toInstance(discoveryModule.getDiscovery());
                    {
                        RecoverySettings recoverySettings = new RecoverySettings(settings, settingsModule.getClusterSettings());
                        processRecoverySettings(settingsModule.getClusterSettings(), recoverySettings);
                        b.bind(PeerRecoverySourceService.class).toInstance(new PeerRecoverySourceService(settings, transportService,
                                indicesService, recoverySettings, clusterService));
                        b.bind(PeerRecoveryTargetService.class).toInstance(new PeerRecoveryTargetService(settings, threadPool,
                                transportService, recoverySettings, clusterService));
                    }
~~~

可以看到巨长务必的xxxService,如果想深入了解Elasticsearch源码,了解这些Service的作用是必须的,下面就枚举这些Service的作用和功能,重要的Service会着重说一下.

**MapperService**

**NodeService**
**PluginsService**
**TribeService**
**ResourceWatcherService**
**CircuitBreakerService**
**ScriptService**
**IngestService**
**MetaStateService**
**IndicesService**
**SearchService**
**SearchTransportService**
**TransportService**
**NetworkService**
**MetaDataIndexUpgradeService**
**ClusterInfoService**
**PeerRecoverySourceService**
**PeerRecoveryTargetService**
**PeerRecoveryTargetService**






### 参考

- [Reading and Writing documents（读写文档）](http://cwiki.apachecn.org/pages/viewpage.action?pageId=10028500)
- [Reading and Writing documents] (https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-replication.html#docs-replication)
- [elasticsearch源码分析之服务端（四）](http://blog.csdn.net/thomas0yang/article/details/52253165)