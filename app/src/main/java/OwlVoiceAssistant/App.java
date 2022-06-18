package OwlVoiceAssistant;

import OwlVoiceAssistant.Commands.CommandInterface;
import OwlVoiceAssistant.Commands.MusicCommand;
import OwlVoiceAssistant.TextToIntent.Intent;
import OwlVoiceAssistant.TextToIntent.TTI;
import ai.picovoice.cheetah.Cheetah;
import ai.picovoice.cheetah.CheetahException;
import ai.picovoice.cheetah.CheetahTranscript;
import ai.picovoice.picovoice.PicovoiceException;
import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineException;
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


    public void Run (Properties prop) throws PicovoiceException, LineUnavailableException, PorcupineException, CheetahException {
        _tts = InitializeTTS();
        _tti = InitializeTTI(prop.getProperty("commandJson"));

        String picovoiceKey = prop.getProperty("picovoiceKey");

        // generate the mapping for the intent -> Command class
        this.intentMap = GenerateIntentCommandMap.MapCommands(prop);


        Porcupine porcupine = new Porcupine.Builder()
                .setAccessKey(picovoiceKey)
                .setBuiltInKeyword(Porcupine.BuiltInKeyword.COMPUTER)
                .build();

        System.out.println("Porcupine initialized");

        Cheetah cheetah = new Cheetah.Builder()
                .setAccessKey(picovoiceKey)
                .setLibraryPath(Cheetah.LIBRARY_PATH)
                .setModelPath(Cheetah.MODEL_PATH)
                .setEndpointDuration(2.5f)
                .build();
        System.out.println("Cheetah initialized");


        // get default audio capture device
        AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine micDataLine;

        micDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        micDataLine.open(format);

        // start audio capture
        micDataLine.start();

        // buffers for processing audio
        int frameLength = porcupine.getFrameLength();
        ByteBuffer captureBuffer = ByteBuffer.allocate(frameLength * 2);
        captureBuffer.order(ByteOrder.LITTLE_ENDIAN);
        short[] audioBuffer = new short[frameLength];

        int numBytesRead;
        boolean awoken = false;

        System.out.println("Now listening");
        while (true) {
            // read a buffer of audio
            numBytesRead = micDataLine.read(captureBuffer.array(), 0, captureBuffer.capacity());

            // don't pass to porcupine if we don't have a full buffer
            if (numBytesRead != frameLength * 2) {
                continue;
            }

            // copy into 16-bit buffer
            captureBuffer.asShortBuffer().get(audioBuffer);

            if(!awoken) {
                // process with porcupine
                int result = porcupine.process(audioBuffer);
                if (result >= 0) {
                    System.out.println("Computer wake word detected");
                    awoken = true;
                    // pause music if it was playing, so speech can be understood better
                    if(MusicCommand.CurrentlyPlaying) {
                        MusicCommand.Pause();
                    }
                }
            } else {
                CheetahTranscript transcriptObj = cheetah.process(audioBuffer);
                System.out.print(transcriptObj.getTranscript());

                if (transcriptObj.getIsEndpoint()) {
                    CheetahTranscript endpointTranscriptObj = cheetah.flush();
                    if(endpointTranscriptObj.getTranscript().equalsIgnoreCase("shutdown")) {
                        break;
                    }
                    var intent = _tti.ParseTextToCommand(endpointTranscriptObj.getTranscript());
                    System.out.println(intent);
                    this.HandleIntent(intent);
                    awoken = false;
                }
                System.out.flush();
            }
        }
        cheetah.delete();
        porcupine.delete();
        System.out.println("....shutting down");
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
            System.err.printf("Failed to stream the properties file!: %s\n", e);
            System.exit(1);
        }
        catch(PicovoiceException | LineUnavailableException | PorcupineException e) {
            logger.fatal("Failed to initialize picovoice: exception = {}", e.getMessage());
        } catch (CheetahException e) {
            logger.fatal("Failed to initialize cheetah: exception = {}", e.getMessage());
        }
    }
}
