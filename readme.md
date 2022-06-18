# Owl voice assistant
An offline privacy first voice assistant

## Prerequisites

- mpd (Music player daemon) setup and listening on localhost:6600
  - in the future this will be configured with a config file


## Running
Create a configuration.properties file with the following key value pairs defined
```properties
rhinoPath={path to rhino .rhn intents file}
porcupinePath={path to .ppn wake word file}
picovoiceKey={your picovoice key}
commandJson={path to grammar json file}
```
you can then run the application with
`java -jar app.jar configuration.properties`

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
