package org.org.apache.lucene.api.demo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-11-23
 */
public class IndexDemo {

    public static void main(String []args) throws IOException, InterruptedException {



        Analyzer analyzer = new StandardAnalyzer();



        Directory directory = FSDirectory.open(Paths.get("/Users/admin/github/elasticsearch-full/index/"));

        DirectoryReader ireader = DirectoryReader.open(directory);

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter iwriter = new IndexWriter(directory, config);

        System.out.println(ireader.getDocCount("field"));
        System.out.println(ireader.getSumTotalTermFreq("field"));


            System.exit(-1);
            Document doc = new Document();
            String text = "文档编号在每个不同的段中都是一个唯一的值.它们的值在更大的上下文中使用之前必须进行转换(比如从段上下文到分片上下文).标准的手法是分配给每个段一个数值范围,然后这些段会使用基于这些范围的数值.为了让每个段的文档编号变成一个外部可用的值,段将会添加一个基本文档编号.为了将一个外部可用的值转换回某个指定段的值,该段将会根据外部的值在某个段内的数值范围来进行识别,并且会减去段的基本文档编号.例如两个有5篇文档的段被提交,因此第一个段的基本文档编号为0,第二个段的基本文档编号为5,则第二个段中的第三个文档对外的编号为8";
            doc.add(new Field("field",text, TextField.TYPE_STORED));
            iwriter.deleteDocuments(new Term("文档"));
            iwriter.forceMerge(2);
            iwriter.close();
    }
}
