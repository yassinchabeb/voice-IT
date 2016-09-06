package com.palo_it.com.myapplication.speech.drone.command;

import android.app.Activity;
import android.util.Log;
import com.palo_it.com.myapplication.speech.text.match.SoundsLikeWordMatcher;
import com.palo_it.com.myapplication.speech.voiceaction.DroneExecutor;
import com.palo_it.com.myapplication.speech.voiceaction.VoiceActionCommand;
import com.palo_it.com.myapplication.text.OntologySearcher;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import root.gast.speech.text.WordList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DroneActionLookup implements VoiceActionCommand {
    private static final String TAG = "DroneActionLookup";
    private final OntologySearcher ontology;

    private DroneExecutor executor;
    private SoundsLikeWordMatcher ontologyMatcher;
    private Activity activity;

    public DroneActionLookup(Activity activity, DroneExecutor executor, OntologySearcher ontology) {
        this.activity = activity;
        this.executor = executor;
        this.ontology = ontology;
        List<String> allWords = new ArrayList<>();
        for (String command : ontology.getCommands()) {
            String[] splitted = command.split(";");
            for (String action : splitted) {
                action = removeStopWords(action);
//                WordList wordList = new WordList(action);
//                String[] allOntoWords = wordList.getWords();
//                allWords.addAll(Arrays.asList(allOntoWords));
                allWords.add(action);
            }
        }
        this.ontologyMatcher = new SoundsLikeWordMatcher(allWords.toArray(new String[0]));
    }

    private String removeStopWords(String action) {
        StringBuilder builder = new StringBuilder();
        try {
            FrenchAnalyzer frenchAnalyzer = new FrenchAnalyzer();
            TokenStream tokenStream = frenchAnalyzer.tokenStream("contents", action);
            CharTermAttribute attribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String term = attribute.toString();
                builder.append(term + " ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public boolean interpret(WordList heard, float[] confidence, boolean full) {
        boolean success = false;
//        String[] heardWords = heard.getWords();
        String heardWord = heard.getSource();
        List<String> actions = new ArrayList<>();
//        for (String word : heardWords) {
        String noStopWords = removeStopWords(heardWord);
        if (ontologyMatcher.isIn(noStopWords)) {
            // Get apiCall from Ontology...
            String apiAction = ontology.getApi(heardWord);
            Log.d(TAG, String.format("Found action: %s , thanks to %s", apiAction, full ? "FULL" : "PARTIAL"));
            actions.add(apiAction);
        }
//        }

        if (actions.size() > 0) {
            String toSay = "L'action du robot est: " + actions.get(0);
            Log.d(TAG, toSay);
            success = true;
            executor.doAction(actions.get(0));
        }
        return success;
    }
}
