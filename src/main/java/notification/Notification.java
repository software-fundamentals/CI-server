package notification;

import javax.servlet.http.HttpServletRequest;

import notification.NotificationJson;
import notification.SlackIntegration;
import utils.BuildStatus;

import java.io.IOException;
import java.util.stream.Collectors;

import org.json.JSONObject;


public class Notification {

    private static final String GIT_HEADER = "X-GitHub-Event";
    private static final String PULL = "pull_request";
    private static final String PUSH = "push";

    public void createNotificationFromRequest(HttpServletRequest request, BuildStatus status, String message) throws IOException {
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

        switch (status) {
            case SUCCESS:
                color = "success";
                text += "SUCCESS";
                break;
            case FAILURE:
                color = "danger";
                text += "FAILURE";
                break;
            default:
                return;
        }

        text += "\n" + message;
        NotificationJson.createSlackJson(authorName, authorUrl, title, titleLink, text, color);
    }

    // public void notifySuccess() {
    //     //slackIntegration.notifySlack(url, requestHeader, branch, success, sender, senderURL, titleLink);
    // }

    // public void notifyFailure() {
    //     // slackIntegration.notifySlack(endpoint, eventType, branch, success, sender, senderURL, titleLink);
    // }
}

