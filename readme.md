# Owl voice assistant
An offline privacy first voice assistant

## Prerequisites

- mpd (Music player daemon) setup and listening on localhost:6600
  - in the future this will be configured with a config file

download a vosk model from [here](https://alphacephei.com/vosk/models) and unzip it. rename folder to model and place it in the `app/` directory



## Running
Create a configuration.properties file with the following key value pairs defined
```properties
commandJson={path to grammar json file}
wakeWord=computer
```
wake word can be any word you want

you can then run the application with
`java -jar app.jar configuration.properties`

## Commands

existing commands can be found in `app/Grammar.json`

TODO: add more docs here
TODO: add google command. e.g. `computer google how many punds in an ounce`


## Pre-Installed voices
- poppy
- prudence
- spike
- obidiah

voices were installed with the marytts gui installer (installed with the marytts server)
then copied from their install location to this projects `lib/voices` folder


## Roadmap

- ability for assistant to change voice
- more integrations
- deployment to a raspberry pi


## Custom voice

https://github.com/marytts/gradle-marytts-voicebuilding-plugin
