package ciserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;

import org.json.*;

class ContinuousIntegrationServerTest {
    @Test
    public void testSample() {
        assertEquals(5, 5);
    }

    /**
     * Tests the functionality of the parseJSON function, both that
     * the error handling works and that it returns the url and ref
     * of requests on the correct format,
     */
    @Test
    public void testJSONErrorHandling() {
        ContinuousIntegrationServer ci = new ContinuousIntegrationServer();
        MockHttpServletRequest request = new MockHttpServletRequest();
        try { //An empty request
            ci.parseJSON(request);
            fail("An empty request should result in an error");
        } catch (java.lang.Error e) {}

        byte[] b = "dummy".getBytes();
        request.setContent(b);
        try { //A request with wrong content
            ci.parseJSON(request);
            fail("A request with wrong content should also result in an error");
        } catch (java.lang.Error e) {}


        MockHttpServletRequest request2 = new MockHttpServletRequest();
        JSONObject obj = new JSONObject();
        JSONObject repo = new JSONObject();
        repo.put("url", "test_url");
        obj.put("repository", repo);
        obj.put("ref", "test_ref");
        byte[] jsonBytes = obj.toString().getBytes();
        request2.setContent(jsonBytes); //A request on the correct format
        String[] result = ci.parseJSON(request2);

        assertEquals(result[0], "test_url");
        assertEquals(result[1], "test_ref");
    }
}
