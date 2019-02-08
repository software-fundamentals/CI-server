package notification;

import notification.NotificationJson;

import java.util.HashMap;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Class for sending notifications to connected services.
 */
public class Notification {

    static Dotenv dotenv = Dotenv.load();
    private final static String GITHUB_TOKEN = dotenv.get("GITHUB_TOKEN");
    private final static String SLACK_URL = dotenv.get("SLACK_URL");
    private final static String BASE_URL = "https://api.github.com/repos/software-fundamentals/CI-server/statuses/";

    /**
     * Method to send notifications to connected services about build status
     * Currently Slack and GitHub.
     * @param parsedData a map of strings (parsed from JSON)
     * @param success indicates whether the build succeeded (true) or not
     * @param message the message to send as notification text (for Slack)
     */
    public static void sendNotifications(HashMap<String, String> parsedData, Boolean success, String message) throws IOException {
        String title = "Commit on branch: " + parsedData.get("branch");
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
        JSONObject slackJson = NotificationJson.createSlackJson(
            parsedData.get("authorName"),
            parsedData.get("authorUrl"),
            title,
            parsedData.get("compareUrl"),
            text,
            color);
        JSONObject gitJson = NotificationJson.createCommitJson(
            (success ? "success" : "failure"),
            parsedData.get("compareUrl"),
            "Build: " + (success ? "SUCCESS" : "FAILURE"),
            "CI Server");
        makePostRequest(SLACK_URL, slackJson);
        makePostRequest(BASE_URL + parsedData.get("sha"), gitJson);
    }

    /**
     * Method for making a post request
     * @param endpoint the url target of the request
     * @param jsonBody the contents of the request
     * @throws IOException
     * @throws MalformedURLException
     */
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

