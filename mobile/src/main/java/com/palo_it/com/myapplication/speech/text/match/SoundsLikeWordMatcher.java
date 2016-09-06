/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palo_it.com.myapplication.speech.text.match;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.bm.BeiderMorseEncoder;
import root.gast.speech.text.match.WordMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * encode strings using encoder
 *
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class SoundsLikeWordMatcher extends WordMatcher {
    protected static BeiderMorseEncoder encoder;

    static {
        encoder = new BeiderMorseEncoder();
    }

    public SoundsLikeWordMatcher(String... wordsIn) {
        this(Arrays.asList(wordsIn));
    }

    public SoundsLikeWordMatcher(List<String> wordsIn) {
        super(encode(wordsIn));
    }

    @Override
    public boolean isIn(String word) {
        return super.isIn(encode(word));
    }

    protected static List<String> encode(List<String> input) {
        List<String> encoded = new ArrayList<>();
        for (String in : input) {
            encoded.add(encode(in));
        }
        return encoded;
    }

    private static String encode(String in) {
        String encoded = in;
        try {
            encoded = encoder.encode(in);
        } catch (EncoderException e) {
            //for weird characters that encoder doesn't understand
            e.printStackTrace();
        }
        return encoded;
    }
}

    
