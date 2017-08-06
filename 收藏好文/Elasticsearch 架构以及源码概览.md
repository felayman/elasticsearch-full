
# Elasticsearch 架构以及源码概览

转载于: [Elasticsearch 架构以及源码概览](http://www.code123.cc/2582.html)

Elasticsearch 是最近两年异军突起的一个兼有搜索引擎和NoSQL数据库功能的开源系统，基于Java/Lucene构建。最近研究了一下，感觉 Elasticsearch 的架构以及其开源的生态构建都有许多可借鉴之处，所以整理成文章分享下。本文的代码以及架构分析主要基于 Elasticsearch 2.X 最新稳定版。

Elasticsearch 看名字就能大概了解下它是一个弹性的搜索引擎。首先弹性隐含的意思是分布式，单机系统是没法弹起来的，然后加上灵活的伸缩机制，就是这里的 Elastic 包含的意思。它的搜索存储功能主要是 Lucene 提供的，Lucene 相当于其存储引擎，它在之上封装了索引，查询，以及分布式相关的接口。

## Elasticsearch 中的几个概念

1. 集群（Cluster）一组拥有共同的 cluster name 的节点。
2. 节点（Node) 集群中的一个 Elasticearch 实例。
3. 索引（Index) 相当于关系数据库中的database概念，一个集群中可以包含多个索引。这个是个逻辑概念。
4. 主分片（Primary shard） 索引的子集，索引可以切分成多个分片，分布到不同的集群节点上。分片对应的是 Lucene 中的索引。
5. 副本分片（Replica shard）每个主分片可以有一个或者多个副本。
6.  类型（Type）相当于数据库中的table概念，mapping是针对 Type 的。同一个索引里可以包含多个 Type。
7.  Mapping 相当于数据库中的schema，用来约束字段的类型，不过 Elasticsearch 的 mapping 可以自动根据数据创建。
8. 文档（Document) 相当于数据库中的row。
9. 字段（Field）相当于数据库中的column。
10. 分配（Allocation） 将分片分配给某个节点的过程，包括分配主分片或者副本。如果是副本，还包含从主分片复制数据的过程。

## 分布式以及 Elastic

分布式系统要解决的第一个问题就是节点之间互相发现以及选主的机制。如果使用了 Zookeeper/Etcd 这样的成熟的服务发现工具，这两个问题都一并解决了。但 Elasticsearch 并没有依赖这样的工具，带来的好处是部署服务的成本和复杂度降低了，不用预先依赖一个服务发现的集群，缺点当然是将复杂度带入了 Elasticsearch 内部。

**服务发现以及选主 ZenDiscovery**

1. 节点启动后先ping（这里的ping是 Elasticsearch 的一个RPC命令。如果 discovery.zen.ping.unicast.hosts 有设置，则ping设置中的host，否则尝试ping localhost 的几个端口， Elasticsearch 支持同一个主机启动多个节点）
2. Ping的response会包含该节点的基本信息以及该节点认为的master节点。
3. 选举开始，先从各节点认为的master中选，规则很简单，按照id的字典序排序，取第一个。
4. 如果各节点都没有认为的master，则从所有节点中选择，规则同上。这里有个限制条件就是 discovery.zen.minimum_master_nodes，如果节点数达不到最小值的限制，则循环上述过程，直到节点数足够可以开始选举。
5. 最后选举结果是肯定能选举出一个master，如果只有一个local节点那就选出的是自己。
6. 如果当前节点是master，则开始等待节点数达到 minimum_master_nodes，然后提供服务。
7. 如果当前节点不是master，则尝试加入master。

Elasticsearch 将以上服务发现以及选主的流程叫做 ZenDiscovery 。由于它支持任意数目的集群（1-N）,所以不能像 Zookeeper/Etcd 那样限制节点必须是奇数，也就无法用投票的机制来选主，而是通过一个规则，只要所有的节点都遵循同样的规则，得到的信息都是对等的，选出来的主节点肯定是一致的。但分布式系统的问题就出在信息不对等的情况，这时候很容易出现脑裂（Split-Brain）的问题，大多数解决方案就是设置一个quorum值，要求可用节点必须大于quorum（一般是超过半数节点），才能对外提供服务。而 Elasticsearch 中，这个quorum的配置就是 discovery.zen.minimum_master_nodes 。 说到这里要吐槽下 Elasticsearch 的方法和变量命名，它的方法和配置中的master指的是master的候选节点，也就是说可能成为master的节点，并不是表示当前的master，我就被它的一个 isMasterNode 方法坑了，开始一直没能理解它的选举规则。

