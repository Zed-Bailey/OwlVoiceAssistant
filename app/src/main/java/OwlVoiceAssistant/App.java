package OwlVoiceAssistant;

import OwlVoiceAssistant.Commands.CommandInterface;
import OwlVoiceAssistant.Commands.MusicCommand;
import ai.picovoice.picovoice.Picovoice;
import ai.picovoice.picovoice.PicovoiceException;
import ai.picovoice.picovoice.PicovoiceInferenceCallback;
import ai.picovoice.picovoice.PicovoiceWakeWordCallback;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Properties;

public class App {

    private static final Logger logger = LogManager.getLogger(App.class);
    private TTS _tts;

    private Map<String, CommandInterface> intentMap;

    /**
     * Initialize the text to speech class
     * if this fails to initialize then the application will exit with ExitCode = 1
     */
    public void InitializeTTS() {
        var voice = "dfki-prudence-hsmm";
        _tts = new TTS(voice);
        try {
            _tts.Configure();
            logger.info("Initialized Text to speech with {}", voice);
        } catch (Exception e) {
            logger.fatal("Failed to initialize the text to speech object: exception = {}", e.getMessage());
            System.exit(1);
        }
    }


    public void Run (String rhinoPath, String porcupinePath, String picovoiceKey) throws PicovoiceException, LineUnavailableException {
        InitializeTTS();

        this.intentMap = GenerateIntentCommandMap.MapCommands();

        PicovoiceWakeWordCallback wakeWordCallback = () -> {
            System.out.println("Wake word detected!");
            // let user know wake word was detected
            //TODO: light up eyes and play notification sound?

            // pause music is it's currently playing
            if (MusicCommand.CurrentlyPlaying) {
                MusicCommand.Pause();
            }

        };

        PicovoiceInferenceCallback inferenceCallback = inference -> {
            if (inference.getIsUnderstood()) {
                final String intent = inference.getIntent();
                final Map<String, String> slots = inference.getSlots();

//                System.out.println(intent);
//                slots.forEach((key, value) -> System.out.println("\t" + key + ":" + value));

                String speak = this.intentMap.get(intent).ExecuteCommand(intent, slots);
                if(speak == null) {
                    logger.error("No mapping for the intent: {}, was found in the intentCommandMap. Did you add it in GenerateIntentCommandMap.MapCommands", intent);
                    _tts.Speak("Sorry i could not find a command for the intent " + intent);
                } else {
                    _tts.Speak(speak);
                }

            } else {
                _tts.Speak("I have no idea what you want me to do!");
            }
        };


        Picovoice picovoice;
        picovoice = new Picovoice.Builder()
                .setAccessKey(picovoiceKey)
                .setKeywordPath(porcupinePath)
                .setWakeWordCallback(wakeWordCallback)
                .setContextPath(rhinoPath)
                .setInferenceCallback(inferenceCallback)
                .setPorcupineSensitivity(0.7f)
                .build();

        logger.info("Initialized Picovoice");

        // buffers for processing audio
        short[] picovoiceBuffer = new short[picovoice.getFrameLength()];
        ByteBuffer captureBuffer = ByteBuffer.allocate(picovoice.getFrameLength() * 2);
        captureBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // get default audio capture device
        AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine micDataLine;

        micDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        micDataLine.open(format);

        // start audio capture
        micDataLine.start();

        int numBytesRead;
        boolean recordingCancelled = false;
        while (!recordingCancelled) {

            // read a buffer of audio
            numBytesRead = micDataLine.read(captureBuffer.array(), 0, captureBuffer.capacity());

            // don't pass to Picovoice if we don't have a full buffer
            if (numBytesRead != picovoice.getFrameLength() * 2) {
                continue;
            }

            // copy into 16-bit buffer
            captureBuffer.asShortBuffer().get(picovoiceBuffer);

            // process with picovoice
            picovoice.process(picovoiceBuffer);
        }
    }


    public static void main(String[] args) {
        // print the default  wake commands that can be added
//        System.out.println("Default wake commands");
//        Porcupine.BUILT_IN_KEYWORD_PATHS.forEach((key,value) -> System.out.println(key + ":" + value));
//        System.out.println("-----\n");

        if(args.length != 1) {
            System.out.println("Path to properties file is required");
            return;
        }

        PropertyConfigurator.configure("log4j2.xml");
        try(InputStream stream = new FileInputStream(args[0])) {
            Properties prop = new Properties();
            prop.load(stream);
            App app = new App();

            app.Run(prop.getProperty("rhinoPath"),prop.getProperty("porcupinePath"), prop.getProperty("picovoiceKey"));

        } catch(IOException e) {
            System.err.printf("Failed to stream the properties file!: %s\n", e);
            System.exit(1);
        } catch(PicovoiceException | LineUnavailableException e) {
            logger.fatal("Failed to initialize picovoice: exception = {}", e.getMessage());
        }
    }
}
