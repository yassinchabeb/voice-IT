# VOICE-IT
Playing around with Android's Speech-to-text &amp; Text-to-Speech; Setting up a Wake-up-word other than OK Google, and trying to match converted text to a given Ontology


## WHY, OH WHY
Well, one of our clients wanted a Virtual Assistant hands free to handle orders like 'Send me a bunch of PRODUCT-X next week', 'Send the same as last week' or 'Wheres my stuff?!'
Kind of like [Amazon's ECHO](https://www.amazon.com/Amazon-Echo-Bluetooth-Speaker-with-WiFi-Alexa/dp/B00X4WHP5E) or [Google HOME](https://home.google.com) 

Our idea was to create a 3D printed object which would hide a Raspberry PI inside who would handle voice interactions and eventually make REST calls to
 CRM services or whatever and then re-translate text to Speech.

As a first prototype, we set up an Android App that would do all of this, given that Android's API provides both Speech to Text an text to Speech out of the box,
there's a speaker and a microphone already integrated and we wouldn't have to deal with hardware stuff from the beginning.

## HOW!??!
So, we've created an Android app which has 2 steps/actions/screens: first an infinite Wake-up-word receiver (a la "OK GOOGLE")
and second, a command receiver which matches recognized text to a set of given actions.

For our small prototype, we set off to send out commands to a small [Drone](https://www.parrot.com/fr/minidrones/parrot-jumping-sumo#parrot-jumping-sumo)
in the form of "Hey robot -> do a long jump!/go forward a bit/turn right and spin!"

Actions were defined via an [Ontology File](https://en.wikipedia.org/wiki/Ontology), created via [Stanford's ontology project 'PROTEGE'](http://protege.stanford.edu/)
 
Once text is recognized via Android's Speech API, we (very basically) search for actions in the Ontology which in turn replies with an API method.
API method is then executed and our current implementation makes our drone move around!

### Links
* We refactored a lot of code to match what people had done in [GAST-LIB](https://github.com/gast-lib/gast-lib)
* [Speech to Text API](https://developer.android.com/reference/android/speech/package-summary.html)
* [Text to Speech API](https://developer.android.com/reference/android/speech/tts/package-summary.html)
* [Drone API](http://developer.parrot.com/docs/SDK3/)
* [Ontology file parser for Android](https://github.com/sbrunk/jena-android)


### A basic Architecture diagram
![Archi!](https://github.com/Palo-IT/voice-IT/blob/gastInstall/img/archi.jpeg)
