package OwlVoiceAssistant;

import OwlVoiceAssistant.Commands.CommandInterface;
import OwlVoiceAssistant.Commands.MusicCommand;
import OwlVoiceAssistant.TextToIntent.Intent;
import OwlVoiceAssistant.TextToIntent.TTI;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class App {

    private static final Logger logger = LogManager.getLogger(App.class);
    private TTS _tts;
    private TTI _tti;

    private Map<String, CommandInterface> intentMap;

    /**
     * Initialize the text to speech class
     * if this fails to initialize then the application will exit with ExitCode = 1
     */
    private TTS InitializeTTS() {
        var voice = "dfki-prudence-hsmm";
        var tts = new TTS(voice);
        try {
            tts.Configure();
            logger.info("Initialized Text to speech with {}", voice);
        } catch (Exception e) {
            logger.fatal("Failed to initialize the text to speech object: exception = {}", e.getMessage());
            System.exit(1);
        }
        return tts;
    }

    private TTI InitializeTTI(String grammarPath) {
        return new TTI(grammarPath);
    }

    private void HandleIntent(Intent intent) {
        if(intent == null) {
            _tts.Speak("Sorry i don't understand");
            return;
        }

        var command = this.intentMap.get(intent.intent);
        if(command == null) {
            _tts.Speak("No mapping for that command");
            return;
        }

        _tts.Speak(command.ExecuteCommand(intent));
    }


    public void Run (Properties prop) throws LineUnavailableException, IOException {
        _tts = InitializeTTS();
        System.out.println("Initialized text to speech");

        _tti = InitializeTTI(prop.getProperty("commandJson"));
        System.out.println("Initialized text to intent");

        String wakeWord = prop.getProperty("wakeWord");
        System.out.println("Using wake word: " + wakeWord);

        // generate the mapping for the intent -> Command class
        this.intentMap = GenerateIntentCommandMap.MapCommands(prop);

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
                System.out.println("Now listening");

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

                        if(stt.contains(wakeWord)) {
                            stt = stt.replace(wakeWord, "").trim();
                            System.out.println(stt);
                            HandleIntent(_tti.ParseTextToCommand(stt));

                            // reset wake word status
                            awoken = false;
                        }
                    } else {
                        var partial = JsonIterator.deserialize(recognizer.getPartialResult()).get("partial").toString();
                        // check if the partial word matches the wake word, if it does
                        if(Objects.equals(partial, wakeWord) && !awoken) {
                            awoken = true;
                            // TODO: do any actions here on wake word detection
                        }
                    }
                }

                microphone.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Path to properties file is required");
            return;
        }

        PropertyConfigurator.configure("log4j2.xml");

        try(InputStream stream = new FileInputStream(args[0])) {

            Properties prop = new Properties();
            prop.load(stream);

            App app = new App();
            app.Run(prop);

        } catch(IOException e) {
            System.err.printf("Failed to get the properties file!: %s\n", e);
            System.exit(1);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
}
