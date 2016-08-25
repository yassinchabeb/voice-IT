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
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;

public class PhoneticAnalyzer extends StopwordAnalyzerBase {

    public PhoneticAnalyzer() {
        super(FrenchAnalyzer.getDefaultStopSet());
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        TokenFilter result = new StandardFilter(source);
        result = new LowerCaseFilter(result);
        result = new BeiderMorseFilter(result, new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, false));
        return new TokenStreamComponents(source, result);
    }
}
