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
| Segments File|segments_N|Stores information about a commit point|
| Lock File|write.lock|The Write lock prevents multiple IndexWriters from writing to the same file|
| Segment Info|.si|Stores metadata about a segment|
| Compound File|.cfs, .cfe|An optional "virtual" file consisting of all the other index files for systems that frequently run out of file handles.|
| Fields|.fnm|Stores information about the fields|
| Field Index|.fdx|Contains pointers to field data|
| Field Data|.fdt|The stored fields for documents|
| Term Dictionary|.tim|The term dictionary, stores term info|
| Term Index|.tip|The index into the Term Dictionary|
| Frequencies|.doc|Contains the list of docs which contain each term along with frequency|
| Positions|.pos|Stores position information about where a term occurs in the index|
| Payloads|.pay|Stores additional per-position metadata information such as character offsets and user payloads|
| Norms|.nvd, .nvm|Encodes length and boost factors for docs and fields|
| Per-Document Values|.dvd, .dvm|Encodes additional scoring factors or other per-document information.|
| Term Vector Index|.tvx|Stores offset into the document data file|
| Term Vector Documents|.tvd|Contains information about each document that has term vectors|
| Term Vector Fields|.tvf|The field level info about term vectors|
| Live Documents|.liv|Info about what files are live|
| Point values|男|Holds indexed points, if any|


### 参考

 - [File Formats](https://lucene.apache.org/core/6_6_0/core/org/apache/lucene/codecs/lucene62/package-summary.html#package.description)
 - [ lucene4.5源码分析系列：lucene默认索引的文件格式-总述](http://blog.csdn.net/liweisnake/article/details/10956645)