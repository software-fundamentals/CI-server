package notification;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationJson {

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
}