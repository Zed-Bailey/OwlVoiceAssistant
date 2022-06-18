package OwlVoiceAssistant.Commands;

import OwlVoiceAssistant.TextToIntent.Intent;

public class WeatherCommand implements CommandInterface {
    @Override
    public String ExecuteCommand(Intent intent) {
        return "The weather is sunny in " + intent.slots.get("location");
    }
}
