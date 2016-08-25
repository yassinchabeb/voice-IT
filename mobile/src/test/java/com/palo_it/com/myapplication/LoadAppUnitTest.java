package com.palo_it.com.myapplication;

import com.palo_it.com.myapplication.text.PhoneticAnalyzer;
import com.palo_it.com.myapplication.text.SemanticParserMatcher;
import com.palo_it.com.myapplication.text.SynonymsUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class LoadAppUnitTest {

    private RAMDirectory directory;

    @Before
    public void setup() {
        directory = new RAMDirectory();
        IndexWriter writer;
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
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals("avance", SynonymsUtil.getActionSynonymFor("bouge"));
        assertEquals("avance", SynonymsUtil.getActionSynonymFor("marche"));

        assertEquals("recul", SynonymsUtil.getActionSynonymFor("reculez"));
    }
}