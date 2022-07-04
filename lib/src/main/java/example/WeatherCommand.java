package example;

import VoiceAssistant.CommandInterface;
import VoiceAssistant.TextToIntent.Intent;

import java.util.Properties;
import java.util.Random;

public class WeatherCommand implements CommandInterface {

    public WeatherCommand(Properties prop) {
        // use properties file to get weather api key
        //....
    }

    @Override
    public String ExecuteCommand(Intent intent) {
        var location = intent.slots.get("location");
        //... Fetch weather data for location
        var weatherConditions = new String[]{"sunny", "windy", "raining", "stormy", "hailing"};
        var random = new Random();
        var randCondition = random.nextInt(weatherConditions.length);
        return String.format("The weather is %s in %s", weatherConditions[randCondition], location);
    }
}
