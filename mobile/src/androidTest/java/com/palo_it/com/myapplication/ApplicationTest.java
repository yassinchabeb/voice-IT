package com.palo_it.com.myapplication;

import android.app.Application;
import android.test.ApplicationTestCase;
import com.palo_it.com.myapplication.drone.JSDrone;
import com.palo_it.com.myapplication.text.OntologySearcher;
import org.junit.Test;

import java.io.IOException;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Test
    public void testSemanticStuff() throws IOException {
        OntologySearcher matcher = new OntologySearcher(getContext().getAssets().open("sumonto.owl"));
        assertEquals(JSDrone.ACTIONS.STOP, JSDrone.ACTIONS.valueOf(matcher.getApi("arrête").toUpperCase()));
        assertEquals(JSDrone.ACTIONS.FORWARD, JSDrone.ACTIONS.valueOf(matcher.getApi("avance").toUpperCase()));
        assertEquals(JSDrone.ACTIONS.RIGHT, JSDrone.ACTIONS.valueOf(matcher.getApi("tourne").toUpperCase()));
        assertEquals(JSDrone.ACTIONS.LEFT, JSDrone.ACTIONS.valueOf(matcher.getApi("tourne à gauche").toUpperCase()));
        assertEquals(JSDrone.ACTIONS.BACKWARD, JSDrone.ACTIONS.valueOf(matcher.getApi("recul").toUpperCase()));
    }

}