package OwlVoiceAssistant.Commands;

import OwlVoiceAssistant.Services.ApiService;
import OwlVoiceAssistant.TextToIntent.Intent;
import com.jsoniter.JsonIterator;
import okhttp3.*;

import java.io.IOException;


public class WeatherCommand implements CommandInterface {

    private final String _apiKey;

    public WeatherCommand(String apiKey) {
        this._apiKey = apiKey;
    }
    @Override
    public String ExecuteCommand(Intent intent) {
        // api docs
        //https://openweathermap.org/current#geocoding

        String baseUrl = "https://api.openweathermap.org/data/2.5/weather";
        HttpUrl.Builder urlBuild = HttpUrl.parse(baseUrl).newBuilder();
        urlBuild.addQueryParameter("q", intent.slots.get("location"));
        // get metric values, celsius rather then kelvin
        urlBuild.addQueryParameter("units", "metric");
        urlBuild.addQueryParameter("appid", this._apiKey);

        var url = urlBuild.build().toString();

        try {
            var response = ApiService.CallApiJsonResponse(url);
            var json = JsonIterator.deserialize(response);
            var description  =  json.get("weather", 0, "description").toString();

            // temperature is in celsius
            var temp = json.get("main", "temp").toDouble();

            return String.format("Current temperature in %s is %.2f with a %s", intent.slots.get("location"), temp, description);
        } catch (IOException e) {
            return "Trouble getting weather!";
        }
    }
}
