# Owl voice assistant



## Commands
commands in parentheses `()` are possible options for the command
eg. `(pause, play) music` can be evaluated as `play music` or `pause music`

commands in square brackets `[]` are optional words that can be said

- '(play, pause, shuffle, skip, rewind, back) music'
- 'tell me a joke'
- 'tell me a dark joke'
- 'switch voice [to] (poppy, prudence, spike, obidiah)'
- 'whats [is] the time'

## Pre installed voices
- poppy
- prudence
- spike
- obidiah

voices were installed with the marytts gui installer (installed with the marytts server)
then copied from their install location to this projects `lib/voices` folder


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
