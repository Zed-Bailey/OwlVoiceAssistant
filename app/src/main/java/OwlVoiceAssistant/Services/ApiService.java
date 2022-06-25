package OwlVoiceAssistant.Services;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class ApiService {
    private static OkHttpClient client = new OkHttpClient();

    /**
     * Calls the passed in api url and returns the response.
     * @param url the url to call
     * @return  an okhttp3 Response object containing all the information
     * @throws IOException
     */
    public static Response CallApi(String url) throws IOException {

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        return call.execute();
    }

    /**
     * Calls the api url and returns the json response as a string.
     * This method is useful if you don't need the entire request object
     * @param url api url to call
     * @return the response's json body as a string
     * @throws IOException
     */
    public static String CallApiJsonResponse(String url) throws IOException {
        var response = CallApi(url);
        return response.body().string();
    }
}
