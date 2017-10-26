## Cluster Administration


为了使用它们,你需要调用AdminClient的cluster()方法

~~~java
ClusterAdminClient clusterAdminClient = client.admin().cluster();
~~~

> ![](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/images/icons/note.png)
> 在剩下的教程中,我们将会使用client.admin().cluster().

### 集群健康(Cluster Health)

**Health**

群集健康API允许获得关于集群健康的非常简单的状态，并且还可以给您一些关于每个索引的集群状态的技术信息

~~~java
ClusterHealthResponse healths = client.admin().cluster().prepareHealth().get();     (1)
String clusterName = healths.getClusterName();              (2)
int numberOfDataNodes = healths.getNumberOfDataNodes();     (3)
int numberOfNodes = healths.getNumberOfNodes();     (4)

for (ClusterIndexHealth health : healths.getIndices().values()) {       (5)
    String index = health.getIndex();       (6)
    int numberOfShards = health.getNumberOfShards();    (7)
    int numberOfReplicas = health.getNumberOfReplicas(); (8)
    ClusterHealthStatus status = health.getStatus(); (9)
}
~~~

1. 获取所有索引信息
2. 获取集群名称
3. 获取所有数据节点的数量
4. 获取所有节点数量
5. 遍历所有索引
6. 索引名称
7. 主分片数量
8. 副本分片的数量
9. 索引状态

**Wait for status**

你可以使用集群健康api来等到集群中的某个索引状态变成指定的状态

~~~java
client.admin().cluster().prepareHealth()            (1)
        .setWaitForYellowStatus()   (2)
        .get();
client.admin().cluster().prepareHealth("company")   (3)
        .setWaitForGreenStatus() (4)
        .get();

client.admin().cluster().prepareHealth("employee")  (5)
        .setWaitForGreenStatus()    (6)
        .setTimeout(TimeValue.timeValueSeconds(2)) (7)
        .get();
~~~

1. 准备一个健康请求
2. 等待集群变成yellow状态
3. 在索引"company"上 准备一个健康请求
4. 等待集群变成green状态
5. 在索引"employee"上 准备一个健康请求
6. 等待集群变成green状态
7. 最多等待2秒钟



