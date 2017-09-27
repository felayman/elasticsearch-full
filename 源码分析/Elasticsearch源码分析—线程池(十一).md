###  线程池

> 每个节点都有一些线程池来优化线程内存的消耗，按节点来配置管理。有些线程池还拥有与之关联的队列配置，用来允许挂住一些未处理的请求，而不是丢弃它。

Elasticsearch对线程池的处理的源码在org.elasticsearch.node.Node中,核心代码为:

~~~java
final ThreadPool threadPool = new ThreadPool(settings, executorBuilders.toArray(new ExecutorBuilder[0]));
~~~

其具体实现为:

~~~java
    super(settings);

    assert Node.NODE_NAME_SETTING.exists(settings);

    final Map<String, ExecutorBuilder> builders = new HashMap<>();
    final int availableProcessors = EsExecutors.boundedNumberOfProcessors(settings);
    final int halfProcMaxAt5 = halfNumberOfProcessorsMaxFive(availableProcessors);
    final int halfProcMaxAt10 = halfNumberOfProcessorsMaxTen(availableProcessors);
    final int genericThreadPoolMax = boundedBy(4 * availableProcessors, 128, 512);
    builders.put(Names.GENERIC, new ScalingExecutorBuilder(Names.GENERIC, 4, genericThreadPoolMax, TimeValue.timeValueSeconds(30)));
    builders.put(Names.INDEX, new FixedExecutorBuilder(settings, Names.INDEX, availableProcessors, 200));
    builders.put(Names.BULK, new FixedExecutorBuilder(settings, Names.BULK, availableProcessors, 200)); // now that we reuse bulk for index/delete ops
    builders.put(Names.GET, new FixedExecutorBuilder(settings, Names.GET, availableProcessors, 1000));
    builders.put(Names.SEARCH, new FixedExecutorBuilder(settings, Names.SEARCH, searchThreadPoolSize(availableProcessors), 1000));
    builders.put(Names.MANAGEMENT, new ScalingExecutorBuilder(Names.MANAGEMENT, 1, 5, TimeValue.timeValueMinutes(5)));
    // no queue as this means clients will need to handle rejections on listener queue even if the operation succeeded
    // the assumption here is that the listeners should be very lightweight on the listeners side
    builders.put(Names.LISTENER, new FixedExecutorBuilder(settings, Names.LISTENER, halfProcMaxAt10, -1));
    builders.put(Names.FLUSH, new ScalingExecutorBuilder(Names.FLUSH, 1, halfProcMaxAt5, TimeValue.timeValueMinutes(5)));
    builders.put(Names.REFRESH, new ScalingExecutorBuilder(Names.REFRESH, 1, halfProcMaxAt10, TimeValue.timeValueMinutes(5)));
    builders.put(Names.WARMER, new ScalingExecutorBuilder(Names.WARMER, 1, halfProcMaxAt5, TimeValue.timeValueMinutes(5)));
    builders.put(Names.SNAPSHOT, new ScalingExecutorBuilder(Names.SNAPSHOT, 1, halfProcMaxAt5, TimeValue.timeValueMinutes(5)));
    builders.put(Names.FETCH_SHARD_STARTED, new ScalingExecutorBuilder(Names.FETCH_SHARD_STARTED, 1, 2 * availableProcessors, TimeValue.timeValueMinutes(5)));
    builders.put(Names.FORCE_MERGE, new FixedExecutorBuilder(settings, Names.FORCE_MERGE, 1, -1));
    builders.put(Names.FETCH_SHARD_STORE, new ScalingExecutorBuilder(Names.FETCH_SHARD_STORE, 1, 2 * availableProcessors, TimeValue.timeValueMinutes(5)));
    for (final ExecutorBuilder<?> builder : customBuilders) {
        if (builders.containsKey(builder.name())) {
            throw new IllegalArgumentException("builder with name [" + builder.name() + "] already exists");
        }
        builders.put(builder.name(), builder);
    }
    this.builders = Collections.unmodifiableMap(builders);

    threadContext = new ThreadContext(settings);

    final Map<String, ExecutorHolder> executors = new HashMap<>();
    for (@SuppressWarnings("unchecked") final Map.Entry<String, ExecutorBuilder> entry : builders.entrySet()) {
        final ExecutorBuilder.ExecutorSettings executorSettings = entry.getValue().getSettings(settings);
        final ExecutorHolder executorHolder = entry.getValue().build(executorSettings, threadContext);
        if (executors.containsKey(executorHolder.info.getName())) {
            throw new IllegalStateException("duplicate executors with name [" + executorHolder.info.getName() + "] registered");
        }
        logger.debug("created thread pool: {}", entry.getValue().formatInfo(executorHolder.info));
        executors.put(entry.getKey(), executorHolder);
    }

    executors.put(Names.SAME, new ExecutorHolder(DIRECT_EXECUTOR, new Info(Names.SAME, ThreadPoolType.DIRECT)));
    this.executors = unmodifiableMap(executors);

    this.scheduler = new ScheduledThreadPoolExecutor(1, EsExecutors.daemonThreadFactory(settings, "scheduler"), new EsAbortPolicy());
    this.scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    this.scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
    this.scheduler.setRemoveOnCancelPolicy(true);

    TimeValue estimatedTimeInterval = ESTIMATED_TIME_INTERVAL_SETTING.get(settings);
    this.cachedTimeThread = new CachedTimeThread(EsExecutors.threadName(settings, "[timer]"), estimatedTimeInterval.millis());
    this.cachedTimeThread.start();
