# Owl voice assistant
An offline privacy first voice assistant

## Prerequisites

- mpd (Music player daemon) setup and listening on localhost:6600
  - in the future this will be configured with a config file


## Building

1. to build the application clone the repo
2. navigate to `app/src/main/resources` and add 3 files there
   1. file 1 is a your picovoice sdk key, this key should be in a file called `picovoicekey.txt`
   2. file 2 is the Rhino command intents these can be created from the picovoice console
   you should rename the file to `RhinoIntents.rhn`
   3. file 3 is the wake word, you can create your own in the picovoice console, or you can use one 
      of the free ones [available here](https://github.com/Picovoice/porcupine/tree/master/resources/keyword_files). the file should be called `WakeWord.ppn`
3. navigate to the root directory and run `./gradlew build`


## Commands
commands in parentheses `()` are possible options for the command
e.g. `(pause, play) music` can be evaluated as `play music` or `pause music`

commands in square brackets `[]` are optional words that can be said

- '(play, pause, shuffle, skip, rewind, back) music'

### Commands on the roadmap
- 'tell me a joke'
- 'tell me a dark joke'
- 'switch voice [to] (poppy, prudence, spike, obidiah)'
- 'whats [is] the time'
- '(play, pause, shuffle, skip, rewind, back) song'
- 'play {songname} [by {artist}]'

## Pre-Installed voices
- poppy
- prudence
- spike
- obidiah

voices were installed with the marytts gui installer (installed with the marytts server)
then copied from their install location to this projects `lib/voices` folder


## Roadmap

pass in a config file via a cli command containing various keys and settings
- mpd url,port, password if one.
- picovoice sdk key

## Custom voice

https://github.com/marytts/gradle-marytts-voicebuilding-plugin

samuel l jackson


## resources and ideas

https://medium.com/picovoice/prioritizing-privacy-add-offline-speech-recognition-to-a-java-application-1c864574fb7e
https://github.com/Picovoice/picovoice/tree/master/sdk/java
https://github.com/marytts/marytts



use rhino to listen for key intent words
for commands where more information is needed use a 2 step process
1. find intent in spoken text
2. pass over to leopard where it will listen from the microphone and parse what's spoken to text
   returned text can then be parsed an executed

eg.
- 'Hey owl play song'
- response: 'ok what do you want me to play? '
- pass over to leopard
- 'play {song name} by {artist}'

[leopard mic demo github](https://github.com/Picovoice/leopard/blob/master/demo/java/src/ai/picovoice/leoparddemo/MicDemo.java)
