package notification;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class SlackIntegration {

    private static final String url = "https://hooks.slack.com/services/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";
    public static void main(String[] args) throws IOException, MalformedURLException {
        SlackIntegration integration = new SlackIntegration();
        String eventType = "commit";
        integration.notifySlack(url, eventType.substring(0, 1).toUpperCase() + eventType.substring(1), "slack-integration", true, "Moa Nyman", "https://github.com/botronic", "https://github.com/software-fundamentals/CI-server");
    }

    public void notifySlack(String endpoint, String eventType, String branch, Boolean success, String sender, String senderURL, String titleLink) throws IOException, MalformedURLException {
        try {
            URL url = new URL (endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.connect();

            JSONObject json = new JSONObject();
            json.put("attachments", createSlackAttachment(eventType, branch, success, sender, senderURL, titleLink));

            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(json.toString());
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

    private JSONArray createSlackAttachment(String eventType, String branch, Boolean success, String sender, String senderURL, String titleLink) {
        JSONObject attachment = new JSONObject();
        attachment.put("fallback", "Unable to display repository status");
        attachment.put("color", (success ? "good" : "danger"));
        attachment.put("author_name", sender);
        attachment.put("author_url", senderURL);
        attachment.put("title", eventType + " event on branch " + branch);
        attachment.put("title_link", titleLink);
        attachment.put("text", "Build status: " + (success ? "SUCCESS" : "FAILING"));
        attachment.put("footer", "Continuous Integration Server");

        JSONArray array = new JSONArray();
        array.put(attachment);
        return array;
    }
}