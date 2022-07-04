# Owl voice assistant
An offline privacy first voice assistant library

## Example
An example implementation of a simple voice assistant can be found in the `example/` directory


## Implementing your own voice assistant
To implement your own assistant, extended the abstract class `VoiceAssistant.Assistant` and implement the methods

As a base you can use `example/ExampleAssistant.java` and build on top of that
The initialize function should call `Assistant.InitializeTTI` and `Assistant.InitializeTTS` and `MapCommands`
```java
// example/ExampleAssistant.java
public class ExampleAssistant extends Assistant {
    
    @Override
    public void Initialize(Properties prop) {
        this.tti = this.InitializeTTI(prop.getProperty("commandJson"));
        this.tts = this.InitializeTTS(AssistantVoice.poppy);
        this.intentMap = this.MapCommands(prop);

        // initialize anything else after
        // ....
    }
}
```
Without initializing these the assistant won't function.

Implementing a new command should extend `CommandInterface` this defines a single method `Execute` that you 
overwrite with your command functionality. 

You should handle any possible failures in the function and return a string containing what you want spoken.
An example implementation is in `example/WeatherCommand.java`



## Defining Commands

An example command can be found in the `example/` directory

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


## Speech to text model
This library uses vosk for speech to text.
1. clone this repo
2. download new model from [here](https://alphacephei.com/vosk/models) and unzip it
3. rename unzipped folder to model and place it in the `app/` directory
4. build a new jar with `./gradlew shadowJar`
5. Use the newly built jar in your project.


## Custom voice

https://github.com/marytts/gradle-marytts-voicebuilding-plugin
