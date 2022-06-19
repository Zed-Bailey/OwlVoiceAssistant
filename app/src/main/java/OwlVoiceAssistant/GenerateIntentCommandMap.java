package OwlVoiceAssistant;

import OwlVoiceAssistant.Commands.CommandInterface;
import OwlVoiceAssistant.Commands.MusicCommand;
import OwlVoiceAssistant.Commands.WeatherCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GenerateIntentCommandMap {
    /**
     * Maps the intents to the appropriate functions
     * @param properties the properties' configuration file, can pass any api keys to the command initializer
     * @return a HashMap containing the intent function key pair.
     */
    public static Map<String, CommandInterface> MapCommands(Properties properties) {
        HashMap<String, CommandInterface> map = new HashMap<>();
        map.put("musicControl", new MusicCommand());
        map.put("getWeather", new WeatherCommand());
        return map;
    }
}
