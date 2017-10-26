
## Elasticsearch目前支持如下几种自动发现模块

- Azure Classic Discovery
        类似于多播）,它可作为插件使用,具体使用参考:[Azure Classic Discovery Plugin](https://www.elastic.co/guide/en/elasticsearch/plugins/5.6/discovery-azure-classic.html)
- EC2 Discovery
        作为插件使用,具体使用参考:[EC2 Discovery Plugin](https://www.elastic.co/guide/en/elasticsearch/plugins/5.6/discovery-ec2.html)
- Google Compute Engine Discovery
         (GCE) discovery (类似于多播）,它可作为插件使用,具体使用参考:[The Google Compute Engine Discovery plugin uses the GCE API for unicast discovery](https://www.elastic.co/guide/en/elasticsearch/plugins/5.6/discovery-gce.html)
- Zen Discovery(内置默认)
         它提供unicast发现，并且扩展到支持云环境和其他形式的发现,具体使用参考:[Zen Discovery](https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-discovery-zen.html)

 ## 单播、多播、广播、组播、泛播概念区分

 具体请参考如下几篇文章:

 - [单播、多播、广播、组播、泛播概念区分](http://blog.csdn.net/wscdylzjy/article/details/45013277)
 - [单播、多播（组播）和广播的区别](http://www.cnblogs.com/rogerroddick/archive/2009/08/31/1557228.html)