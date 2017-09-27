

> Elasticsearch版本为 5.5.0,下面是主要的核心流程,忽略异常补偿部分

## 整体流程

    以代码流的方式来预览Elasticsearch的启动流程
    1. elasticsearch.main(args, terminal)
    2. mainWithoutErrorHandling(args, terminal);
    3. execute(terminal, options)
    4. execute(terminal, options, createEnv(terminal, settings))
    5. init(daemonize, pidFile, quiet, env)
    6. Bootstrap.init(!daemonize, pidFile, quiet, initialEnv)
    7. INSTANCE = new Bootstrap();
    8. INSTANCE.setup(true, environment);
    9. INSTANCE.start();

上面是Elasticsearch启动过程中的一些比较关键和具有分割点意义的代码,在每个

### 1. 配置默认settings,如path.conf,path.data,path.home,path.logs,这些参数都可以通过VM options中进行设置

    ~~~java
    putSystemPropertyIfSettingIsMissing(settings, "path.conf", "es.path.conf");
    putSystemPropertyIfSettingIsMissing(settings, "path.data", "es.path.data");
    putSystemPropertyIfSettingIsMissing(settings, "path.home", "es.path.home");
    putSystemPropertyIfSettingIsMissing(settings, "path.logs", "es.path.logs");
    ~~~

