package VoiceAssistant.CalBacks;

import VoiceAssistant.TextToIntent.Intent;

public interface IntentCallBack {
    void intentParsed(Intent intent);
}
