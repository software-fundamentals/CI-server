package notification;

import notification.NotificationJson;
import notification.SlackIntegration;
import notification.GitHubIntegration;

import java.io.IOException;

import org.json.JSONObject;

public class Notification {

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
        JSONObject gitJson = NotificationJson.createCommitJson((success ? "success" : "failure"), "https://google.com", "Build: " + (success ? "SUCCESS" : "FAILURE"), "CI Server");
        //JSONObject object = NotificationJson.createCommitJson("success", "http://google.com/", "Build " + (success ?, "CI SERVER");
        GitHubIntegration.setCommitStatus(sha, gitJson);
        SlackIntegration.notifySlack(slackJson);
    }
}

