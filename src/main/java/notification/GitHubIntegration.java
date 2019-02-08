package notification;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import io.github.cdimascio.dotenv.Dotenv;

import org.json.JSONObject;

public class GitHubIntegration {

    static Dotenv dotenv = Dotenv.load();
    private final static String OAUTH_TOKEN = dotenv.get("GITHUB_TOKEN");

    private final static String baseUrl = "https://api.github.com/repos/software-fundamentals/CI-server/statuses/";

    public static void setCommitStatus(String sha, JSONObject jsonBody) throws IOException, MalformedURLException {
        try {
            URL url = new URL (baseUrl + sha);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + OAUTH_TOKEN);
            connection.connect();

            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(jsonBody.toString());
            osw.flush();

            int httpResult = connection.getResponseCode();
            if (httpResult != HttpURLConnection.HTTP_OK) {
                System.out.println(connection.getResponseMessage());
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}