package com.palo_it.com.myapplication.text;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.CharsRef;

import java.io.IOException;
import java.io.StringReader;

public class SynonymsUtil {

    private static SynonymMap synonymMap;

    static {
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

    public static String getActionSynonymFor(String action) throws IOException {
        StringReader stringReader = new StringReader(action);
        StandardAnalyzer analyzer = new StandardAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream("action", stringReader);

        SynonymFilter synonymFilter = new SynonymFilter(tokenStream, synonymMap, false);
        synonymFilter.reset();
        StringBuilder builder = new StringBuilder();
        while (synonymFilter.incrementToken()) {
            String term = synonymFilter.getAttribute(CharTermAttribute.class).toString();
            if (!action.equals(term)) {
                builder.append(term);
            }
        }
        return builder.toString();
    }
}
