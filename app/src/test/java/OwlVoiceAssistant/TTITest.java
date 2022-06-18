package OwlVoiceAssistant;

import OwlVoiceAssistant.TextToIntent.TTI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class TTITest {
    static TTI tti;

    @BeforeAll
    static void Init() {
        var path = Paths.get("src","test","resources", "example.json").toAbsolutePath().toString();
        tti = new TTI(path);
    }

    @Test
    void TestCommandIntent() {
        var match = tti.ParseTextToCommand("PLAY MUSIC");
        assertNotNull(match);
        assertEquals("musicControl", match.intent);
        assertEquals("play", match.slots.get("action"));
    }

    @Test
    void TestCommandWithWildcard() {
        var match = tti.ParseTextToCommand("WEATHER IN MELBOURNE");
        assertNotNull(match);
        assertEquals("getWeather", match.intent);
        assertEquals("melbourne", match.slots.get("location"));
    }
}
