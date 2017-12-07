package org.elasticsearch.api.demo.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-23
 */
public class SampleIndexDemo {

    public static void main(String []args) throws IOException {

//        Analyzer analyzer = new StandardAnalyzer();
//
//        Directory directory = FSDirectory.open(Paths.get("/Users/admin/lucene"));
//
//        IndexWriterConfig config = new IndexWriterConfig(analyzer);
//
//        IndexWriter iwriter = new IndexWriter(directory, config);
//
//        Document doc = new Document();
//
//        String text = "This is the text to be indexed.";
//
//        doc.add(new Field("fieldname", text, TextField.TYPE_STORED));
//
//        iwriter.addDocument(doc);
//
//        iwriter.close();

    }
}
