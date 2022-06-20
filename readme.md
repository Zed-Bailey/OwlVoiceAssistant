# Owl voice assistant
An offline privacy first voice assistant

## Prerequisites

- mpd (Music player daemon) setup and listening on localhost:6600
  - in the future this will be configured with a config file

## Using a different speech to text model
download a vosk model from [here](https://alphacephei.com/vosk/models) and unzip it. rename folder to model and place it in the `app/` directory


## Building
assuming you have graalvm 22.1.0 for jdk 17
and graalvm `native-image` component installed
```
# from root project directory
./gradlew shadowJar

# build a native executable 
native-image -jar app/build/libs/OwlVoiceAssistant-0.1.0-all.jar voice-assistant

# run executable
./voice-assistant <path to configuration.properties>
```


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

creating your own command
example weather grammar
```json
{
    "name": "getWeather",
    "speech" : [
      "what's the weather in $*:location",
      "weather in $*:location",
      "get weather in $*:location",
      "get the weather in $*:location"
    ],
    "slots" : {}
}
```
`$*:location` is a wildcard slot and will return whatever value is in that position
"weather in melbourne" will return the intent `{intent: getWeather, slots {location = "melbourne"}}`
if you want to match against a set list of values you can use slots
```json
{
    "name": "musicControl",
    "speech" : [
      "$controlaction:action music",
      "$controlaction:action song"
    ],
    "slots" : {
      "controlaction" : ["pause", "play", "skip", "rewind", "shuffle"]
    }
  }
```
here `$controlaction:action` will map the spoken value to one of the possible values in the controlaction array
"play music" will return an intent `{intent: musicControl, slots {action = "play"}}`

to return the rest of the sentence `$>:variable` can be used
```json
{
    "name": "google",
    "speech" : [
      "google $>:search"
    ],
    "slots" : {}
  }
```
"google how many kilos in a pound" will return an intent `{intent: google, slots {search = "how many kilos in a pound"}}`

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
