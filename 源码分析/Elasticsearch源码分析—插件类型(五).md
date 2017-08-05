
>  Elasticsearch版本为 5.5.0,下面是主要的核心流程,忽略异常补偿部分


### Elasticsearch中的插件


 > [Plugins](https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-plugins.html)  are a way to enhance the basic elasticsearch functionality in a custom manner. They range from adding custom mapping types, custom analyzers (in a more built in fashion), native scripts, custom discovery and more

Elasticseaarch提供插件的方式来让更多的开发者来增强Elasticsearch的功能

### 插件类型

    Elasticsearch提供了如下几种插件类型

#### 1. ActionPlugin

> Rest命令请求插件,如果Elasticsearch内置的命令如_all,_cat,_/cat/health等rest命令无法满足需求,开发者可以自己开发需要的rest命令.

#### 2. AnalysisPlugin

> 分析插件,用于开发者开发额外的分析功能来增强Elasticsearch自身分析功能的不足.

#### 3. ClusterPlugin

> 集群插件,

#### 4. DiscoveryPlugin

> 发现插件

#### 5. IngestPlugin

> 预处理插件

#### 6. MapperPlugin

> 映射插件,强ES的数据类型.比如增加一个attachment类型,里面可以放PDF或者WORD数据

#### 7. NetworkPlugin

> 网络插件插件,

#### 8. RepositoryPlugin

> 存储插件,提供快照和恢复

#### 8. ScriptPlugin

> 脚本插件.这个插件本质来说,就是会调用用户的脚本,所以可以执行任何的程序,举例的话,可以通过这个插件,支持javascript语言,python语言,也可以是用户自定义的任何语言或者程序

#### 8. SearchPlugin

> 查询插件,扩展Elasticsearch的查询功能