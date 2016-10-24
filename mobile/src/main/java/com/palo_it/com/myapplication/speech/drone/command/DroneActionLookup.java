package com.palo_it.com.myapplication.speech.drone.command;

import android.app.Activity;
import android.util.Log;

import android.util.Pair;
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
import java.util.Arrays;
import java.util.List;

public class DroneActionLookup implements VoiceActionCommand {
    private static final String TAG = "DroneActionLookup";
    private final OntologySearcher ontology;

    private DroneExecutor executor;
    private SoundsLikeWordMatcher ontologyMatcher;
    private Activity activity;
    private String name = "";

    public DroneActionLookup(Activity activity, DroneExecutor executor, OntologySearcher ontology) {
        this.activity = activity;
        this.executor = executor;
        this.ontology = ontology;
        List<String> allWords = new ArrayList<>();
        for (String command : ontology.getCommands()) {
            String[] splitted = command.split(";");
            for (String action : splitted) {
                //action = removeStopWords(action);
//                WordList wordList = new WordList(action);
//                String[] allOntoWords = wordList.getWords();
//                allWords.addAll(Arrays.asList(allOntoWords));
                allWords.add(action.trim());
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
        List<Pair<String, String>> actions = new ArrayList<>();
//        for (String word : heardWords) {
        //String noStopWords = removeStopWords(heardWord);


        List<String> separators = new ArrayList<>();
        // Get list of separators from the ontology
        //separators = getSeparatorsFromOntology();
//        separators.add("et");
//        separators.add("puis");
//        separators.add("ensuite");
//        separators.add("enfin");
//        separators.add("avant");
        // Use the separator in order to split the heard sentence and get the list of orders

        List<String> separatedWords = Arrays.asList(heard.getWords());
        if (heardWord.startsWith("je m'appelle")) {
            this.name = heard.getStringAfter(separatedWords.indexOf("m'appelle"));
            heardWord = heardWord.toLowerCase().replace(name.trim().toLowerCase(), "").trim();
        }
        List<String> orders = getOrders(separators, heardWord);
        for (String order : orders) {
            System.out.println("order:" + order);
            if (ontologyMatcher.isIn(order)) {
                Pair<String, String> apiAction = ontology.getApi(order);
                Log.d(TAG, String.format("Found action: %s , thanks to %s", apiAction, full ? "FULL" : "PARTIAL"));
                actions.add(apiAction);
            }
        }


//        }

        if (actions.size() > 0) {
            String toSay = "L'action du robot est: " + actions.get(0);
            Log.d(TAG, toSay);
            success = true;
            executor.doAction(actions.get(0), name);
        }
        return success;
    }

    public List<String> getOrders(List<String> separators, String heardWord) {
        List<String> orders = new ArrayList<String>();
        boolean exist = false;
        for (int i = 0; i < separators.size(); i++) {
            if (heardWord.contains(separators.get(i))) {
                exist = true;
            }
        }
        if (!exist) {
            orders.add(heardWord.trim());
            return orders;
        } else {

            for (int i = 0; i < separators.size(); i++) {
                if (heardWord.contains(separators.get(i))) {
                    String[] splitted = heardWord.split(separators.get(i));
                    separators.remove(i);
                    for (String heardPart : splitted) {
                        orders.addAll(getOrders(separators, heardPart));
                    }
                }
            }
            return orders;
        }
    }
}
