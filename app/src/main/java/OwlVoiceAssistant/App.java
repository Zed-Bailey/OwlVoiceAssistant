package OwlVoiceAssistant;

import OwlVoiceAssistant.Commands.CommandInterface;
import OwlVoiceAssistant.Commands.MusicCommand;
import ai.picovoice.picovoice.Picovoice;
import ai.picovoice.picovoice.PicovoiceException;
import ai.picovoice.picovoice.PicovoiceInferenceCallback;
import ai.picovoice.picovoice.PicovoiceWakeWordCallback;
import ai.picovoice.porcupine.Porcupine;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class App {

    private static final Logger logger = LogManager.getLogger(App.class.getName());
    private TTS _tts;

    private Map<String, CommandInterface> intentMap;



    // FIXME? pass these paths through main function rather then hardcoding them, have them be passed as cli arguments?
    private final String porcupineKeyword = "WakeWord.ppn";
    private final String rhinoContextPath = "RhinoIntents.rhn";
    private final String picovoiceKeyPath = "picovoicekey.txt";


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

    /**
     * Read the file that stores the picovoice key
     * if the file is not found then the system will exit and a fatal log will be created.
     * @param path the file path to read
     * @return a string containing the  key
     */
    public String ReadPicoKeyFile(String path) {
        try {
            var inputStream = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream(path);
            if(inputStream == null) throw new NullPointerException("Input stream was null while trying to read picovoice key file path");

            String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            return  result;

        } catch(NullPointerException e) {
            logger.fatal("failed to load in key path: {}\nexception: {}", path, e);
            System.exit(1);
        } catch (IOException e) {
            logger.fatal("failed to read key file path = {}\nexception: {}", path, e);
            System.exit(1);
        }

        return "";
    }

    public void Run () throws PicovoiceException, LineUnavailableException {
        InitializeTTS();
        this.intentMap = GenerateIntentCommandMap.MapCommands();

        var rhinoPath = this.getClass()
                .getClassLoader()
                .getResource(this.rhinoContextPath)
                .getPath();

        var wakeWordPath = this.getClass()
                .getClassLoader()
                .getResource(this.porcupineKeyword)
                .getPath();

        // set an inbuilt wake word paths
//        final Porcupine.BuiltInKeyword builtInKeyword = Porcupine.BuiltInKeyword.valueOf("JARVIS");
//        wakeWordPath = Porcupine.BUILT_IN_KEYWORD_PATHS.get(builtInKeyword);

        if(rhinoPath == null) {
            throw new IllegalArgumentException("Could not find rhino context file: " + this.rhinoContextPath + " in the class path");
        }

        if (wakeWordPath == null) {
            throw new IllegalArgumentException("Could not find porcupine keyword file: " + this.porcupineKeyword + " in the class path");
        }

        var picovoicekey = ReadPicoKeyFile(this.picovoiceKeyPath);


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
                // TODO read cli arguments to get model paths and key rather then hard coding it in
                .setAccessKey(picovoicekey)
                .setKeywordPath(wakeWordPath)
                .setWakeWordCallback(wakeWordCallback)
                .setContextPath(rhinoPath)
                .setInferenceCallback(inferenceCallback)
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
        PropertyConfigurator.configure("log4j2.properties");
        App app = new App();
        try {
            app.Run();
        }
        catch(PicovoiceException | LineUnavailableException e) {
            logger.fatal("Failed to initialize picovoice: exception = {}", e.getMessage());
        }


    }
}
