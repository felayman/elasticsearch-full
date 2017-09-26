
> Elasticsearch版本为 5.5.0

### 1. 源码下载

    git clone https://github.com/elastic/elasticsearch

### 2. 分支切换

    git checkout v5.5.0

### 3 运行gradle idea,gradle build -x test

>  如果运行gradle idea会报如下错误:请修改elasticsearch源文件中的BuildPlugin.groory文件中的findJavaHome()方法的第一行代码,源码为 String javaHome = System.getenv('JAVA_HOME'),修改为String javaHome = "你的JAVA_HOME地址,输入echo $JAVA_HOME($)"
~~~
What went wrong:
A problem occurred evaluating project ':benchmarks'.
> Failed to apply plugin [id 'elasticsearch.build']
   > JAVA_HOME must be set to build Elasticsearch
~~~

    这里我使用的是idea工具来调试源码,因此先不要用idea导入该项目,而是进入到下载的elasticsearch目录下,执行gradle idea,等待漫长的依赖下载,等待下载完成后
    执行 gradle build -x test 对源码进行编译,等待编译完成.

###  4. 注释掉jar hell相关代码

    全局搜索JarHell.checkJarHell,以及checkJarHell,注释掉相应的代码,否则运行会报错

###  5. 配置Edit Configuation

    mainClass为  org.elasticsearch.bootstrap.Elasticsearch
    VM options为     -Des.path.home=/Users/admin/elk/elasticsearch-5.5.0 -Dlog4j2.disable.jmx=true