### 2. 初始化Elasticsearch,下面代码并非在同一个类或包下.

    ~~~java
     1.initLoggerPrefix(); TODO: why? is it just a bad default somewhere? or is it some BS around 'but the client' garbage <-- my guess
     2.INSTANCE = new Bootstrap();
     3.final SecureSettings keystore = loadSecureSettings(initialEnv);
     4.Environment environment = createEnvironment(foreground, pidFile, keystore, initialEnv.settings());
     5.LogConfigurator.configure(environment);
     6.checkForCustomConfFile();
     7.checkConfigExtension(environment.configExtension());
     8.PidFile.create(environment.pidFile(), true);
     9.checkLucene();
     10.node = new Node(environment)  ##核心分割线,上面的步骤都是为该节点创建做准备工作##
     11.nodeEnvironment = new NodeEnvironment(tmpSettings, environment);
     12.final JvmInfo jvmInfo = JvmInfo.jvmInfo();
     13.this.pluginsService = new PluginsService(tmpSettings, environment.modulesFile(), environment.pluginsFile(), classpathPlugins);
     14. for (final ExecutorBuilder<?> builder : threadPool.builders()) {
                        additionalSettings.addAll(builder.getRegisteredSettings());
                    }
     15. client = new NodeClient(settings, threadPool);
     16. ModulesBuilder modules = new ModulesBuilder(); modules.add(clusterModule);modules.add(indicesModule);....modules.add(new RepositoriesModule(this.environment, pluginsService.filterPlugins(RepositoryPlugin.class), xContentRegistry));
     17. final RestController restController = actionModule.getRestController();
     18. modules.add(b -> {
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
                             httpBind.accept(b);
                             pluginComponents.stream().forEach(p -> b.bind((Class) p.getClass()).toInstance(p));
                         }
                     );
                     19. injector = modules.createInjector();
                     20. logger.info("initialized");
                     21. INSTANCE.start();正式启动Elasticsearch节点
                     22. injector.getInstance(MappingUpdatedAction.class).setClient(client);
                                 injector.getInstance(IndicesService.class).start();
                                 injector.getInstance(IndicesClusterStateService.class).start();
                                 injector.getInstance(IndicesTTLService.class).start();
                                 injector.getInstance(SnapshotsService.class).start();
                                 injector.getInstance(SnapshotShardsService.class).start();
                                 injector.getInstance(RoutingService.class).start();
                                 injector.getInstance(SearchService.class).start();
                                 injector.getInstance(MonitorService.class).start();
                      23.  tribeService.startNodes();
    ~~~

 **这个初始化流程如下：**

  1. 初始化日志前缀,至于为什么这么做,Elasticsearch的后来开发者给出了猜测,用了一个TODO对该部分进行了猜测,我们这里忽略
  2. 创建Bootstrap实例,内部注册了一个关闭的钩子并使用非守护线程来保证只有一个Bootstrap实例启动。
  3. 如果注册了安全模块则将相关配置加载进来
  4. 创建Elasticsearch运行的必须环境以及相关配置,如将config,scripts,plugins,modules,logs,lib,bin等配置目录加载到运行环境中
  5. 配置日志相关,创建日志上下文
  6. 检查自定义配置文件,如es.default.config,elasticsearch.config,es.config等
  7. 检查配置文件的扩展名,如果配置文件后缀为yaml或json, Elasticsearch会友好提示你使用yml进行配置
  8. 创建PID文件
  9. 检查Lucene版本
  10. 用当前环境来创建一个节点,核心分割线,上面的步骤都是为该节点创建做准备工作,后续的流程基本都能在Elasticsearch启动日志中体现.
  11. 创建节点环境,比如节点名称,节点ID,分片信息,存储元,以及分配内存准备给节点使用
  12. 打印出JVM相关信息
  13. 利用PluginsService加载相应的模块和插件,因为我下载的Elasticsearch5.5.0安装目录中没有安装任何其他插件,所以只会加载modules目录下的相关模块,如aggs-matrix-stats,ingest-common,lang-expression,lang-groovy等,具体哪些模块可以去modules目录下查看
   14. 加载一些额外的配置参数
   15. 创建一个节点客户端(核心地方)
   16. 缓存一系列模块,如NodeModule,ClusterModule,IndicesModule,ActionModule,GatewayModule,SettingsModule,RepositioriesModule
   17. 获取RestController,用于处理各种Elasticsearch的rest命令,如_cat,_all,_cat/health,_clusters等rest命令(Elasticsearch称之为action)
   18. 绑定处理各种服务的实例,这里是最核心的地方,也是Elasticsearch能处理各种服务的核心.
   19. 利用Guice将各种模块以及服务(xxxService)注入到Elasticsearch环境中
   20. 初始化工作完成
   21. 正式启动Elasticsearch节点
   22. 利用Guice获取上述注册的各种木块以及服务
   23. 将当前节点加入到一个集群簇中去,并启动当前节点

  从步骤10以后的相关启动日志如下:
  ~~~
    [2017-07-11T22:50:24,010][INFO ][o.e.n.Node               ] [] initializing ...
    [2017-07-11T22:50:24,104][INFO ][o.e.e.NodeEnvironment    ] [iobPZcg] using [1] data paths, mounts [[/ (/dev/disk1)]], net usable_space [133.4gb], net total_space [232.6gb], spins? [unknown], types [hfs]
    [2017-07-11T22:50:24,104][INFO ][o.e.e.NodeEnvironment    ] [iobPZcg] heap size [3.5gb], compressed ordinary object pointers [true]
    [2017-07-11T22:50:24,120][INFO ][o.e.n.Node               ] node name [iobPZcg] derived from node ID [iobPZcgEQBKodmURFuo_Gw]; set [node.name] to override
    [2017-07-11T22:50:24,121][INFO ][o.e.n.Node               ] version[5.5.0-SNAPSHOT], pid[66342], build[Unknown/Unknown], OS[Mac OS X/10.12/x86_64], JVM[Oracle Corporation/Java HotSpot(TM) 64-Bit Server VM/1.8.0_101/25.101-b13]
    [2017-07-11T22:50:24,121][INFO ][o.e.n.Node               ] JVM arguments [-agentlib:jdwp=transport=dt_socket,address=127.0.0.1:50649,suspend=y,server=n, -Des.path.home=/Users/admin/elk/elasticsearch-5.5.0, -Dlog4j2.disable.jmx=true, -javaagent:/Users/admin/Library/Caches/IntelliJIdea2017.1/groovyHotSwap/gragent.jar, -Dfile.encoding=UTF-8]
    [2017-07-11T22:50:24,121][WARN ][o.e.n.Node               ] version [5.5.0-SNAPSHOT] is a pre-release version of Elasticsearch and is not suitable for production
    [2017-07-11T22:50:24,781][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [aggs-matrix-stats]
    [2017-07-11T22:50:24,781][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [ingest-common]
    [2017-07-11T22:50:24,782][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [lang-expression]
    [2017-07-11T22:50:24,782][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [lang-groovy]
    [2017-07-11T22:50:24,782][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [lang-mustache]
    [2017-07-11T22:50:24,782][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [lang-painless]
    [2017-07-11T22:50:24,782][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [parent-join]
    [2017-07-11T22:50:24,782][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [percolator]
    [2017-07-11T22:50:24,782][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [reindex]
    [2017-07-11T22:50:24,782][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [transport-netty3]
    [2017-07-11T22:50:24,782][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [transport-netty4]
    [2017-07-11T22:50:24,783][INFO ][o.e.p.PluginsService     ] [iobPZcg] no plugins loaded
    [2017-07-11T22:50:26,679][INFO ][o.e.d.DiscoveryModule    ] [iobPZcg] using discovery type [zen]
    [2017-07-11T22:50:27,230][INFO ][o.e.n.Node               ] initialized
    [2017-07-11T22:50:27,231][INFO ][o.e.n.Node               ] [iobPZcg] starting ...
    [2017-07-11T22:50:27,278][INFO ][i.n.u.i.PlatformDependent] Your platform does not provide complete low-level API for accessing direct buffers reliably. Unless explicitly requested, heap buffer will always be preferred to avoid potential system instability.
    [2017-07-11T22:50:27,459][INFO ][o.e.t.TransportService   ] [iobPZcg] publish_address {127.0.0.1:9300}, bound_addresses {[fe80::1]:9300}, {[::1]:9300}, {127.0.0.1:9300}
    [2017-07-11T22:50:27,477][WARN ][o.e.b.BootstrapChecks    ] [iobPZcg] initial heap size [268435456] not equal to maximum heap size [4294967296]; this can cause resize pauses and prevents mlockall from locking the entire heap
    [2017-07-11T22:50:30,554][INFO ][o.e.c.s.ClusterService   ] [iobPZcg] new_master {iobPZcg}{iobPZcgEQBKodmURFuo_Gw}{Uxli57L-S--Xi5sQtZJlpg}{127.0.0.1}{127.0.0.1:9300}, reason: zen-disco-elected-as-master ([0] nodes joined)
    [2017-07-11T22:50:30,599][INFO ][o.e.h.n.Netty4HttpServerTransport] [iobPZcg] publish_address {127.0.0.1:9200}, bound_addresses {[fe80::1]:9200}, {[::1]:9200}, {127.0.0.1:9200}
    [2017-07-11T22:50:30,599][INFO ][o.e.n.Node               ] [iobPZcg] started
    [2017-07-11T22:50:30,891][INFO ][o.e.g.GatewayService     ] [iobPZcg] recovered [2] indices into cluster_state
    [2017-07-11T22:50:31,141][INFO ][o.e.c.r.a.AllocationService] [iobPZcg] Cluster health status changed from [RED] to [YELLOW] (reason: [shards started [[twitter][4], [twitter][3]] ...]).
  ~~~

### 3. 整个初始化流程和启动流程的大概情况如上述,其内部详细的流程以及方式,会在后面一一详细分析.

