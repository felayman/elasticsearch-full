## Lucene文件索引文件格式

> 本文是针对[Lucene 6.2 file format](https://lucene.apache.org/core/6_6_0/core/org/apache/lucene/codecs/lucene62/package-summary.html#package.description)的总结,参考了其他文章

### 版本

    Elasticsearch 5.5.0
    Lucene 6_6_0


### Lucene 扩展文件说明

- segments_N  Stores information about a commit point
- write.lock  The Write lock prevents multiple IndexWriters from writing to the same file.
- .si Stores metadata about a segment
- .cfs, .cfe An optional "virtual" file consisting of all the other index files for systems that frequently run out of file handles.
- .fnm Stores information about the fields
- .fdx Contains pointers to field data
- .fdt The stored fields for documents
- .tim The term dictionary, stores term info
- .tip The index into the Term Dictionary
- .doc Contains the list of docs which contain each term along with frequency
- .pos Stores position information about where a term occurs in the index
- .pay Stores additional per-position metadata information such as character offsets and user payloads
- .nvd, .nvm Encodes length and boost factors for docs and fields
- .dvd, .dvm Encodes additional scoring factors or other per-document information.
- .tvx Stores offset into the document data file
- .tvd Contains information about each document that has term vectors
- .tvf The field level info about term vectors
- .liv Info about what files are live
- .dii, .dim Holds indexed points, if any

针对上面的说明,之前有大神给出了逐个说明,见下图：

![索引文件的整体结构](http://img.blog.csdn.net/20131015081355671?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbGl3ZWlzbmFrZQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

由于版本问题,可能发生了一些变化,下表是对上述进行了补充:

| 文件名称|扩展名|描述|
| -|-|-|
| Segments File|segments_N|存储段文件的提交点信息|
| Lock File|write.lock|文件锁，保证任何时刻只有一个线程可以写入索引|
| Segment Info|.si|存储每个段文件的元数据信息|
| Compound File|.cfs, .cfe|一个可选的虚拟文件,考虑到系统其它的索引文件频繁的用尽文件句柄.|
| Fields|.fnm|存储域文件的信息|
| Field Index|.fdx|存储域数据的指针|
| Field Data|.fdt|存储所有文档的字段信息|
| Term Dictionary|.tim|term字典，存储term信息|
| Term Index|.tip|term字典的索引文件|
| Frequencies|.doc|词频文件，包含文档列表以及每一个term和其词频|
| Positions|.pos|位置信息，存储每个term，在索引中的准确位置|
| Payloads|.pay|Stores additional per-position metadata information such as character offsets and user payloads|
| Norms|.nvd, .nvm|存储文档和域的编码长度以及加权因子|
| Per-Document Values|.dvd, .dvm|编码除外的额外的打分因素|
| Term Vector Index|.tvx|term向量索引，存储term在文档中的偏移距离|
| Term Vector Documents|.tvd|包含每个文档向量的信息|
| Term Vector Fields|.tvf|存储filed级别的向量信息|
| Live Documents|.liv|文件存活信息|
| Point values|.dii, .dim|持有索引指针|
| Deleted Documents|.del|存储索引删除文件的信息|


### 索引结果说明

每个索引段都会包含如下内容：

- Segment info.  包含一个索引段的元信息,比如文档数
- Field names   包含一系列索引中的域名称
- Stored Field values   存储每个文档的字段键值对信息,属性为域名称,它们用来存储文档的辅助信息.比如标题,链接或者一个访问数据库的唯一标识.存储的域会在每次查询的时候返回.
- Term dictionary  包含所有文档的所有索引字段中使用的所有词项的字典。字典还包含包含词项的文档数量，以及词项频率和邻近数据的指针。
- Term Frequency data  词频数据信息
- Term Proximity data  词项位置数据信息
- Normalization factors 域的归一化因子
- Term Vectors  词向量信息
- Per-document values Like stored values, these are also keyed by document number, but are generally intended to be loaded into main memory for fast access. Whereas stored values are generally intended for summary results from searches, per-document values are useful for things like scoring factors
- Live documents
- Point values

### 参考

 - [File Formats](https://lucene.apache.org/core/6_6_0/core/org/apache/lucene/codecs/lucene62/package-summary.html#package.description)
 - [ lucene4.5源码分析系列：lucene默认索引的文件格式-总述](http://blog.csdn.net/liweisnake/article/details/10956645)