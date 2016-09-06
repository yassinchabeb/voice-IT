/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package com.palo_it.com.myapplication.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.palo_it.com.myapplication.R;
import com.palo_it.com.myapplication.speech.activation.SpeechActivationService;
import root.gast.speech.SpeechRecognitionUtil;
import root.gast.speech.SpeechRecognizingAndSpeakingActivity;

import java.util.Arrays;
import java.util.List;

public class WakeUpWordActivity extends SpeechRecognizingAndSpeakingActivity {

    private static final String TAG = "WakeUpWordActivity";
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_speech);
        TextView mText = (TextView) findViewById(R.id.spokenText);
        mText.setText(getString(R.string.welcome));
        mText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSuccessfulInit(TextToSpeech tts) {
        super.onSuccessfulInit(tts);
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
        Intent intent = SpeechActivationService.makeStartServiceIntent(this);
        WakeUpWordActivity.this.startService(intent);
        Log.d(TAG, "started service for " +
                Arrays.toString(getResources().getStringArray(R.array.voiceaction_wakeupphrases)));
    }

    @Override
    protected void receiveWhatWasHeard(List<String> heard, float[] confidenceScores) {

    }

    @Override
    protected void recognitionFailure(int errorCode) {
        AlertDialog a =
                new AlertDialog.Builder(this).setTitle("Error").setMessage(SpeechRecognitionUtil.diagnoseErrorCode
                        (errorCode)).setPositiveButton("Ok", null).create();
        a.show();
    }
}
