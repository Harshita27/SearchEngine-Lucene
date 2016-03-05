/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchenginelucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

/**
 *
 * @author Harshita
 *
 * The following tasks are performed: 1) Indexing the raw (un pre-processed)
 * CACM corpus using Lucene. 2) Building a list of (unique term, term_frequency)
 * pairs over the entire collection. Sort by frequency. 3) Performing search for
 * test queries and return the top 100 results for each query.
 */
public class LuceneSearchEngine {

    private static Analyzer sAnalyzer = new SimpleAnalyzer(Version.LUCENE_47);  // Instantiate the Simple Analyzer
    private IndexWriter writer;
    private ArrayList<File> queue = new ArrayList<File>();

    //Main Method    
    public static void main(String[] args) throws IOException {
        System.out.println("Enter the FULL path where the index will be created: (e.g. /Usr/index or c:\\temp\\index)");

        String indexLocation = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String s = br.readLine();
        LuceneSearchEngine indexer = null;
        try {
            indexLocation = s;
            indexer = new LuceneSearchEngine(s);

        } catch (Exception ex) {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }

        // ========================================================================
        // Ask the user to enter a valid location where the index should be created
        // ========================================================================
        while (!s.equalsIgnoreCase("q")) {
            try {
                System.out
                        .println("Enter the FULL path to add into the index (q=quit): (e.g. /home/mydir/docs or c:\\Users\\mydir\\docs)");
                System.out
                        .println("[Acceptable file types: .xml, .html, .html, .txt]");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }

                // =============================================================
                // Function call to create index
                // =============================================================
                indexer.indexFileOrDirectory(s);
            } catch (Exception e) {
                System.out.println("Error indexing " + s + " : "
                        + e.getMessage());
            }
        }

        indexer.closeIndex();

