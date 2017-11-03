## Mapping

mapping是用来处理一个文档以及字段是如何包含,存储和索引的过程.例如,使用mapping来进行如下定义:

- 一个字符串字段应该被当做一个全文本字段
- 一个字段包含数字,日期,或者GEO地理位置
- 是否所有的字段都需要被索引到_all字段中
- 各种日期格式化
- 自定义控制动态字段添加

### Mapping Types(mapping类型)

每个索引都会有一个或多个mapping类型,用于从逻辑上隔离一个索引单元的数据.比如用户文档应该存储在 user类型,博客文章应该存储在blogpostl类型中.

~~[6.0.0]~~

索引的mapping类型从6.0.0版本开始会被移除

[Meta-fields](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-fields.html)

元字段被用来自定义如何与一个文档的元数据进行关联绑定的.比如通常一个文档的元字段应该包含_index,_type,_id,_source。

[Fields or properties](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html)

一个mapping通常包含一系列属性或字段.

一个user类型也许包含title,name,age等字段,一个blogpost类型也许包含title,body,user_id等字段.

同一个索引下的不同类型下只能包含一个名称相同的字段.

### Field datatype(字段类型)

每个字段都会有一个字段类型,可以是如下类型:

- 简单数据类型如text,keyword,date,long,double,boolean,ip
- 支持类似json层级结构的数据类型如nested,object
- 一些特殊数据类型如geo_point,geo_shape,completion

通常情况下,我们会为了不同的目的而用不同的方式来索引同一个字段.比如我们会索引一个字段为string来做
全文检索,也会索引一个字段为keyword用来做排序或聚合操作.此外,你可以为一个字段设置索引为 standard analyzer,english analyzer,french analyzer.

### Settings to prevent mappings explosion(防止mapping字段激增)



在索引中定义太多字段是导致映射爆炸的条件，它会导致内存错误和困难情况从中恢复。
这个问题可能比预期的更常见。例如，考虑一种情况，其中插入的每一个新文档都引入新字段。
这在动态映射中很常见。每当文档包含新字段时，这些字段将最终出现在索引的映射中。
对于少量的数据来说，这并不令人担忧，但随着映射的发展，它可能会成为一个问题。
以下设置允许您限制可以手动或动态创建的字段映射的数量，以防止不良文档导致映射爆炸

**index.mapping.total_fields.limit**

一个索引中的字段/属性的最大数量，默认为1000.

**index.mapping.depth.limit**

一个字段/属性的最大深度，用于控制内部对象的数量。例如，如果所有的字段/属性都定义为root对象级别，那么深度就为1。如果有一个对象映射，那么深度为2。默认值为20

**index.mapping.nested_fields.limit**

一个索引中嵌套字段/属性的最大值，默认为50。用100个嵌套字段索引1个文档实际上相当于索引了101个文档，因为每个嵌套文档都会作为单独的隐藏文档被索引


### Dynamic mapping(Dynamic mapping)

字段和映射在被使用前是不需要定义的。可以通过索引一个文档来自动添加新的映射和字段名。可以向顶层映射类型和内部对象以及嵌套字段中添加新的字段。
通过配置动态映射规则来定制映射，用于新的类型和字段

### Explicit mappings(显式映射)

你可以在创建索引的时候创建映射类型和字段映射，并且你可以通过PUT mapping API添加映射类型和字段到一个已经存在的索引中

### Updating existing mappings(更新已存在的映射)

除了已经存在的文档之外，已经存在的类型和字段映射也不能被更新。改变映射意味着让已经索引的文档失效。相反，你应该使用正确的映射以及重新索引数据来创建新的索引

### Fields are shared across mapping types(映射类型中共享字段)

映射类型用于给字段分组，但是每个映射类型中的字段不是相互独立的。以下情况下的字段具有相同的映射

- 相同名称
- 在同一索引中
- 不同的映射类型
- 映射到同一字段内部

如果title字段同时存在于user和blogpost映射类型中，那么title字段在每个类型中都有相同的映射。除了copy_to, dynamic, enabled, ignore_above, include_in_all, 和 properties，这些属性在每个字段中可能会有不同的设置。
通常，具有相同名称的字段也包含同样的数据类型，所以具有相同的映射不是问题。通过选择更多的描述名称能够解决冲突，如user_title 和blog_title


### Example mapping(映射例子)

~~~
PUT my_index                                            (1)
{
  "mappings": {
    "user": {  (2)
      "_all":       { "enabled": false  },  (3)
      "properties": {(4)
        "title":    { "type": "text"  },(5)
        "name":     { "type": "text"  },(6)
        "age":      { "type": "integer" }(7)
      }
    },
    "blogpost": {(8)
      "_all":       { "enabled": false  },(9)
      "properties": {(10)
        "title":    { "type": "text"  },(11)
        "body":     { "type": "text"  },(12)
        "user_id":  {
          "type":   "keyword"(13)
        },
        "created":  {
          "type":   "date",(14)
          "format": "strict_date_optional_time||epoch_millis"
        }
      }
    }
  }
}
~~~


### Removal of mapping types(删除映射类型)

> 在以后创建的指标可能Elasticsearch 6.0.0只包含一个单一的映射类型。创建于5的指标。X多映射类型将继续发挥在Elasticsearch 6。x映射类型将在Elasticsearch 7.0.0完全去除。


