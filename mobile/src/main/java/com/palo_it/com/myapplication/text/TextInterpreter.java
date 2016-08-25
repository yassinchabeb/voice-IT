package com.palo_it.com.myapplication.text;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Base class to match recognized text to proper Actions... via an Ontology
 */
public class TextInterpreter {

    private static RAMDirectory directory;

    private static final TextInterpreter INSTANCE = new TextInterpreter();
    private static InputStream ontologyFile;
    private static SemanticParserMatcher parserMatcher;

//    static {
//        INSTANCE = new TextInterpreter();
//        directory = new RAMDirectory();
//        IndexWriter writer;
//        try {
//            IndexWriterConfig writerConfig = new IndexWriterConfig(new PhoneticAnalyzer())
//                    .setOpenMode(IndexWriterConfig.OpenMode.CREATE);
//            writer = new IndexWriter(directory, writerConfig);
//            Document avanceDoc = new Document();
//            avanceDoc.add(new TextField("contents", "avance", Field.Store.YES));
//            writer.addDocument(avanceDoc);
//            Document recouleDoc = new Document();
//            recouleDoc.add(new TextField("contents", "recul", Field.Store.YES));
//            writer.addDocument(recouleDoc);
//            Document tourneDoc = new Document();
//            tourneDoc.add(new TextField("contents", "tourne", Field.Store.YES));
//            writer.addDocument(tourneDoc);
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static TextInterpreter getInstance() {
        return INSTANCE;
    }

    public static TextInterpreter getInstance(InputStream file) {
        ontologyFile = file;
        parserMatcher = new SemanticParserMatcher(ontologyFile);
        return INSTANCE;
    }

    public String matchText(String text) {
        String result = parserMatcher.getApi(text.trim());
        if (result.equals(SemanticParserMatcher.UNKNOWN)) {
            return null;
        }
//        String result = null;
//        try {
//            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
//            TopDocs hits = searcher.search(new FuzzyQuery(new Term("contents", text)), 5);
//            if (hits.totalHits > 0) {
//                System.out.println("Found matching docs: " + hits.totalHits);
//                result = searcher.doc(hits.scoreDocs[0].doc).getField("contents").stringValue();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return result;
    }
}
