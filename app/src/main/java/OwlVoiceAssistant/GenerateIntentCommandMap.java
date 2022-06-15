package OwlVoiceAssistant;

import OwlVoiceAssistant.Commands.CommandInterface;
import OwlVoiceAssistant.Commands.MusicCommand;

import java.util.HashMap;
import java.util.Map;

public class GenerateIntentCommandMap {
    /**
     * Maps the intents to the appropriate functions
     * @return a HashMap containing the intent function key pair.
     */
    public static Map<String, CommandInterface> MapCommands() {
        HashMap<String, CommandInterface> map = new HashMap<>();
        map.put("musicControl", new MusicCommand());
        return map;
    }
}
