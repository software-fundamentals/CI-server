package notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import notification.NotificationJson;

public class NotificationJsonTest {

    @Test
    public void testSlackJson() {
        String authorName = "test name";
        String authorUrl = "test url";
        String title = "test title";
        String titleLink = "test title link";
        String text = "test text";
        String color = "test color";

        JSONObject outerObject = NotificationJson.createSlackJson(authorName, authorUrl, title, titleLink, text, color);
        JSONArray array = outerObject.getJSONArray("attachments");
        JSONObject innerObject = array.getJSONObject(0);

        assertEquals("New build: " + title, innerObject.get("fallback"));
        assertEquals(authorName, innerObject.get("author_name"));
        assertEquals(authorUrl, innerObject.get("author_url"));
        assertEquals(title, innerObject.get("title"));
        assertEquals(titleLink, innerObject.get("title_link"));
        assertEquals(text, innerObject.get("text"));
        assertEquals(color, innerObject.get("color"));
        assertEquals("Continuous Integration Server", innerObject.get("footer"));
    }

    @Test
    public void testGitHubJson() {
        String state = "test state";
        String targetUrl = "test target url";
        String description = "test description";
        String context = "test context";

        JSONObject object = NotificationJson.createCommitJson(state, targetUrl, description, context);

        assertEquals(state, object.get("state"));
        assertEquals(targetUrl, object.get("target_url"));
        assertEquals(description, object.get("description"));
        assertEquals(context, object.get("context"));
    }

}