        // =========================================================
        // Fetch term frequencies and obtain query results
        // =========================================================
        getTermFrequencyPairs(indexLocation);                      // Get the frequencies for all the terms indexed.
        searchForQuery(indexLocation, s, br);                      // Search for queries.

    }

    // =========================================================
    // Function to fetch the term frequencies for all the terms
    // in the index.
    // =========================================================
    public static void getTermFrequencyPairs(String indexLocation) throws IOException {
        Map<String, Integer> termfrequency = new HashMap<String, Integer>();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
                indexLocation)));
        // Temporary location to store the interediate term frequency results
        PrintWriter writer_tf = new PrintWriter("..\\terf-frequency.csv");

        int docnum = reader.numDocs();
       // System.out.println("docnum:" + docnum);
        Fields fields1 = MultiFields.getFields(reader);
        for (String field : fields1) {
            Terms terms1 = fields1.terms("contents");
            TermsEnum termsEnum = terms1.iterator(null);
            int noWords = 0;

            while (termsEnum.next() != null) {
                noWords++;
                int count = 0;
                DocsEnum docsEnum = termsEnum.docs(null, null);
                int docIdEnum;
                //System.out.print("The term is->" + termsEnum.term().utf8ToString());
                while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                    count += docsEnum.freq();
                }
                //System.out.println("count:" + count);
                termfrequency.put(termsEnum.term().utf8ToString(), count);
            }
            System.out.println("Total Number of Words:" + noWords);
        }

        // =========================================================
        // Write the terms anf their frequencies in a file
        // =========================================================
        for (String key : termfrequency.keySet()) {
            writer_tf.print(key + ",");
            writer_tf.println(termfrequency.get(key));
        }
        writer_tf.close();

    }
    // =========================================================
    // Function to search the given queries in the index using
    // Lucene's searching libraries.
    // =========================================================

    public static void searchForQuery(String indexLocation, String s, BufferedReader br) throws IOException {

        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
                indexLocation)));
        IndexSearcher searcher = new IndexSearcher(reader);
        PrintWriter writer_query = new PrintWriter("..\\Query-1.csv");

        s = "";
        while (!s.equalsIgnoreCase("q")) {
            TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);

            try {
                System.out.println("Enter the search query (q=quit):");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    writer_query.close();
                    break;
                }

                Query q = new QueryParser(Version.LUCENE_47, "contents", sAnalyzer).parse(s);
                searcher.search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                // Write the results in a file
                System.out.println("Found " + hits.length + " hits.");
                for (int i = 0; i < hits.length; ++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    writer_query.println((i + 1) + "," + s + ", " + d.get("filename")
                            + "," + hits[i].score);
                }
                // 
                // Term termInstance = new Term("contents", s);
                // System.out.println("The term is:" + termInstance.toString());
                // long termFreq = reader.totalTermFreq(termInstance);
                //  long docCount = reader.docFreq(termInstance);
                // System.out.println(s + " Term Frequency " + termFreq
                //        + " - Document Frequency " + docCount);

            } catch (Exception e) {
                System.out.println("Error searching " + s + " : "
                        + e.getMessage());
                break;
            }
            writer_query.close();
        }

    }

    /**
     * Constructor
     *
     * @param indexDir the name of the folder in which the index should be
     * created
     * @throws java.io.IOException when exception creating index.
     */
    LuceneSearchEngine(String indexDir) throws IOException {

        FSDirectory dir = FSDirectory.open(new File(indexDir));

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,
                sAnalyzer);

        writer = new IndexWriter(dir, config);

    }

    /**
     * Indexes a file or directory
     *
     * @param fileName the name of a text file or a folder we wish to add to the
     * index
     * @throws java.io.IOException when exception /*
     * ************************************************************************
     * At the given IndexLocation, create a new Index using Lucene's
     * SimpleAnalyzer. 1) Read each file from the given location 2) For each
     * file cleanup the HTML markups using Jsoup 3) Obtain plain text and create
     * a TextField named "contents" 4) Write the indexer with the fields.
     */
    public void indexFileOrDirectory(String fileName) throws IOException {
        // ===================================================
        // gets the list of files in a folder (if user has submitted
        // the name of a folder) or gets a single file name (is user
        // has submitted only the file name)
        // ===================================================
        addFiles(new File(fileName));

        int originalNumDocs = writer.numDocs();
        for (File f : queue) {
            FileReader fr = null;
            StringReader strread = null;
            BufferedReader br = null;
            //  HTMLStripCharFilter htmlfilter = null;
            //HTMLStripCharFilter htmlfilter;
            try {
                Document doc = new Document();

                // ===================================================
                // add contents of file
                // ===================================================
                fr = new FileReader(f);

                // Read the file into BufferedReader 
                br = new BufferedReader(fr);
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                // Convert each Reader into a String of words
                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                // Parse the String using Jsoup to remove HTML markup and write 
                // to the StringReader.
                strread = new StringReader(Jsoup.parse(sb.toString()).text());
                //System.out.println("The plain text:::" + strread.toString());

                FieldType type = new FieldType();
                type.setIndexed(true);
                type.setStored(true);

                // Add the cleaned up text from Jsoup as a TextField named 'contents'
                doc.add(new TextField("contents", strread));
                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                doc.add(new StringField("filename", f.getName(),
                        Field.Store.YES));

                writer.addDocument(doc); // Write the index
                //System.out.println("The plain text:::" + strread.toString());

                System.out.println("Added: " + f);
                //  htmlfilter.close();
            } catch (Exception e) {
                System.out.println("Could not add: " + f);
            } finally {
                //htmlfilter.close();
                fr.close();

            }
        }

        int newNumDocs = writer.numDocs();
        System.out.println("");
        System.out.println("************************");
        System.out
                .println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");

        queue.clear();
    }

    private void addFiles(File file) {

        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            String filename = file.getName().toLowerCase();
            // ===================================================
            // Only index text files
            // ===================================================
            if (filename.endsWith(".htm") || filename.endsWith(".html")
                    || filename.endsWith(".xml") || filename.endsWith(".txt")) {
                queue.add(file);
            } else {
                System.out.println("Skipped " + filename);
            }
        }
    }

    /**
     * Close the index.
     *
     * @throws java.io.IOException when exception closing
     */
    public void closeIndex() throws IOException {
        writer.close();
    }

}
