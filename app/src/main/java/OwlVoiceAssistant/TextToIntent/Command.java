package OwlVoiceAssistant.TextToIntent;

import java.util.*;

public class Command {
    public String name;
    public ArrayList<String> speech;
    public Map<String, ArrayList<String>> slots;


    public Intent Match(String text) {
        for(String speech: speech) {
            var match =  this.tryMatch(text, speech);
            if(match != null) {
                match.intent = this.name;
                return match;
            }
        }

        return null;
    }



    private Intent tryMatch(String spokenText, String phrase) {
        var inputSplit = spokenText.toLowerCase().split(" ");
        Intent parsedIntent = new Intent();

        // convert phrase to upper case
        var speechSplit = phrase.split(" ");
        if (inputSplit.length == speechSplit.length) {
            for (int i = 0; i < inputSplit.length; i++) {
                if(speechSplit[i].contains("$")) {
                    // $controlaction:action -> ["controlaction", "action"]
                    var slot = (speechSplit[i].replace("$", "")).split(":");
                    // System.out.println("slots = " + Arrays.toString(slot)); // DEBUG

                    // handle wildcard slot
                    // simply assigns whatever is in the current position in the spoken text to the slot key value
                    if(Objects.equals(slot[0], "*")) {
                        parsedIntent.slots.put(slot[1], inputSplit[i]);
                        continue;
                    }

                    // get the possible slot values for controlaction
                    var slotValues = this.slots.get(slot[0]);

                    // check that the spoken input has a matching input in the slots
                    var slotIndex = slotValues.indexOf(inputSplit[i]);
                    if(slotIndex == -1){
                        return null;
                    }
                    // add the parsed slot to the value
                    parsedIntent.slots.put(slot[1], slotValues.get(slotIndex));
                }
                else if(!Objects.equals(inputSplit[i], speechSplit[i])) {
                    return null;
                }
            }
        }
        return parsedIntent;
    }

}
