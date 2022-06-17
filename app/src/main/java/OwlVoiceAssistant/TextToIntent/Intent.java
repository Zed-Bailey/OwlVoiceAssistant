package OwlVoiceAssistant.TextToIntent;

import java.util.HashMap;
import java.util.Map;

public class Intent {
    public String intent;
    public HashMap<String, String> slots = new HashMap<>();

    public String mapping;

    @Override
    public String toString() {
        return String.format("intent: %s\nslots: %s", this.intent, this.slots);
    }
}
