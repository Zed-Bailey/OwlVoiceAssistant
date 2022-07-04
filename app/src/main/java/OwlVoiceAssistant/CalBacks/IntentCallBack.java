package OwlVoiceAssistant.CalBacks;

import OwlVoiceAssistant.TextToIntent.Intent;

public interface IntentCallBack {
    void intentParsed(Intent intent);
}
