package notification;

import notification.NotificationJson;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;

public class Notification {

    static Dotenv dotenv = Dotenv.load();
    private final static String GITHUB_TOKEN = dotenv.get("GITHUB_TOKEN");
    private final static String SLACK_URL = dotenv.get("SLACK_URL");
    private final static String BASE_URL = "https://api.github.com/repos/software-fundamentals/CI-server/statuses/";

    public static void sendNotifications(String authorName, String authorUrl, String branch, String compareUrl, String sha, Boolean success, String message) throws IOException {
        String title = "Commit on branch: " + branch;
        String text = "Build status: ";
        String color;

        if (success) {
            color = "good";
            text += "SUCCESS";
        } else {
            color = "danger";
            text += "FAILURE";
        }

        text += "\n" + message;
        JSONObject slackJson = NotificationJson.createSlackJson(authorName, authorUrl, title, compareUrl, text, color);
        JSONObject gitJson = NotificationJson.createCommitJson((success ? "success" : "failure"), compareUrl, "Build: " + (success ? "SUCCESS" : "FAILURE"), "CI Server");
        makePostRequest(SLACK_URL, slackJson);
        makePostRequest(BASE_URL + sha, gitJson);
    }

    private static void makePostRequest(String endpoint, JSONObject jsonBody) throws IOException, MalformedURLException {
        try {
            URL url = new URL (endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + GITHUB_TOKEN);
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

