package org.elasticsearch.api.demo.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LegacyLongField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.apache.lucene.document.TextField.TYPE_STORED;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-10
 */
public class CreateIndexDemo {

    @Test
    public void test() throws Exception {
        Path path = FileSystems.getDefault().getPath("", "index");
        Directory directory = FSDirectory.open(path);
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer).setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        Document document = new Document();
        document.add(new LegacyLongField("id", 5499, Field.Store.YES));
        document.add(new Field("title", "小米6", TYPE_STORED));
        document.add(new Field("sellPoint", "骁龙835，6G内存，双摄！", TYPE_STORED));
        document.
        indexWriter.addDocument(document);
        indexWriter.commit();
        indexWriter.close();
    }
}
