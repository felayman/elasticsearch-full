package org.elasticsearch.api.demo.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-23
 */
public class TermQueryDemo {
    public static void main(String []args) throws IOException {
        Analyzer analyzer = new StandardAnalyzer();

        Directory directory = FSDirectory.open(Paths.get("/Users/admin/lucene"));

        DirectoryReader ireader = DirectoryReader.open(directory);

        IndexSearcher indexSearcher  = new IndexSearcher(ireader);

        Term term = new Term("fieldname","国");
        Query query = new TermQuery(term);

        ScoreDoc[] hits = indexSearcher.search(query, 10, Sort.RELEVANCE).scoreDocs;


        for (int i = 0; i < hits.length; i++) {
            Document hitDoc = indexSearcher.doc(hits[i].doc);
            System.out.println(hitDoc.toString()+","+hits[i].score);
        }
        ireader.close();
        directory.close();


    }
}
