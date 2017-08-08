## Fuzzy Query

>  国内对于Elasticsearch深入的人并不多,或者大多数大牛不屑于分享关于Elasticsearch的知识,这里讲讲 Elasticsearch中的Fuzzy Query

### 概念

    模糊查询利用了基于Levenshtein编辑距离的相似度
    关于什么是编辑距离(Levenshtein)以及算法,请查看下面的参考部分.

### 语法

~~~
GET /_search
{
    "query": {
        "fuzzy" : {
            "user" : {
                "value" :         "ki",
                    "boost" :         1.0,
                    "fuzziness" :     2,
                    "prefix_length" : 0,
                    "max_expansions": 100
            }
        }
    }
}
~~~

**参数说明**
- fuzziness   控制编辑距离(目前只支持0,1,2)
- boost 设置查询权重
- prefix_length  设置匹配的term的前prefix_length个字符不会参与模糊查询
- max_expansions 控制最大的返回结果


### Fuzziness ( 模糊性 )

当查询 text ( 文本 ) 或者 keyword fields ( 关键字字段 )时，模糊性被解释为 Levenshtein Edit Distance —— 是指两个字串之间，由一个转成另一个所需的最少编辑操作次数max_expansions来控制查询结果.

模糊性参数可以指定为：

**0， 1， 2**

最大允许 Levenshtein Edit Distance （或者编辑次数）。

**AUTO**

基于该项的长度 generates an edit distance ( 生成编辑距离 )。对于长度：

**0..2**
必须完全匹配
**3..5**
允许 one edit allowed ( 编辑一次 )
**>5**
允许 two edits allowed ( 编辑两次 )

> ![import](https://www.elastic.co/guide/en/elasticsearch/reference/5.5/images/icons/tip.png)  目前Elasticsearch仅支持编辑距离=2的查询,因为该操作比较重,在使用的时候,请最好使用

### Java API

~~~java
/**
         * fuzzyQuery基于编辑距离(Levenshtein)来进行相似搜索,比如搜索kimzhy,可以搜索出kinzhy(编辑距离为1)
         * 为了进行测试说明,前创建一个索引,插入几条数据ka,kab,kib,ba,我们的搜索源为ki
         * 了解更多关于编辑距离(Levenshtein)的概念,请参考:<a href='http://www.cnblogs.com/biyeymyhjob/archive/2012/09/28/2707343.html'></a>
         * 了解更多编辑距离的算法,请参考:<a href='http://blog.csdn.net/ironrabbit/article/details/18736185'></a>
         *  ki — ka 编辑距离为1
         *  ki — kab 编辑距离为2
         *  ki — kbia 编辑距离为3
         *  ki — kib 编辑距离为1
         *  所以当我们设置编辑距离(ES中使用fuzziness参数来控制)为0的时候,没有结果
         *  所以当我们设置编辑距离(ES中使用fuzziness参数来控制)为1的时候,会出现结果ka,kib
         *  所以当我们设置编辑距离(ES中使用fuzziness参数来控制)为2的时候,会出现结果ka,kib,kab
         *  所以当我们设置编辑距离(ES中使用fuzziness参数来控制)为3的时候,会出现结果ka,kib,kab,kbaa(很遗憾,ES本身最多只支持到2,因此不会出现此结果)
         */
        QueryBuilder qb = QueryBuilders.fuzzyQuery("username","ki")
//                .fuzziness(Fuzziness.ZERO);  没有结果
//                .fuzziness(Fuzziness.ONE);  会出现结果ka,kib
//                .fuzziness(Fuzziness.TWO);会出现结果ka,kib,kab
                .fuzziness(Fuzziness.AUTO); //会出现结果ka,kib,kab
        SearchResponse response = client.prepareSearch()
                .setIndices("index")
                .setTypes("type")
                .setQuery(qb)
                .execute()
                .actionGet();
~~~

代码详解请参考:[FuzzyQueryDemo.java](https://github.com/felayman/elasticsearch-java-api/blob/master/src/test/java/org/visualchina/elasticsearch/api/demo/query/FuzzyQueryDemo.java)

## 参考
- [Damerau–Levenshtein distance](https://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance)
- [编辑距离及编辑距离算法](http://www.cnblogs.com/biyeymyhjob/archive/2012/09/28/2707343.html)
- [Java算法之Levenshtein Distance（编辑距离）算法](http://blog.csdn.net/ironrabbit/article/details/18736185)
- [常见选项](http://cwiki.apachecn.org/pages/viewpage.action?pageId=4882851)

