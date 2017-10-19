package org.elasticsearch.api.demo.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

/**
 * lucene官方例子
 * @see <a href='https://lucene.apache.org/core/6_6_0/demo/src-html/org/apache/lucene/demo/IndexFiles.html'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-08-23
 */
public class IndexFiles {

    private IndexFiles() {}

    public static void main(String[] args) {

        String indexPath = "index";
        String docsPath = "/Users/admin/github/elasticsearch-java-api/src/test/java/org/visualchina/elasticsearch/api/";
        boolean create = true;

        final Path docDir = Paths.get(docsPath);
        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            if (create) {
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docDir);
            writer.close();
            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }

    }

    static void indexDocs(final IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException ignore) {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            Document doc = new Document();
            Field pathField = new StringField("path", file.toString(), Field.Store.YES);
            doc.add(pathField);
            doc.add(new LongPoint("modified", lastModified));
            doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
            if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                System.out.println("adding " + file);
                writer.addDocument(doc);
                System.out.println("updating " + file);
                writer.updateDocument(new Term("path", file.toString()), doc);
            }
        }
    }

}
