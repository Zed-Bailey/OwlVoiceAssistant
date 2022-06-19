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


    private Map<String, CommandInterface> intentMap;

    /**
     * Initialize the text to speech class
     * if this fails to initialize then the application will exit with ExitCode = 1
     */
    private TTS InitializeTTS() {
        var voice = "dfki-poppy-hsmm";
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
            _tts.Speak("Sorry i do not understand");
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

        var tti = InitializeTTI(prop.getProperty("commandJson"));
        System.out.println("Initialized text to intent");

        String wakeWord = prop.getProperty("wakeWord");
        System.out.println("Using wake word: " + wakeWord);

        // do something here when intent has been parsed
        var listener = new SpeechListener(wakeWord, tti)
                .setWakeWordCallback(() -> {
                    // do something here when wake word detected
                    // pause music if playing
                    if(MusicCommand.CurrentlyPlaying) {
                        MusicCommand.Pause();
                    }
                }).setIntentCallBack(this::HandleIntent);

        // generate the mapping for the intent -> Command class
        this.intentMap = GenerateIntentCommandMap.MapCommands(prop);

        listener.Start();
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
