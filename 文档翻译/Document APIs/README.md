## 文档APIs

原文地址: [Document APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs.html)

该部分以一个比较简短的数据复制模型开始,接下来会详细的描述下面的CURL APIs.

### 单文档 APIs

- [Index API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html)
- [Get API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get.html)
- [Delete API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete.html)
- [Update API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html)

### 多文档APIs

- [Multi Get API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html)
- [Bulk API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html)
- [Delete By Query API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html)
- [Update By Query API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update-by-query.html)
- [Reindex API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html)

> ![](https://www.elastic.co/guide/en/elasticsearch/reference/current/images/icons/note.png) 所有的CRUD APIs都是单文档APIs. 索引参数
> 会接收一个单索引的名称或者别名.