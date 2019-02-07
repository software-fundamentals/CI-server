package notification;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

public class SlackIntegration {

    private static final String SLACK_URL = "https://hooks.slack.com/services/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";

    public static void notifySlack(JSONObject jsonBody) throws IOException, MalformedURLException {
        try {
            URL url = new URL (SLACK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
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