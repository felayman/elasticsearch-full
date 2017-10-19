/**
 * lucenu相关的demo
 * 如果想深入了解Elasticsearch,了解lucene是必不可少的
 * lucenu的应用
 * 1.文本搜索
 * 2.网站信息检索
 * 3.数据库的搜索
 * lucenu的包结构说明
 * org.apache.lucene                                                ：lucene的核心包
 * org.apache.lucene.analysis                                ：文本分析
 * org.apache.lucene.analysis.standard              ：标准分析
 * org.apache.lucene.analysis.tokenattributes  ：token属性
 * org.apache.lucene.codecs                                    ：自定义索引结构
 * org.apache.lucene.codecs.blocktree                 ：BlockTree terms dictionary.
 * org.apache.lucene.codecs.compressing           ：压缩模块
 * org.apache.lucene.codecs.lucene50                  ：lucene5.0版本的编码解码
 * org.apache.lucene.codecs.lucene53                   ：lucene5.3版本的编码解码
 * org.apache.lucene.codecs.perfield                      ：字段格式解析
 * org.apache.lucene.document                                ：文档索引和查询的逻辑
 * org.apache.lucene.geo                                             ：geo模块
 * org.apache.lucene.index                                          ：维护和访问索引
 * org.apache.lucene.search                                        ：查询索引
* org.apache.lucene.search.similarities                  ：相似度算法
 * org.apache.lucene.search.spans                            ：span结构
 * org.apache.lucene.store                                           ：二进制I/O存储
 * org.apache.lucene.util                                               ：工具类
 * org.apache.lucene.util.automaton                         ：对正则表达式的有限状态自动机
 * org.apache.lucene.util.bkd                                       ：Block KD-tree, implementing the generic spatial data structure described in this paper
 * org.apache.lucene.util.fst                                          ：状态转换
 * org.apache.lucene.util.graph                                    ：图表处理
 * org.apache.lucene.util.mutable                               ：对象包装
 * org.apache.lucene.util.packed                                 ：打包整型数组以及流数据
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-10
 */
package org.elasticsearch.api.demo.lucene;