package OwlVoiceAssistant.Commands;

import OwlVoiceAssistant.TextToIntent.Intent;
import com.jsoniter.JsonIterator;
import okhttp3.*;

import java.io.IOException;


public class WeatherCommand implements CommandInterface {

    private final String _apiKey;
    private final String baseUrl = "https://api.openweathermap.org/data/2.5/weather";
    private static OkHttpClient client = new OkHttpClient();

    public WeatherCommand(String apiKey) {
        this._apiKey = apiKey;
    }
    @Override
    public String ExecuteCommand(Intent intent) {
        // api docs
        //https://openweathermap.org/current#geocoding

        HttpUrl.Builder urlBuild = HttpUrl.parse(this.baseUrl).newBuilder();
        urlBuild.addQueryParameter("q", intent.slots.get("location"));
        // get metric values, celsius rather then kelvin
        urlBuild.addQueryParameter("units", "metric");
        urlBuild.addQueryParameter("appid", this._apiKey);

        var url = urlBuild.build().toString();
//        System.out.println("[WEATHER COMMAND] url => " + url); // DEBUG

        Request request = new Request.Builder()
                .url(url)
                .build();


        try {
            Call call = client.newCall(request);
            Response response = call.execute();
            var body = response.body().string();
//            System.out.println("Response body: " + body); // DEBUG

            var json = JsonIterator.deserialize(body);
            var description  =  json.get("weather", 0, "description").toString();

            // temperature is in celsius
            var temp = json.get("main", "temp").toDouble();

            return String.format("Current temperature in %s is %.2f with a %s", intent.slots.get("location"), temp, description);
        } catch (IOException e) {
            return "Trouble getting weather!";
        }
    }
}
