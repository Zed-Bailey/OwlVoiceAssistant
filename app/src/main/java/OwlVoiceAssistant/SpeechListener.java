package OwlVoiceAssistant;

import OwlVoiceAssistant.TextToIntent.Intent;
import OwlVoiceAssistant.TextToIntent.TTI;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

interface WakeWordCallBack {
    void wakeWordDetected();
}

interface IntentCallBack {
    void intentParsed(Intent intent);
}


public class SpeechListener {

    private final String wakeWord;
    private WakeWordCallBack wakeWordCallBack;
    private IntentCallBack intentCallBack;
    private final TTI _tti;

    public SpeechListener(String wakeWord, TTI tti) {
        this.wakeWord = wakeWord;
        this._tti = tti;
    }

    public SpeechListener setWakeWordCallback(WakeWordCallBack w) {
        this.wakeWordCallBack = w;
        return this;
    }

    public SpeechListener setIntentCallBack(IntentCallBack i) {
        this.intentCallBack = i;
        return this;
    }

    public void Start() throws IOException {
        LibVosk.setLogLevel(LogLevel.DEBUG);

        // answer to this question is the basis of the vosk microphone listening
        // https://stackoverflow.com/questions/68401284/use-the-microphone-in-java-for-speech-recognition-with-vosk

        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 60000, 16, 2, 4, 44100, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine microphone;
        try (Model model = new Model("model");
             Recognizer recognizer = new Recognizer(model, 120000)) {
            try {

                recognizer.setMaxAlternatives(1);

                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int numBytesRead;
                int CHUNK_SIZE = 1024;
                System.out.println("[INFO] Now listening");

                byte[] b = new byte[4096];
                boolean shutdown = false;
                boolean awoken = false;

                while (!shutdown) {
                    numBytesRead = microphone.read(b, 0, CHUNK_SIZE);

                    out.write(b, 0, numBytesRead);

                    if (recognizer.acceptWaveForm(b, numBytesRead)) {
                        var input = recognizer.getFinalResult();
                        // parse the json string that vosk outputs
                        Any any = JsonIterator.deserialize(input);
                        var stt = any.get("alternatives", 0, "text").toString();
                        if (stt.contains(wakeWord)) {
                            // remove wake word from command
                            stt = stt.replace(wakeWord, "").trim();
                            // parse intent
                            var intent = _tti.ParseTextToCommand(stt);

                            // reset wake word status
                            awoken = false;

                            this.intentCallBack.intentParsed(intent);
                        }


                    } else {
                        var partial = JsonIterator.deserialize(recognizer.getPartialResult()).get("partial").toString();
                        // check if the partial word matches the wake word, if it does
                        if (Objects.equals(partial, wakeWord) && !awoken) {
                            awoken = true;
                            this.wakeWordCallBack.wakeWordDetected();
                        }
                    }
                }

                microphone.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
