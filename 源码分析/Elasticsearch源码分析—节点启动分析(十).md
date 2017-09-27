###  节点启动过程

Elasticsearch节点启动的入口在org.elasticsearch.node.Node类中的start()方法中.

~~~java
/**
     * Start the node. If the node is already started, this method is no-op.
     */
    public Node start() throws NodeValidationException {
        if (!lifecycle.moveToStarted()) {
            return this;
        }

        Logger logger = Loggers.getLogger(Node.class, NODE_NAME_SETTING.get(settings));
        logger.info("starting ...");
        // hack around dependency injection problem (for now...)
        injector.getInstance(Discovery.class).setAllocationService(injector.getInstance(AllocationService.class));
        pluginLifecycleComponents.forEach(LifecycleComponent::start);

        injector.getInstance(MappingUpdatedAction.class).setClient(client);
        injector.getInstance(IndicesService.class).start();
        injector.getInstance(IndicesClusterStateService.class).start();
        injector.getInstance(IndicesTTLService.class).start();
        injector.getInstance(SnapshotsService.class).start();
        injector.getInstance(SnapshotShardsService.class).start();
        injector.getInstance(RoutingService.class).start();
        injector.getInstance(SearchService.class).start();
        injector.getInstance(MonitorService.class).start();

        final ClusterService clusterService = injector.getInstance(ClusterService.class);

        final NodeConnectionsService nodeConnectionsService = injector.getInstance(NodeConnectionsService.class);
        nodeConnectionsService.start();
        clusterService.setNodeConnectionsService(nodeConnectionsService);

        // TODO hack around circular dependencies problems
        injector.getInstance(GatewayAllocator.class).setReallocation(clusterService, injector.getInstance(RoutingService.class));

        injector.getInstance(ResourceWatcherService.class).start();
        injector.getInstance(GatewayService.class).start();
        Discovery discovery = injector.getInstance(Discovery.class);
        clusterService.setDiscoverySettings(discovery.getDiscoverySettings());
        clusterService.addInitialStateBlock(discovery.getDiscoverySettings().getNoMasterBlock());
        clusterService.setClusterStatePublisher(discovery::publish);

        // start before the cluster service since it adds/removes initial Cluster state blocks
        final TribeService tribeService = injector.getInstance(TribeService.class);
        tribeService.start();

        // Start the transport service now so the publish address will be added to the local disco node in ClusterService
        TransportService transportService = injector.getInstance(TransportService.class);
        transportService.getTaskManager().setTaskResultsService(injector.getInstance(TaskResultsService.class));
        transportService.start();
        validateNodeBeforeAcceptingRequests(settings, transportService.boundAddress(), pluginsService.filterPlugins(Plugin.class).stream()
            .flatMap(p -> p.getBootstrapChecks().stream()).collect(Collectors.toList()));

        clusterService.addStateApplier(transportService.getTaskManager());
        clusterService.start();
        assert localNodeFactory.getNode() != null;
        assert transportService.getLocalNode().equals(localNodeFactory.getNode())
            : "transportService has a different local node than the factory provided";
        assert clusterService.localNode().equals(localNodeFactory.getNode())
            : "clusterService has a different local node than the factory provided";
        // start after cluster service so the local disco is known
        discovery.start();
        transportService.acceptIncomingRequests();
        discovery.startInitialJoin();
        // tribe nodes don't have a master so we shouldn't register an observer         s
        final TimeValue initialStateTimeout = DiscoverySettings.INITIAL_STATE_TIMEOUT_SETTING.get(settings);
        if (initialStateTimeout.millis() > 0) {
            final ThreadPool thread = injector.getInstance(ThreadPool.class);
            ClusterState clusterState = clusterService.state();
            ClusterStateObserver observer = new ClusterStateObserver(clusterState, clusterService, null, logger, thread.getThreadContext());
            if (clusterState.nodes().getMasterNodeId() == null) {
                logger.debug("waiting to join the cluster. timeout [{}]", initialStateTimeout);
                final CountDownLatch latch = new CountDownLatch(1);
                observer.waitForNextChange(new ClusterStateObserver.Listener() {
                    @Override
                    public void onNewClusterState(ClusterState state) { latch.countDown(); }

                    @Override
                    public void onClusterServiceClose() {
                        latch.countDown();
                    }

                    @Override
                    public void onTimeout(TimeValue timeout) {
                        logger.warn("timed out while waiting for initial discovery state - timeout: {}",
                            initialStateTimeout);
                        latch.countDown();
                    }
                }, state -> state.nodes().getMasterNodeId() != null, initialStateTimeout);

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new ElasticsearchTimeoutException("Interrupted while waiting for initial discovery state");
                }
            }
        }


        if (NetworkModule.HTTP_ENABLED.get(settings)) {
            injector.getInstance(HttpServerTransport.class).start();
        }

        if (WRITE_PORTS_FILE_SETTING.get(settings)) {
            if (NetworkModule.HTTP_ENABLED.get(settings)) {
                HttpServerTransport http = injector.getInstance(HttpServerTransport.class);
                writePortsFile("http", http.boundAddress());
            }
            TransportService transport = injector.getInstance(TransportService.class);
            writePortsFile("transport", transport.boundAddress());
        }

        // start nodes now, after the http server, because it may take some time
        tribeService.startNodes();
        logger.info("started");

        return this;
    }
~~~

代码不多,但是确做了需要事情,尤其是启动了必要的服务,其中最核心的代码当是injector.getInstance(XXX.class).start();我们逐一分析下这些实例启动背后都做了什么

- injector.getInstance(IndicesService.class).start();
- injector.getInstance(IndicesClusterStateService.class).start();
- injector.getInstance(IndicesTTLService.class).start();
- injector.getInstance(SnapshotsService.class).start();
- injector.getInstance(SnapshotShardsService.class).start();
- injector.getInstance(RoutingService.class).start();
- injector.getInstance(SearchService.class).start();
- injector.getInstance(MonitorService.class).start();
- injector.getInstance(NodeConnectionsService.class).start();
- injector.getInstance(ResourceWatcherService.class).start();
- injector.getInstance(GatewayService.class).start();
- injector.getInstance(TribeService.class).start();
-  injector.getInstance(TransportService.class).start();
-  injector.getInstance(ClusterService.class).start();
- injector.getInstance(TransportService.class).start();
- injector.getInstance(Discovery.class).start();
- injector.getInstance(HttpServerTransport.class).start();