~~~

从源码中可以看到,Elasticsearch的线程池基本有许多不同名称的线程池,这些线程池的命名都缓存在一个常量静态内部类ThreadPool.Names中,源码如下:

~~~java
public static class Names {
        public static final String SAME = "same";
        public static final String GENERIC = "generic";
        public static final String LISTENER = "listener";
        public static final String GET = "get";
        public static final String INDEX = "index";
        public static final String BULK = "bulk";
        public static final String SEARCH = "search";
        public static final String MANAGEMENT = "management";
        public static final String FLUSH = "flush";
        public static final String REFRESH = "refresh";
        public static final String WARMER = "warmer";
        public static final String SNAPSHOT = "snapshot";
        public static final String FORCE_MERGE = "force_merge";
        public static final String FETCH_SHARD_STARTED = "fetch_shard_started";
        public static final String FETCH_SHARD_STORE = "fetch_shard_store";
    }
~~~

而且Elasticsearch还将这些线程池分成了三个类型,分别为direct,fixed,scaling,这些类别也缓存在改常量类中,源码为:

~~~java
 public enum ThreadPoolType {
        DIRECT("direct"),
        FIXED("fixed"),
        SCALING("scaling");
        private final String type;
        //省略getter/setter
~~~

默认地,Elasticsearch将上述的各个线程池采用不同的类型,源码如下:

~~~java
static {
        HashMap<String, ThreadPoolType> map = new HashMap<>();
        map.put(Names.SAME, ThreadPoolType.DIRECT);
        map.put(Names.GENERIC, ThreadPoolType.SCALING);
        map.put(Names.LISTENER, ThreadPoolType.FIXED);
        map.put(Names.GET, ThreadPoolType.FIXED);
        map.put(Names.INDEX, ThreadPoolType.FIXED);
        map.put(Names.BULK, ThreadPoolType.FIXED);
        map.put(Names.SEARCH, ThreadPoolType.FIXED);
        map.put(Names.MANAGEMENT, ThreadPoolType.SCALING);
        map.put(Names.FLUSH, ThreadPoolType.SCALING);
        map.put(Names.REFRESH, ThreadPoolType.SCALING);
        map.put(Names.WARMER, ThreadPoolType.SCALING);
        map.put(Names.SNAPSHOT, ThreadPoolType.SCALING);
        map.put(Names.FORCE_MERGE, ThreadPoolType.FIXED);
        map.put(Names.FETCH_SHARD_STARTED, ThreadPoolType.SCALING);
        map.put(Names.FETCH_SHARD_STORE, ThreadPoolType.SCALING);
        THREAD_POOL_TYPES = Collections.unmodifiableMap(map);
    }
~~~

### 各线程池功能说明

- GENERIC

      用于通用的操作（例如：后台节点发现），线程池类型为 scaling

- INDEX

      用于index/delete操作，线程池类型为 fixed， 大小的为处理器数量，队列大小为200，最大线程数为 1 + 处理器数量

- BULK

      用于bulk操作，线程池类型为 fixed， 大小的为处理器数量，队列大小为200，该池的最大线程数为 1 + 处理器数量

- GET

       用于get操作。线程池类型为 fixed，大小的为处理器数量，队列大小为1000。

- SEARCH

      用于count/search/suggest操作。线程池类型为 fixed， 大小的为 int((处理器数量 3) / 2) +1，队列大小为1000

- MANAGEMENT

        官方暂未说明(新版本才有)

- LISTENER

        主要用于Java客户端线程监听器被设置为true时执行动作。线程池类型为 scaling，最大线程数为min(10, (处理器数量)/2)

- FLUSH

        用于flush操作。线程池类型为 scaling，线程空闲保持存活时间为5分钟，最大线程数为min(10, (处理器数量)/2)

- REFRESH

        用于refresh操作。线程池类型为 scaling，线程空闲保持存活时间为5分钟，最大线程数为min(10, (处理器数量)/2)

- WARMER

        用于segment warm-up操作。线程池类型为 scaling，线程保持存活时间为5分钟，最大线程数为min(5, (处理器数量)/2)

- SNAPSHOT

        用于snaphost/restore操作。线程池类型为 scaling，线程保持存活时间为5分钟，最大线程数为min(5, (处理器数量)/2)

- FETCH_SHARD_STARTED

        官方暂未说明(新版本才有)

- FORCE_MERGE

        官方暂未说明(新版本才有)

- FETCH_SHARD_STORE

        官方暂未说明(新版本才有)

- SAME

        官方暂未说明(新版本才有)

### 各线程类型说明

- direct

      此类线程是一种不支持关闭的线程,就意味着一旦使用,则会一直存活下去.

- fixed

      此类线程池拥有固定数量的线程来处理请求，在没有空闲线程时请求将被挂在队列中（可选配）

- scaling

      此类线程池拥有的线程数量是动态的。这个数字介于core和max参数的配置之间变化

这些线程池的创建如果在调试源码的时候日志级别更改为DEBUG,也是可以看出的,如下:

~~~
[2017-09-27T14:31:47,558][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [force_merge], size [1], queue size [unbounded]
[2017-09-27T14:31:47,560][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [fetch_shard_started], core [1], max [16], keep alive [5m]
[2017-09-27T14:31:47,561][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [listener], size [4], queue size [unbounded]
[2017-09-27T14:31:47,565][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [index], size [8], queue size [200]
[2017-09-27T14:31:47,565][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [refresh], core [1], max [4], keep alive [5m]
[2017-09-27T14:31:47,566][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [generic], core [4], max [128], keep alive [30s]
[2017-09-27T14:31:47,566][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [warmer], core [1], max [4], keep alive [5m]
[2017-09-27T14:31:47,566][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [search], size [13], queue size [1k]
[2017-09-27T14:31:47,567][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [flush], core [1], max [4], keep alive [5m]
[2017-09-27T14:31:47,567][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [fetch_shard_store], core [1], max [16], keep alive [5m]
[2017-09-27T14:31:47,567][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [management], core [1], max [5], keep alive [5m]
[2017-09-27T14:31:47,568][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [get], size [8], queue size [1k]
[2017-09-27T14:31:47,568][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [bulk], size [8], queue size [200]
[2017-09-27T14:31:47,568][DEBUG][o.e.t.ThreadPool         ] [x2LMQHg] created thread pool: name [snapshot], core [1], max [4], keep alive [5m]
~~~



### 参考

- [Thread Pool](https://www.elastic.co/guide/en/elasticsearch/reference/6.x/modules-threadpool.html)