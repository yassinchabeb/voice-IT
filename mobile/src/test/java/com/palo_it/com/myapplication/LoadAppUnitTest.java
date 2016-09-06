package com.palo_it.com.myapplication;

import com.palo_it.com.myapplication.drone.JSDrone;
import com.palo_it.com.myapplication.speech.drone.command.DroneActionLookup;
import com.palo_it.com.myapplication.text.OntologySearcher;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import root.gast.speech.text.WordList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class LoadAppUnitTest {

    @Test
    public void testDroneActionLookup() {
        OntologySearcher searcher = Mockito.mock(OntologySearcher.class);
        Mockito.when(searcher.getCommands()).thenReturn(Arrays.asList("tourne à gauche"));
        DroneActionLookup actionLookup = new DroneActionLookup(null, null, searcher);

        List<String> separators = new ArrayList<String>();
        // Get list of separators from the ontology
        //separators = getSeparatorsFromOntology();
        separators.add("et");
        separators.add("puis");
        separators.add("ensuite");
        separators.add("enfin");
        separators.add("avant");
        Assert.assertEquals(actionLookup.getOrders(separators,"tourner à gauche et tourner à droite puis reculer").size(),3);
    }
}