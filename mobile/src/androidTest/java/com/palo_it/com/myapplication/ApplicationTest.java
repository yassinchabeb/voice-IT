package com.palo_it.com.myapplication;

import android.app.Application;
import android.test.ApplicationTestCase;
import com.palo_it.com.myapplication.text.SemanticParserMatcher;
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
        SemanticParserMatcher matcher = new SemanticParserMatcher(getContext().getAssets().open("sumonto.owl"));
        matcher.getApi("arrête");
    }
}