### 6. 运行org.elasticsearch.bootstrap.Elasticsearch中的main方法

    ~~~java
    [2017-07-11T22:00:08,396][INFO ][o.e.n.Node               ] [] initializing ...
    [2017-07-11T22:00:08,487][INFO ][o.e.e.NodeEnvironment    ] [iobPZcg] using [1] data paths, mounts [[/ (/dev/disk1)]], net usable_space [133.3gb], net total_space [232.6gb], spins? [unknown], types [hfs]
    [2017-07-11T22:00:08,487][INFO ][o.e.e.NodeEnvironment    ] [iobPZcg] heap size [3.5gb], compressed ordinary object pointers [true]
    [2017-07-11T22:00:08,503][INFO ][o.e.n.Node               ] node name [iobPZcg] derived from node ID [iobPZcgEQBKodmURFuo_Gw]; set [node.name] to override
    [2017-07-11T22:00:08,503][INFO ][o.e.n.Node               ] version[5.5.0-SNAPSHOT], pid[65630], build[Unknown/Unknown], OS[Mac OS X/10.12/x86_64], JVM[Oracle Corporation/Java HotSpot(TM) 64-Bit Server VM/1.8.0_101/25.101-b13]
    [2017-07-11T22:00:08,503][INFO ][o.e.n.Node               ] JVM arguments [-Des.path.home=/Users/admin/elk/elasticsearch-5.5.0, -Dlog4j2.disable.jmx=true, -javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=49964:/Applications/IntelliJ IDEA.app/Contents/bin, -Dfile.encoding=UTF-8]
    [2017-07-11T22:00:08,504][WARN ][o.e.n.Node               ] version [5.5.0-SNAPSHOT] is a pre-release version of Elasticsearch and is not suitable for production
    [2017-07-11T22:00:09,201][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [aggs-matrix-stats]
    [2017-07-11T22:00:09,201][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [ingest-common]
    [2017-07-11T22:00:09,201][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [lang-expression]
    [2017-07-11T22:00:09,201][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [lang-groovy]
    [2017-07-11T22:00:09,201][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [lang-mustache]
    [2017-07-11T22:00:09,201][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [lang-painless]
    [2017-07-11T22:00:09,201][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [parent-join]
    [2017-07-11T22:00:09,202][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [percolator]
    [2017-07-11T22:00:09,202][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [reindex]
    [2017-07-11T22:00:09,202][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [transport-netty3]
    [2017-07-11T22:00:09,202][INFO ][o.e.p.PluginsService     ] [iobPZcg] loaded module [transport-netty4]
    [2017-07-11T22:00:09,202][INFO ][o.e.p.PluginsService     ] [iobPZcg] no plugins loaded
    [2017-07-11T22:00:11,386][INFO ][o.e.d.DiscoveryModule    ] [iobPZcg] using discovery type [zen]
    [2017-07-11T22:00:12,068][INFO ][o.e.n.Node               ] initialized
    [2017-07-11T22:00:12,068][INFO ][o.e.n.Node               ] [iobPZcg] starting ...
    [2017-07-11T22:00:12,120][INFO ][i.n.u.i.PlatformDependent] Your platform does not provide complete low-level API for accessing direct buffers reliably. Unless explicitly requested, heap buffer will always be preferred to avoid potential system instability.
    [2017-07-11T22:00:12,291][INFO ][o.e.t.TransportService   ] [iobPZcg] publish_address {127.0.0.1:9300}, bound_addresses {[fe80::1]:9300}, {[::1]:9300}, {127.0.0.1:9300}
    [2017-07-11T22:00:12,308][WARN ][o.e.b.BootstrapChecks    ] [iobPZcg] initial heap size [268435456] not equal to maximum heap size [4294967296]; this can cause resize pauses and prevents mlockall from locking the entire heap
    [2017-07-11T22:00:15,383][INFO ][o.e.c.s.ClusterService   ] [iobPZcg] new_master {iobPZcg}{iobPZcgEQBKodmURFuo_Gw}{J8yIAMTeS3uOKeW35gG0kw}{127.0.0.1}{127.0.0.1:9300}, reason: zen-disco-elected-as-master ([0] nodes joined)
    [2017-07-11T22:00:15,408][INFO ][o.e.h.n.Netty4HttpServerTransport] [iobPZcg] publish_address {127.0.0.1:9200}, bound_addresses {[fe80::1]:9200}, {[::1]:9200}, {127.0.0.1:9200}
    [2017-07-11T22:00:15,409][INFO ][o.e.n.Node               ] [iobPZcg] started
    [2017-07-11T22:00:15,639][INFO ][o.e.g.GatewayService     ] [iobPZcg] recovered [2] indices into cluster_state
    [2017-07-11T22:00:15,850][INFO ][o.e.c.r.a.AllocationService] [iobPZcg] Cluster health status changed from [RED] to [YELLOW] (reason: [shards started [[twitter][3]] ...]).
    ~~~

### 可能遇到的异常

- ERROR: the system property [es.path.conf] must be set

    原因是没有指定es.path.conf,设置-Des.path.conf=你调试源码版本对应的conf目录

-  Exception in thread "main" java.lang.IllegalStateException: path.home is not configured

     原因是因为没有为elasticsearch配置path.home参数,可以在Edit Configuation中设置虚拟机参数：-Des.path.home=你下载的对应的elasticsearch的安装目录,这么做的原因
     是elasticsearch在启动中会加载一些默认配置以及插件,我们直接加载elasticsearch安装目录下的配置和插件即可,后面会在源码中体现

-  2017-06-23 14:00:44,760 main ERROR Could not register mbeans java.security.AccessControlException: access denied ("javax.management.MBeanTrustPermission" "register")

    原因是因为elasticsearch在启动过程中使用到了jmx,我们这里禁止使用即可,配置也是在Edit Configuation中设置虚拟机参数 -Dlog4j2.disable.jmx=true

- org.elasticsearch.bootstrap.StartupException: org.elasticsearch.bootstrap.BootstrapException: java.lang.IllegalStateException: jar hell!或Classname: org.elasticsearch.search.aggregations.matrix.MatrixAggregationPlugin due to jar hell

    原因是因为elasticsearch中大量存在一个类或一个资源文件存在多个jar中,我们注释掉相应代码即可,主要是PluginsService中374行的JarHell.checkJarHell(union)以及
    Bootstrap中220行的JarHell.checkJarHell(),最简单的方式就是将JarHell.checkJarHell()中的方法体注释掉

- org.elasticsearch.bootstrap.StartupException: java.lang.IllegalArgumentException: plugin [aggs-matrix-stats] is incompatible with version [7.0.0-alpha1]; was designed for version [5.6.1]

    原因是一般情况下我们调试的源码非某个发布版本,有些配置项并未发布,我们的配置与当前代码的版本匹配不上,这个时候我们需要将调试的源码设置成某个发布版本,一般来说,Elasticsearch每发布
    一个稳定版本,都会有一个对应的tag,我们进入到ES源码目录下执行git tag, 我这里调试的版本为v5.6.1,所以执行git checkout v5.6.1,切换到v5.6.1tag.


