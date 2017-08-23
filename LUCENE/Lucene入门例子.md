## Lucene 入门例子

### 创建索引

~~~java
 Analyzer analyzer = new StandardAnalyzer();

        Directory directory = FSDirectory.open(Paths.get("/Users/admin/lucene"));

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter iwriter = new IndexWriter(directory, config);

        Document doc = new Document();

        String text = "This is the text to be indexed.";

        doc.add(new Field("fieldname", text, TextField.TYPE_STORED));

        iwriter.addDocument(doc);

        iwriter.close();

~~~

### 查询索引

~~~java
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
~~~

