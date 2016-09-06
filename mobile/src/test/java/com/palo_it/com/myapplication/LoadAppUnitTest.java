package com.palo_it.com.myapplication;

import com.palo_it.com.myapplication.drone.JSDrone;
import com.palo_it.com.myapplication.speech.drone.command.DroneActionLookup;
import com.palo_it.com.myapplication.text.OntologySearcher;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import root.gast.speech.text.WordList;

import java.util.Arrays;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class LoadAppUnitTest {

    @Test
    public void testDroneActionLookup() {
        OntologySearcher searcher = Mockito.mock(OntologySearcher.class);
        Mockito.when(searcher.getCommands()).thenReturn(Arrays.asList("tourne à gauche"));
        DroneActionLookup actionLookup = new DroneActionLookup(null, null, searcher);

        Assert.assertEquals(actionLookup.interpret(new WordList("tourner à gauche"), new float[]{}, true), JSDrone.ACTIONS
                .LEFT);
    }
}