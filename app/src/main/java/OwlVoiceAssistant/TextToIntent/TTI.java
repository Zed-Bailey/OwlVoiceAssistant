package OwlVoiceAssistant.TextToIntent;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A text to intent parser
 */
public class TTI {

    Command[] commands;


    /**
     *
     * @param intentFilePath The file path to the intent definitions that will be matched to the speech
     */
    public TTI(String intentFilePath) {
        commands = LoadAndParse(intentFilePath);
    }

    public Intent ParseTextToCommand(String text) {

        for (Command c : commands) {
            var match = c.Match(text);
            if(match != null)
                return match;
        }

        return null;
    }

    private Command[] LoadAndParse(String path)  {

        var filePath = Path.of(path);
        String content = null;
        try {
            content = Files.readString(filePath);
        }catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        if(content == null) {
            System.exit(1);
        }

        Gson gson = new Gson();
        Command[] parsed = gson.fromJson(content, Command[].class);
        return parsed;
    }

    private void PrintCommand(Command c) {
        System.out.printf("Command:\n\tname:%s\n", c.name);
        System.out.printf("\tspeech: %s\n", c.speech);
        c.slots.forEach((s, strings) -> System.out.printf("\tslot: %s : %s\n", s, strings));
    }



    /*


    wildcard slots $*:output
    wildcard slots will simply return whatever word is in that position

    intent = the intent of the command, e.g musicControl or getWeather
    speech = array of possible alternatives sayings for this intent
    each speech string can have optional sayings in ()
    each speech saying can also have alternative words in []
    e.g
    "turn on [all the] lights" will evaluate to "turn on lights" and "turn on all the lights"
     and will return `{ "intent" : "lightControl" }`

    wildcard slots
    "get weather in $*:location"
    this will simply return the text at that location eg.
    "get weather in melbourne" will return
    {"intent": "getWeather", "slots" : {"location": "melbourne"}}
    alternatively had I said something else instead of melbourne then that would've been returned





     */
}
