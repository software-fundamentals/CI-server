package notification;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationJson {

    /**
     * Method for creating a JSON object that Slack can interpret.
     * @param authorName who made the commit
     * @param authorUrl link to author's github profile
     * @param title title of message
     * @param titleLink link to github page
     * @param text contents of the message
     * @param color color of slack message styling,
     * can be "good", "danger", "warning" or hex value strings.
     * @return a JSON object according to slack format
     */
    public static JSONObject createSlackJson(String authorName, String authorUrl, String title, String titleLink, String text, String color) {
        JSONObject attachment = new JSONObject();
        attachment.put("fallback", "New build: " + title);
        attachment.put("author_name", authorName);
        attachment.put("author_url", authorUrl);
        attachment.put("title", title);
        attachment.put("title_link", titleLink);
        attachment.put("text", text);
        attachment.put("color", color);
        attachment.put("footer", "Continuous Integration Server");

        JSONArray attachments = new JSONArray();
        attachments.put(attachment);
        JSONObject json = new JSONObject();
        json.put("attachments", attachments);
        return json;
    }

    /**
     * Method for creating a JSON that can set a commit status on GitHub.
     * @param state "success" or "failure", will set the status to a check or a cross
     * @param targetUrl a url to learn more
     * @param description A short descriptive text about the status
     * @param context origin of the status
     * @return a JSON object according to git commit status format.
     */
    public static JSONObject createCommitJson(String state, String targetUrl, String description, String context) {
        JSONObject json = new JSONObject();
        json.put("state", state);
        json.put("target_url", targetUrl);
        json.put("description", description);
        json.put("context", context);
        return json;
    }
}