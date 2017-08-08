## Match Query

>  国内对于Elasticsearch深入的人并不多,或者大多数大牛不屑于分享关于Elasticsearch的知识,这里讲讲 Elasticsearch中的Fuzzy Query

### 概念

    Match Query 是Elasticsearch提供的一个基于全文字段处理的高级查询.它知道如何处理全文字段(Full-text, analyzed)和精确值字段(Exact-value，not_analyzed)

 > 但是大多数人对于 Match Query的认识只是出于一个浅尝辄止的阶段,在很多地方经常会听到一些初学者在滥用Match Query后抱怨查询结果不理想.


### 基础语法


**索引一些数据**
首先，我们会创建一个新的索引并通过bulk API索引一些文档：
~~~
DELETE /my_index

PUT /my_index
{ "settings": { "number_of_shards": 1 }}

POST /my_index/my_type/_bulk
{ "index": { "_id": 1 }}
{ "title": "The quick brown fox" }
{ "index": { "_id": 2 }}
{ "title": "The quick brown fox jumps over the lazy dog" }
{ "index": { "_id": 3 }}
{ "title": "The quick brown fox jumps over the quick dog" }
{ "index": { "_id": 4 }}
{ "title": "Brown fox brown dog" }
~~~

**单词查询(Single word query)**

~~~
GET /_search
{
    "query": {
        "match" : {
            "title" : "QUICK!"
        }
    }
}
~~~

    Match Query会对用户输入的内容进行高级处理,比如对其进行分词处理,会分析输入内容的类型,如果是日期类型或数字类型,就会采用精确匹配,如果是是一个文本内容,则
    会对其进行分析成词条(terms),然后采用比较低级的term查询进行处理,同时 Match Query提供了一些特性来更好的帮助我们优化搜索结果,比如fuzziness

当我们使用了如上的查询,ES会按照如下的方式执行上面的match查询：

1. 检查字段类型

        title字段是一个全文字符串字段(analyzed)，意味着查询字符串也需要被分析。

2. 解析查询字符串

        查询字符串"QUICK!"会被传入到标准解析器中，得到的结果是单一词条"quick"。因为我们得到的只有一个词条，match查询会使用一个term低级查询来执行查询。

3. 找到匹配的文档

        term查询会在倒排索引中查询"quick"，然后获取到含有该词条的文档列表，在这个例子中，文档1，2，3会被返回。

4. 对每份文档打分

        term查询会为每份匹配的文档计算其相关度分值_score，该分值通过综合考虑词条频度(Term Frequency)("quick"在匹配的每份文档的title字段中出现的频繁程度)，倒排频度(Inverted Document Frequency)("quick"在整个索引中的所有文档的title字段中的出现程度)，以及每个字段的长度(较短的字段会被认为相关度更高)来得到。参考什么是相关度(What is Relevance?)

 **说明:** 该部分参考文章:[[Elasticsearch] 全文搜索 (一) - 基础概念和match查询 ](http://blog.csdn.net/dm_vincent/article/details/41693125)


## 高级特性

###  Fuzziness ( 模糊性 )

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

当我们在进行Match Query的时候,Elasticsearch同时也提供了对查询的内容进行模糊性处理,处理的方式是靠fuzziness参数控制

我们继续以上面的查询为例,当我们使用如下的查询:
~~~
POST /my_index/my_type/_search
{
  "query": {
    "match": {
      "title": "quick"
    }
  }
}
~~~

我们能得到三条匹配结果:
~~~
{
  "took": 0,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "failed": 0
  },
  "hits": {
    "total": 3,
    "max_score": 0.42327404,
    "hits": [
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "1",
        "_score": 0.42327404,
        "_source": {
          "title": "The quick brown fox"
        }
      },
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "3",
        "_score": 0.42211798,
        "_source": {
          "title": "The quick brown fox jumps over the quick dog"
        }
      },
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "2",
        "_score": 0.2887157,
        "_source": {
          "title": "The quick brown fox jumps over the lazy dog"
        }
      }
    ]
  }
}
~~~

我们修改上面的查询为:
~~~
POST /my_index/my_type/_search
{
  "query": {
    "match": {
      "title": "quack"
    }
  }
}
~~~

我们将得不到查询结果:
~~~
{
  "took": 0,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "failed": 0
  },
  "hits": {
    "total": 0,
    "max_score": null,
    "hits": []
  }
}
~~~

因为索引的文档中并没有quack词条,因此无法匹配到相关文档,那我们来尝试使用Match Query提供的Fuzziness特性,我们修改查询为:
~~~
POST /my_index/my_type/_search
{
  "query": {
    "match": {
      "title": {
        "query": "quack",
        "fuzziness": 1
      }
    }
  }
}
~~~

我们只是添加了一个fuzziness参数,并设置该值为1(即编辑距离为1),我们再来看查询结果:
~~~
{
  "took": 5,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "failed": 0
  },
  "hits": {
    "total": 3,
    "max_score": 0.3386192,
    "hits": [
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "1",
        "_score": 0.3386192,
        "_source": {
          "title": "The quick brown fox"
        }
      },
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "3",
        "_score": 0.33769438,
        "_source": {
          "title": "The quick brown fox jumps over the quick dog"
        }
      },
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "2",
        "_score": 0.23097254,
        "_source": {
          "title": "The quick brown fox jumps over the lazy dog"
        }
      }
    ]
  }
}
~~~

