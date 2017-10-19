package org.elasticsearch.api.demo.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-23
 */
public class SampleSearchDemo {
    public static void main(String []args) throws IOException, ParseException {


        Analyzer analyzer = new StandardAnalyzer();

        Directory directory = FSDirectory.open(Paths.get("/Users/admin/lucene"));

        DirectoryReader ireader = DirectoryReader.open(directory);

        IndexSearcher indexSearcher  = new IndexSearcher(ireader);

        QueryParser parser = new QueryParser("fieldname", analyzer);

        Query query = parser.parse("text");

        ScoreDoc[] hits = indexSearcher.search(query, 10, Sort.INDEXORDER).scoreDocs;

        for (int i = 0; i < hits.length; i++) {
            Document hitDoc = indexSearcher.doc(hits[i].doc);
            System.out.println(hitDoc.toString());
        }
        ireader.close();
        directory.close();

    }
}
