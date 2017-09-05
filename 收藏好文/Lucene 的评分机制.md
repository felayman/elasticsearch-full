
## lucene 的评分机制

elasticsearch是基于lucene的，所以他的评分机制也是基于lucene的。评分就是我们搜索的短语和索引中每篇文档的相关度打分。
如果没有干预评分算法的时候，每次查询，lucene会基于一个评分算法来计算所有文档和搜索语句的相关评分。
使用lucene的评分机制基本能够把最符合用户需要的搜索放在最前面。
当然有的时候，我们可能想要自定义评分算法，这个就和lucene的评分算法没有什么关系了。当然，我们大多数应该还是会根据自己的需求，来调整lucene本身的算法。

## lucene的评分公式
lucene的评分是叫做TF/IDF算法，基本意思就是词频算法。
根据分词词库，所有的文档在建立索引的时候进行分词划分。进行搜索的时候，也对搜索的短语进行分词划分。
TF代表分词项在文档中出现的次数（term frequency），IDF代表分词项在多少个文档中出现（inverse document frequency）。

lucene的算法简单来说就是将搜索的短语进行分词得出分词项，每个分词项和每个索引中的文档根据TF/IDF进行词频出现的评分计算。
然后每个分词项的得分相加，就是这个搜索对应的文档得分。

![评分公式](http://dl2.iteye.com/upload/attachment/0066/9775/f5df5770-8ce5-3d59-ad02-ba691b179eca.png?_=4860134)

这个评分公式有6个部分组成

- coord(q,d) 评分因子，基于文档中出现查询项的个数。越多的查询项在一个文档中，说明文档的匹配程度越高。
- queryNorm(q)查询的标准查询
- tf(t in d) 指项t在文档d中出现的次数frequency。具体值为次数的开根号。
- idf(t) 反转文档频率, 出现项t的文档数docFreq
- t.getBoost 查询时候查询项加权
- norm(t,d) 长度相关的加权因子


**coord(q, d)**

这个评分因子的计算公式是：

~~~java
public float coord(int overlap, int maxOverlap) {
    return overlap / (float)maxOverlap;
}
~~~

- overlap: 文档中命中检索的个数
- maxOverlap: 检索条件的个数

比如检索"english book"， 现在有一个文档是"this is an chinese book"。
那么，这个搜索对应这个文档的overlap为1（因为匹配了book），而maxOverlap为2（因为检索条件有两个book和english）。
最后得到的这个搜索对应这个文档的coord值为0.5。

**queryNorm(q)**

这个因素对所有文档都是一样的值，所以它不影响排序结果。比如如果我们希望所有文档的评分大一点，那么我们就需要设置这个值。

~~~java
public float queryNorm(float sumOfSquaredWeights) {
    return (float)(1.0 / Math.sqrt(sumOfSquaredWeights));
}
~~~


**tf(t in d)**

项t在文档d中出现的次数

~~~java
public float tf(float freq) {
    return (float)Math.sqrt(freq);
}
~~~

比如有个文档叫做"this is book about chinese book"， 我的搜索项为"book"，那么这个搜索项对应文档的freq就为2，那么tf值就为根号2，即1.4142135

**idf**

~~~java
public float idf(long docFreq, long numDocs) {
    return (float)(Math.log(numDocs/(double)(docFreq+1)) + 1.0);
}
~~~

这里的两个值解释下

- docFreq 指的是项出现的文档数，就是有多少个文档符合这个搜索
- numDocs 指的是索引中有多少个文档。

我在用es实际看这里的时候遇到一个问题，numDocs数和实际的文档数不一致，最后弄明白了，这里的numDocs指的是分片的文档数据，而不是所有分片的文档数。
所以使用es分析这个公式的时候，最好将分片数设置为1。

比如我现在有三个文档，分别为:

- this book is about english
- this book is about chinese
- this book is about japan

我要搜索的词语是"chinese"，那么对第二篇文档来说，docFreq值就是1，因为只有一个文档符合这个搜索，而numDocs就是3。最后算出idf的值是:

~~~java
(float)(Math.log(numDocs/(double)(docFreq+1)) + 1.0) = ln(3/(1+1)) + 1 = ln(1.5) + 1 = 0.40546510810816 + 1 = 1.40546510810816
~~~

**t.getBoost**

查询时期项t的加权，这个就是一个影响值，比如我希望匹配chinese的权重更高，就可以把它的boost设置为2

**norm(t,d)**

这个项是长度的加权因子，目的是为了将同样匹配的文档，比较短的放比较前面。
比如两个文档:

- chinese
- chinese book

我搜索chinese的时候，第一个文档会放比较前面。因为它更符合"完全匹配"。


~~~java
norm(t,d) = doc.getBoost()· lengthNorm· ∏ f.getBoost()

public float lengthNorm(FieldInvertState state) {
    final int numTerms;
    if (discountOverlaps)
        numTerms = state.getLength() - state.getNumOverlap();
    else
        numTerms = state.getLength();
    return state.getBoost() * ((float) (1.0 / Math.sqrt(numTerms)));
}
~~~

这里的doc.getBoost表示文档的权重，f.getBoost表示字段的权重，如果这两个都设置为1，那么nor(t,d)就和lengthNorm一样的值。

比如我现在有一个文档:

- chinese book

搜索的词语为chinese， 那么numTerms为2，lengthNorm的值为 1/sqrt(2) = 0.71428571428571。

但是非常遗憾，如果你使用explain去查看es的时候，发现lengthNorm显示的只有0.625。
这个官方给出的原因是精度问题，norm在存储的时候会进行压缩，查询的时候进行解压，而这个解压是不可逆的，即decode(encode(0.714)) = 0.625。

示例
es中可以使用_explain接口进行评分解释查看。

比如现在我的文档为：

- chinese book

搜索词为：

~~~java
{
  "query": {
    "match": {
      "content": "chinese"
    }
  }
}
~~~

explain得到的结果为：

~~~json
{
    "_index": "scoretest",
    "_type": "test",
    "_id": "2",
    "matched": true,
    "explanation": {
        "value": 0.8784157,
        "description": "weight(content:chinese in 1) [PerFieldSimilarity], result of:",
        "details": [
            {
                "value": 0.8784157,
                "description": "fieldWeight in 1, product of:",
                "details": [
                    {
                        "value": 1,
                        "description": "tf(freq=1.0), with freq of:",
                        "details": [
                            {
                                "value": 1,
                                "description": "termFreq=1.0"
                            }
                        ]
                    },
                    {
                        "value": 1.4054651,
                        "description": "idf(docFreq=1, maxDocs=3)"
                    },
                    {
                        "value": 0.625,
                        "description": "fieldNorm(doc=1)"
                    }
                ]
            }
        ]
    }
}
~~~

看到这篇文档的总得分为 0.8784157

tf(t in d): 1

idf: ln(3/(1+1)) + 1 = 1.4054651

norm(t,d): decode(encode(1/sqrt(2))) = 0.625

总分: 1.4054651 * 0.625 = 0.8784157

原文地址:[lucene 的评分机制](http://www.cnblogs.com/yjf512/p/4860134.html)

## 参考(建议看看)
- [打分策略详解与explain手把手计算](http://blog.csdn.net/molong1208/article/details/50623948)