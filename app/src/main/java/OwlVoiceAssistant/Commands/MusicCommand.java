package OwlVoiceAssistant.Commands;

import java.util.Map;

public class MusicCommand implements CommandInterface {

    @Override
    public String ExecuteCommand(String intent, Map<String, String> slots) {
        return String.format("%s", intent);
    }
}