**弹性伸缩 Elastic**

Elasticsearch 的弹性体现在两个方面：

1. 服务发现机制让节点很容易加入和退出。
 2. 丰富的设置以及allocation API。

Elasticsearch 节点启动的时候只需要配置discovery.zen.ping.unicast.hosts，这里不需要列举集群中所有的节点，只要知道其中一个即可。当然为了避免重启集群时正好配置的节点挂掉，最好多配置几个节点。节点退出时只需要调用 API 将该节点从集群中排除 （Shard Allocation Filtering），系统会自动迁移该节点上的数据，然后关闭该节点即可。当然最好也将不可用的已知节点从其他节点的配置中去除，避免下次启动时出错。

分片（Shard）以及副本（Replica）  分布式存储系统为了解决单机容量以及容灾的问题，都需要有分片以及副本机制。Elasticsearch 没有采用节点级别的主从复制，而是基于分片。它当前还未提供分片切分（shard-splitting）的机制，只能创建索引的时候静态设置。

![](http://7xlqnq.com1.z0.glb.clouddn.com/wp-content/uploads/2016/07/e2b403a5d4f628f1c63139bac4bee366.png)

(elasticsearch 官方博客的图片)

比如上图所示，开始设置为5个分片，在单个节点上，后来扩容到5个节点，每个节点有一个分片。如果继续扩容，是不能自动切分进行数据迁移的。官方文档的说法是分片切分成本和重新索引的成本差不多，所以建议干脆通过接口重新索引。

Elasticsearch 的分片默认是基于id 哈希的，id可以用户指定，也可以自动生成。但这个可以通过参数（routing）或者在mapping配置中修改。当前版本默认的哈希算法是MurmurHash3。

Elasticsearch 禁止同一个分片的主分片和副本分片在同一个节点上，所以如果是一个节点的集群是不能有副本的。

**恢复以及容灾**

分布式系统的一个要求就是要保证高可用。前面描述的退出流程是节点主动退出的场景，但如果是故障导致节点挂掉，Elasticsearch 就会主动allocation。但如果节点丢失后立刻allocation，稍后节点恢复又立刻加入，会造成浪费。Elasticsearch的恢复流程大致如下：

1. 集群中的某个节点丢失网络连接
2. master提升该节点上的所有主分片的在其他节点上的副本为主分片
3. cluster集群状态变为 yellow ,因为副本数不够
4. 等待一个超时设置的时间，如果丢失节点回来就可以立即恢复（默认为1分钟，通过 index.unassigned.node_left.delayed_timeout 设置）。如果该分片已经有写入，则通过translog进行增量同步数据。
5. 否则将副本分配给其他节点，开始同步数据。

但如果该节点上的分片没有副本，则无法恢复，集群状态会变为red，表示可能要丢失该分片的数据了。

分布式集群的另外一个问题就是集群整个重启后可能导致不预期的分片重新分配（部分节点没有启动完成的时候，集群以为节点丢失），浪费带宽。所以 Elasticsearch 通过以下静态配置（不能通过API修改）控制整个流程，以10个节点的集群为例：

- gateway.recover_after_nodes: 8
- gateway.expected_nodes: 10
- gateway.recover_after_time: 5m

比如10个节点的集群，按照上面的规则配置，当集群重启后，首先系统等待 minimum_master_nodes（6）个节点加入才会选出master， recovery操作是在 master节点上进行的，由于我们设置了 recover_after_nodes（8），系统会继续等待到8个节点加入， 才开始进行recovery。当开始recovery的时候，如果发现集群中的节点数小于expected_nodes，也就是还有部分节点未加入，于是开始recover_after_time 倒计时(如果节点数达到expected_nodes则立刻进行 recovery)，5分钟后，如果剩余的节点依然没有加入，则会进行数据recovery。

## 搜索引擎 Search

Elasticsearch 除了支持 Lucene 本身的检索功能外，在之上做了一些扩展。 1. 脚本支持
Elasticsearch 默认支持groovy脚本，扩展了 Lucene 的评分机制，可以很容易的支持复杂的自定义评分算法。它默认只支持通过sandbox方式实现的脚本语言（如lucene expression，mustache），groovy必须明确设置后才能开启。Groovy的安全机制是通过java.security.AccessControlContext设置了一个class白名单来控制权限的，1.x版本的时候是自己做的一个白名单过滤器，但限制策略有漏洞，导致一个远程代码执行漏洞。 2. 默认会生成一个 _all 字段，将所有其他字段的值拼接在一起。这样搜索时可以不指定字段，并且方便实现跨字段的检索。 3. Suggester Elasticsearch 通过扩展的索引机制，可以实现像google那样的自动完成suggestion以及搜索词语错误纠正的suggestion。

## NoSQL 数据库

Elasticsearch 可以作为数据库使用，主要依赖于它的以下特性：

1. 默认在索引中保存原始数据，并可获取。这个主要依赖 Lucene 的store功能。
2. 实现了translog，提供了实时的数据读取能力以及完备的数据持久化能力（在服务器异常挂掉的情况下依然不会丢数据）。Lucene 因为有 IndexWriter buffer, 如果进程异常挂掉，buffer中的数据是会丢失的。所以 Elasticsearch 通过translog来确保不丢数据。同时通过id直接读取文档的时候，Elasticsearch 会先尝试从translog中读取，之后才从索引中读取。也就是说，即便是buffer中的数据尚未刷新到索引，依然能提供实时的数据读取能力。Elasticsearch 的translog 默认是每次写请求完成后统一fsync一次，同时有个定时任务检测（默认5秒钟一次）。如果业务场景需要更大的写吞吐量，可以调整translog相关的配置进行优化。
3. dynamic-mapping 以及 schema-free
4. Elasticsearch 的dynamic-mapping相当于根据用户提交的数据，动态检测字段类型，自动给数据库表建立表结构，也可以动态增加字段，所以它叫做schema-free，而不是schema-less。这种方式的好处是用户能一定程度享受schema-less的好处，不用提前建立表结构，同时因为实际上是有schema的，可以做查询上的优化，检索效率要比纯schema-less的数据库高许多。但缺点就是已经创建的索引不能变更数据类型（Elasticsearch 写入数据的时候如果类型不匹配会自动尝试做类型转换，如果失败就会报错，比如数字类型的字段写入字符串”123”是可以的，但写入”abc”就不可以。），要损失一定的自由度。另外 Elasticsearch 提供的index-template功能方便用户动态创建索引的时候预先设定索引的相关参数以及type mapping，比如按天创建日志库，template可以设置为对 log-* 的索引都生效。这两个功能我建议新的数据库都可以借鉴下。
5. 丰富的QueryDSL功能
6. Elasticsearch 的query语法基本上和sql对等的，除了join查询，以及嵌套临时表查询不能支持。不过 Elasticsearch 支持嵌套对象以及parent外部引用查询，所以一定程度上可以解决关联查询的需求。另外group by这种查询可以通过其aggregation实现。Elasticsearch 提供的aggregation能力非常强大，其生态圈里的 Kibana 主要就是依赖aggregation来实现数据分析以及可视化的。

## 系统架构

Elasticsearch 的依赖注入用的是guice，网络使用netty，提供http rest和RPC两种协议。

Elasticsearch 之所以用guice，而不是用spring做依赖注入，关键的一个原因是guice可以帮它很容易的实现模块化，通过代码进行模块组装，可以很精确的控制依赖注入的管理范围。比如 Elasticsearch 给每个shard单独生成一个injector，可以将该shard相关的配置以及组件注入进去，降低编码和状态管理的复杂度，同时删除shard的时候也方便回收相关对象。这方面有兴趣使用guice的可以借鉴。

**ClusterState**

前面我们分析了 Elasticsearch 的服务发现以及选举机制，它是内部自己实现的。服务发现工具做的事情其实就是跨服务器的状态同步，多个节点修改同一个数据对象，需要有一种机制将这个数据对象同步到所有的节点。Elasticsearch 的ClusterState 就是这样一个数据对象，保存了集群的状态，索引/分片的路由表，节点列表，元数据等，还包含一个ClusterBlocks，相当于分布式锁，用于实现分布式的任务同步。

主节点上有个单独的进程处理 ClusterState 的变更操作，每次变更会更新版本号。变更后会通过PRC接口同步到其他节点。主节知道其他节点的ClusterState 的当前版本，发送变更的时候会做diff，实现增量更新。

**Rest 和 RPC**

![elasticsearch-rest](http://7xlqnq.com1.z0.glb.clouddn.com/wp-content/uploads/2016/07/7fee084fa20eb9318faf30671fdc2b8d.jpg)

Elasticsearch 的rest请求的传递流程如上图（这里对实际流程做了简化）： 1. 用户发起http请求，Elasticsearch 的9200端口接受请求后，传递给对应的RestAction。 2. RestAction做的事情很简单，将rest请求转换为RPC的TransportRequest，然后调用NodeClient，相当于用客户端的方式请求RPC服务，只不过transport层会对本节点的请求特殊处理。

这样做的好处是将http和RPC两层隔离，增加部署的灵活性。部署的时候既可以同时开启RPC和http服务，也可以用client模式部署一组服务专门提供http rest服务，另外一组只开启RPC服务，专门做data节点，便于分担压力。

Elasticsearch 的RPC的序列化机制使用了 Lucene 的压缩数据类型，支持vint这样的变长数字类型，省略了字段名，用流式方式按顺序写入字段的值。每个需要传输的对象都需要实现：
~~~
1 void writeTo(StreamOutput out)
2  T readFrom(StreamInput in)
~~~


两个方法。虽然这样实现开发成本略高，增删字段也不太灵活，但对 Elasticsearch 这样的数据库系统来说，不用考虑跨语言，增删字段肯定要考虑兼容性，这样做效率最高。所以 Elasticsearch 的RPC接口只有java client可以直接请求，其他语言的客户端都走的是rest接口。

**网络层**

Elasticsearch 的网络层抽象很值得借鉴。它抽象出一个 Transport 层，同时兼有client和server功能，server端接收其他节点的连接，client维持和其他节点的连接，承担了节点之间请求转发的功能。Elasticsearch 为了避免传输流量比较大的操作堵塞连接，所以会按照优先级创建多个连接，称为channel。

- recovery: 2个channel专门用做恢复数据。如果为了避免恢复数据时将带宽占满，还可以设置恢复数据时的网络传输速度。
- bulk: 3个channel用来传输批量请求等基本比较低的请求。
- regular: 6个channel用来传输通用正常的请求，中等级别。
- state: 1个channel保留给集群状态相关的操作，比如集群状态变更的传输，高级别。
- ping: 1个channel专门用来ping，进行故障检测。

![channels-three-nodes](http://7xlqnq.com1.z0.glb.clouddn.com/wp-content/uploads/2016/07/7c02b0ae85ce240dc313991851ca8c82.png)
(3个节点的集群连接示意，来源 Elasticsearch 官方博客)

每个节点默认都会创建13个到其他节点的连接，并且节点之间是互相连接的，每增加一个节点，该节点会到每个节点创建13个连接，而其他每个节点也会创建13个连回来的连接。

**线程池**

由于java不支持绿色线程（fiber/coroutine)，我前面的《并发之痛》那篇文章也分析了线程池的问题，线程池里保留多少线程合适？如何避免慢的任务占用线程池，导致其他比较快的任务也得不到执行？很多应用系统里，为了避免这种情况，会随手创建线程池，最后导致系统里充塞了大的量的线程池，浪费资源。而 Elasticsearch 的解决方案是分优先级的线程池。它默认创建了10多个线程池，按照不同的优先级以及不同的操作进行划分。然后提供了4种类型的线程池，不同的线程池使用不同的类型：

- CACHED 最小为0，无上限，无队列（SynchronousQueue，没有缓冲buffer），有存活时间检测的线程池。通用的，希望能尽可能支撑的任务。
- DIRECT 直接在调用者的线程里执行，其实这不算一种线程池方案，主要是为了代码逻辑上的统一而创造的一种线程类型。
- FIXED 固定大小的线程池，带有缓冲队列。用于计算和IO的耗时波动较小的操作。
- SCALING 有最小值，最大值的伸缩线程池，队列是基于LinkedTransferQueue 改造的实现，和java内置的Executors生成的伸缩线程池的区别是优先增加线程，增加到最大值后才会使用队列，和java内置的线程池规则相反。用于计算和IO耗时都不太稳定，需要限制系统承载最大任务上限的操作。

这种解决方案虽然要求每个用到线程池的地方都需要评估下执行成本以及应该用什么样的线程池，但好处是限制了线程池的泛滥，也缓解了不同类型的任务互相之间的影响。

**脑洞时间**

以后每篇分析架构的文章，我都最后会提几个和该系统相关的改进或者扩展的想法，称为脑洞时间，作为一种锻炼。不过只提供想法，不深入分析可行性以及实现。

1. 支持shard-spliting
2. 这个被人吐糟了好长时间，官方就是不愿意提供。我简单构想了下，感觉实现这个应该也不复杂。一种实现方式是按照传统的数据库sharding机制，1分2，2分4，4分8等，主要扩展点在数据迁移以及routing的机制上。但这种方式没办法实现1分3，3分5，这样的sharding。另外一个办法就是基于当前官方推荐的重建索引的机制，只是对外封装成resharding的接口，先给旧索引创建别名，客户端通过别名访问索引，然后设定新索引的sharding数目，后台创建新的索引，倒数据，等数据追上的时候，切换别名，进行完整性检查，这样整个resharding的机制可以自动化了。
3. 支持mapreduce
4. 认为Elasticsearch 可以借鉴 Mongo 的轻量mapreduce机制，这样可以支持更丰富的聚合查询。
5. 支持语音以及图片检索
6. 当前做语音和图片识别的库或者服务的开发者可以提供一个 Elasticsearch 插件，把语音以及图片转换成文本进行索引查询，应用场景应该也不少。
    用ForkJoinPool来替代 Elasticsearch 当前的线程池方案
    ForkJoinPool加上java8的CompletableFuture，一定程度上可以模拟coroutine效果，再加上最新版本的netty内部已经默认用了ForkJoinPool，Elasticsearch 这种任务有需要拆子任务的场景，很适合使用ForkJoinPool。

## Elasticsearch 的开源产品启示

还记得10年前在大学时候捣鼓 Lucene，弄校园内搜索，还弄了个基于词典的分词工具。毕业后第一份工作也是用 Lucene 做站内搜索。当时搭建的服务和 Elasticsearch 类似，提供更新和管理索引的api给业务程序，当然没有 Elasticsearch 这么强大。当时是有想过做类似的一个开源产品的，后来发现apache已经出了 Solr（2004年的时候就创建了，2008年1.3发布，已经相对成熟），感觉应该没啥机会了。但 Elasticsearch 硬是在这种情况下成长起来了（10年创建，14年才发布1.0）。 二者的功能以及性能几乎都不相上下（开始性能上有些差距，但 Solr 有改进，差不多追上了），参看文末比较链接。

我觉得一方面是 Elasticsearch 的简单友好的分布式机制占了先机，也正好赶上了移动互联网爆发移动应用站内搜索需求高涨的时代。第一波站内搜索是web时代，也是 Lucene 诞生的时代，但web的站内搜索可以简单的利用搜索引擎服务的自定义站点实现，而应用的站内搜索就只能靠自己搭了。另外一方面是 Elasticsearch 的周边生态以及目标市场看把握的非常精准。Elasticsearch 现在的主要目标市场已经从站内搜索转移到了监控与日志数据的收集存储和分析，也就是大家常谈论的ELK。

Elasticsearch 现在主要的应用场景有三块。站内搜索，主要和 Solr 竞争，属于后起之秀。NoSQL json文档数据库，主要抢占 Mongo 的市场，它在读写性能上优于 Mongo（见文末比较链接），同时也支持地理位置查询，还方便地理位置和文本混合查询，属于歪打正着。监控，统计以及日志类时间序的数据的存储和分析以及可视化，这方面是引领者。

据说 Elasticsearch 的创始人当初创建 Elasticsearch 的时候是为了给喜欢做菜的媳妇搭建个菜谱的搜索网站，虽然菜谱搜索网站最后一直没做出来，但诞生了 Elasticsearch。所以程序员坚持一个业余项目也是很重要的，万一无心插柳就成荫了呢？
