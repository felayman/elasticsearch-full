# Elasticsearch系统概念及架构图

## Elasticsearch集群的概念（cluster）

在一个分布式系统里面,可以通过多个elasticsearch运行实例组成一个集群,这个集群里面有一个节点叫做主节点(master),elasticsearch是去中心化的,所以这里的主节点是动态选举出来的,不存在单点故障。



在同一个子网内，只需要在每个节点上设置相同的集群名,elasticsearch就会自动的把这些集群名相同的节点组成一个集群。节点和节点之间通讯以及节点之间的数据分配和平衡全部由elasticsearch自动管理。



在外部看来elasticsearch就是一个整体。


## Elasticsearch 节点(node)



每一个运行实例称为一个节点,每一个运行实例既可以在同一机器上,也可以在不同的机器上。



所谓运行实例,就是一个服务器进程，在测试环境中可以在一台服务器上运行多个服务器进程，在生产环境中建议每台服务器运行一个服务器进程。


## Elasticsearch索引(index)



Elasticsearch里的索引概念是名词而不是动词，在elasticsearch里它支持多个索引。



优点类似于关系数据库里面每一个服务器可以支持多个数据库是一个道理，在每一索引下面又可以支持多种类型，这又类似于关系数据库里面的一个数据库可以有多张表一样。



但是本质上和关系数据库还是有很大的区别，我们这里暂时可以这么理解。


## Elasticsearch 分片(shards)



Elasticsearch 它会把一个索引分解为多个小的索引，每一个小的索引就叫做分片。



分片之后就可以把各个分片分配到不同的节点中去。


## Elasticsearch副本(replicas)



Elasticsearch的每一个分片都可以有0到多个副本，而每一个副本也都是分片的完整拷贝，好处是可以用它来增加速度的同时也提高了系统的容错性。



一旦Elasticsearch的某个节点数据损坏或则服务不可用的时候，那么这个时就可以用其他节点来代替坏掉的节点，以达到高考用的目的。


## Elasticsearch 的recovery概念



Elasticsearch 的recovery代表的是数据恢复或者叫做数据重新分布。



elasticsearch 当有节点加入或退出时时它会根据机器的负载对索引分片进行重新分配，当挂掉的节点再次重新启动的时候也会进行数据恢复。


## Elasticsearch river



Elasticsearch river 代表的是一个数据源，这也是其它存储方式（比如：数据库）同步数据到 elasticsearch 的一个方法。



它是以插件方式存在的一个 elasticsearch 服务，通过读取 river 中的数据并把它索引到 elasticsearch 当中去，官方的 river 有 couchDB、RabbitMQ、Twitter、Wikipedia。


## Elasticsearch 的 gateway 概念



gateway 代表 elasticsearch 索引的持久化存储方式，elasticsearch 默认是先把索引存放到内存中去，当内存满了的时候再持久化到硬盘里。



当这个 elasticsearch 集群关闭或者再次重新启动时就会从 gateway 中读取索引数据。



elasticsearch 支持多种类型的 gateway，有本地文件系统（默认），分布式文件系统，Hadoop 的 HDFS 和 amazon 的 s3 云存储服务。


## Elasticsearch的discovery.zen概念



discovery.zen代表 elasticsearch 的自动节点发现机制，而且 elasticsearch还是一个基于 p2p 的系统。



首先它它会通过以广播的方式去寻找存在的节点，然后再通过多播协议来进行节点之间的通信，于此同时也支持点对点的交互操作。


## Elasticsearch里Transport的概念



Transport代表 elasticsearch 内部的节点或者集群与客户端之间的交互方式。



默认的内部是使用 tcp 协议来进行交互的，同时它支持 http 协议（json格式）、thrift、servlet、memcached、zeroMQ等多种的传输协议（通过插件方式集成）。


## Elasticsearch分布式搜索引擎架构图



说完Elasticsearch的几个基本概念后，给大伙上一张 Elasticsearch分布式搜索引擎的总体框架图：

![Elasticsearch分布式搜索引擎的总体框架图](http://images2015.cnblogs.com/blog/462486/201608/462486-20160819212144031-548437647.png)



ElasticSearch是基于Lucene开发的分布式搜索框架，包含如下特性：



1. 分布式索引、搜索。
2. 索引自动分片、负载均衡。
3. 自动发现机器、组建集群。
4. 支持Restful 风格接口。
5. 配置简单等。
