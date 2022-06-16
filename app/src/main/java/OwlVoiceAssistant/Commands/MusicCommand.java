package OwlVoiceAssistant.Commands;

import org.bff.javampd.server.MPD;

import java.util.Map;

public class MusicCommand implements CommandInterface {

    public static boolean CurrentlyPlaying = false;

    private static final MPD _mpd = MPD.builder().build();


    @Override
    public String ExecuteCommand(String intent, Map<String, String> slots) {
        var action = slots.get("action");
        var output = "";

        switch(action) {
            case "play":
                Play();
                output = "Music is now playing";
                break;
            case "pause":
                Pause();
                output = "Music is now paused";
                break;

            case "skip":
                Skip();
                break;

            case "back":
            case "rewind":
                Rewind();
                break;

            case "shuffle":
                output = "Tracks shuffled";
                Shuffle();
                break;

            default:
                output = "Sorry i dont understand that action!";
                break;
        }

        return output;
    }

    public static void Pause() {
        if(CurrentlyPlaying) {
            _mpd.getPlayer().stop();
            CurrentlyPlaying = false;
        }

    }
    public static void Play() {
        if(!CurrentlyPlaying) {
            _mpd.getPlayer().play();
            CurrentlyPlaying = true;
        }
    }
    public static void Skip() {
        _mpd.getPlayer().playNext();
    }
    public static void Rewind() {
        _mpd.getPlayer().playPrevious();
    }

    public static void Shuffle() {
        _mpd.getPlayer().randomizePlay();
        Skip();
    }
}