而这种情况的使用场景是什么呢？

当用户输入一个短语或单词,用户很可能输错了其中一个字母或汉字,那么这个时候我们的系统是应该能发现这种情况,并返回给用户期望的结果。

###  operator

Match Query 的另一个特性就是允许我们控制在分析匹配短语后的并存情况,这个特性是靠参数operator来进行控制的.

我们仍旧拿上面的例子说明,这次我们修改查询语句为:
~~~
POST /my_index/my_type/_search
{
  "query": {
    "match": {
      "title": {
        "query": "quick dog",
        "operator": "and"
      }
    }
  }
}
~~~

结果为:
~~~
{
  "took": 0,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "failed": 0
  },
  "hits": {
    "total": 2,
    "max_score": 0.71083367,
    "hits": [
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "3",
        "_score": 0.71083367,
        "_source": {
          "title": "The quick brown fox jumps over the quick dog"
        }
      },
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "2",
        "_score": 0.5774314,
        "_source": {
          "title": "The quick brown fox jumps over the lazy dog"
        }
      }
    ]
  }
}
~~~

查询内容"quick dog"被分析后成为两个词条("quick","dog"),而operator参数则控制这两个词条的并存情况,如果operator为and,则匹配到的文档
中必须同时包含"quick","dog"两个词条,如果operator为or,则匹配到的文档中则必须包含"quick","dog"两个词条中的一个即可。

我们修改operator为or,结果为
~~~
{
  "took": 0,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "failed": 0
  },
  "hits": {
    "total": 4,
    "max_score": 0.71083367,
    "hits": [
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "3",
        "_score": 0.71083367,
        "_source": {
          "title": "The quick brown fox jumps over the quick dog"
        }
      },
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "2",
        "_score": 0.5774314,
        "_source": {
          "title": "The quick brown fox jumps over the lazy dog"
        }
      },
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "1",
        "_score": 0.42327404,
        "_source": {
          "title": "The quick brown fox"
        }
      },
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "4",
        "_score": 0.42327404,
        "_source": {
          "title": "Brown fox brown dog"
        }
      }
    ]
  }
}
~~~

###  Zero terms query(零词条查询)

        Zero terms query是Match Query的一个特例,我们都知道,有些分析器会在分析输入内容的时候对内容进行停顿词处理(即将一些停顿词删除掉),比如"english"分析器就会将
        类似to,or,a,the ,not ,be等高频词汇当做停顿词处理,那么这样就会存在这样一个特殊情况,比如我们在对内容"to be or not to be"进行搜索的话,很遗憾,该内容被标准分析
        器分析之后将不会得到词条

我们使用Elasticsearch提供的内置"english"分析器对"to be or not to be"进行分词之后,查看结果为:
~~~
POST /_analyze
{
  "text": ["to be or not to be"],
  "analyzer": "english"
}
~~~
结果为:
~~~
{
  "tokens": []
}
~~~

一脸懵逼,不局限于英语,任何语言都可能存在这种情况,那如何能保证能够搜索这样的文本内容呢？Elasticsearch提供了zero_terms_query参数来控制,

比如我们使用这样的查询来看看:
~~~
POST /my_index/my_type/_search
{
  "query": {
    "match": {
      "title": {
        "query": "to be or not to be",
        "analyzer": "english"
      }
    }
  }
}
~~~

结果为:
~~~
{
  "took": 0,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "failed": 0
  },
  "hits": {
    "total": 0,
    "max_score": null,
    "hits": []
  }
}
~~~

我们添加zero_terms_query参数之后,
~~~
POST /my_index/my_type/_search
{
  "query": {
    "match": {
      "title": {
        "query": "to be or not to be",
        "analyzer": "english",
        "zero_terms_query": "all"
      }
    }
  }
}
~~~

结果都出来了:
~~~
{
  "took": 0,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "failed": 0
  },
  "hits": {
    "total": 5,
    "max_score": 1,
    "hits": [
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "1",
        "_score": 1,
        "_source": {
          "title": "The quick brown fox"
        }
      },
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "2",
        "_score": 1,
        "_source": {
          "title": "The quick brown fox jumps over the lazy dog"
        }
      },
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "3",
        "_score": 1,
        "_source": {
          "title": "The quick brown fox jumps over the quick dog"
        }
      },
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "4",
        "_score": 1,
        "_source": {
          "title": "Brown fox brown dog"
        }
      },
      {
        "_index": "my_index",
        "_type": "my_type",
        "_id": "5",
        "_score": 1,
        "_source": {
          "title": "to be or not to be"
        }
      }
    ]
  }
}
~~~

## 参考
- [Damerau–Levenshtein distance](https://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance)
- [编辑距离及编辑距离算法](http://www.cnblogs.com/biyeymyhjob/archive/2012/09/28/2707343.html)
- [Java算法之Levenshtein Distance（编辑距离）算法](http://blog.csdn.net/ironrabbit/article/details/18736185)
- [常见选项](http://cwiki.apachecn.org/pages/viewpage.action?pageId=4882851)

