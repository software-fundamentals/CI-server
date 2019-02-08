package notification;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationJson {

    /**
     * Creates a jsonObject to be used for the slack integration message.
     * @param authorName ...and further params... data from repo and response from gradle build
     * @return A Json object containing the appropriate notification.
     */
    public static JSONObject createSlackJson(String authorName, String authorUrl, String title, String titleLink, String text, String color) {
        JSONObject attachment = new JSONObject();
        attachment.put("fallback", "Unable to display repository status");
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