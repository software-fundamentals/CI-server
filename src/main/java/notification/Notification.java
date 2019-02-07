package notification;

import javax.servlet.http.HttpServletRequest;

import notification.NotificationJson;
import notification.SlackIntegration;

import java.io.IOException;
import java.util.stream.Collectors;

import org.json.JSONObject;


public class Notification {

    private static final String GIT_HEADER = "X-GitHub-Event";
    private static final String PUSH = "push";

    public static void createNotificationFromRequest(HttpServletRequest request, Boolean success, String message) throws IOException {
        String authorName = "", authorUrl="", title = "", titleLink = "", text = "Build status: ", color = "", branch = "", sha = "";
        JSONObject sender;

        String header = request.getHeader(GIT_HEADER);
        JSONObject jsonBody = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));

        switch (header) {
            case PUSH:
                sender = jsonBody.getJSONObject("sender");
                authorName = sender.getString("login");
                authorUrl = sender.getString("url");
                branch = jsonBody.getString("refs");
                title = "Commit on branch " + branch + ": ";
                titleLink = "";
                sha = jsonBody.getString("after");
                break;
            default:
                return;
        }

        if (success) {
            color = "success";
            text += "SUCCESS";
        } else {
            color = "danger";
            text += "FAILURE";
        }

        text += "\n" + message;
        JSONObject slackJson = NotificationJson.createSlackJson(authorName, authorUrl, title, titleLink, text, color);
        SlackIntegration.notifySlack(slackJson);
    }
}

