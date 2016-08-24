package com.palo_it.com.myapplication.text;

import org.apache.commons.codec.language.bm.NameType;
import org.apache.commons.codec.language.bm.PhoneticEngine;
import org.apache.commons.codec.language.bm.RuleType;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.phonetic.BeiderMorseFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.CharsRef;

import java.io.IOException;
import java.io.InputStream;

/**
 * Base class to match recognized text to proper Actions... via an Ontology
 */
public class TextInterpreter {

    private static RAMDirectory directory;

    private static final TextInterpreter INSTANCE;
    private static InputStream ontologyFile;

    static {
        INSTANCE = new TextInterpreter();
        directory = new RAMDirectory();
        IndexWriter writer = null;
        try {
            IndexWriterConfig writerConfig = new IndexWriterConfig(new PhoneticAnalyzer())
                    .setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            writer = new IndexWriter(directory, writerConfig);
            Document avanceDoc = new Document();
            avanceDoc.add(new TextField("contents", "avance", Field.Store.YES));
            writer.addDocument(avanceDoc);
            Document recouleDoc = new Document();
            recouleDoc.add(new TextField("contents", "recul", Field.Store.YES));
            writer.addDocument(recouleDoc);
            Document tourneDoc = new Document();
            tourneDoc.add(new TextField("contents", "tourne", Field.Store.YES));
            writer.addDocument(tourneDoc);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TextInterpreter getInstance() {
        return INSTANCE;
    }

    public static TextInterpreter getInstance(InputStream file) {
        ontologyFile = file;
        return INSTANCE;
    }

    public String phoneticSearch(String text) {
        SemanticParserMatcher p = new SemanticParserMatcher(ontologyFile);
        String apiResponse = p.getApi(text);

        String result = null;
        try {
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
            TopDocs hits = searcher.search(new FuzzyQuery(new Term("contents", text)), 5);
            if (hits.totalHits > 0) {
                System.out.println("Found matching docs: " + hits.totalHits);
                result = searcher.doc(hits.scoreDocs[0].doc).getField("contents").stringValue();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static class PhoneticAnalyzer extends StopwordAnalyzerBase {

        private SynonymMap synonymMap;

        public PhoneticAnalyzer() {
            super(FrenchAnalyzer.getDefaultStopSet());
            SynonymMap.Builder builder = new SynonymMap.Builder(true);
            builder.add(new CharsRef("reculez"), new CharsRef("recul"), true);
            builder.add(new CharsRef("reculer"), new CharsRef("recul"), true);

            builder.add(new CharsRef("avancer"), new CharsRef("avance"), true);
            builder.add(new CharsRef("avancez"), new CharsRef("avance"), true);
            builder.add(new CharsRef("bouge"), new CharsRef("avance"), true);
            builder.add(new CharsRef("bougez"), new CharsRef("avance"), true);
            builder.add(new CharsRef("marche"), new CharsRef("avance"), true);
            builder.add(new CharsRef("marchez"), new CharsRef("avance"), true);

            try {
                synonymMap = builder.build();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            Tokenizer source = new StandardTokenizer();
            TokenFilter result = new StandardFilter(source);
            result = new LowerCaseFilter(result);
            result = new BeiderMorseFilter(result, new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, false));
//            result = new SynonymFilter(result, synonymMap, false);
            return new TokenStreamComponents(source, result);
        }
    }

}
