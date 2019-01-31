package ciserver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.json.JSONObject;
import org.json.JSONArray;

/**
  Skeleton of a ContinuousIntegrationServer which acts as webhook
  See the Jetty documentation for API documentation of those classes.
  */
public class ContinuousIntegrationServer extends AbstractHandler
{
    public void handle(String target,
            Request baseRequest,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);
        System.out.println("Request");
        System.out.println(request);
        System.out.println(baseRequest.getContext());
        System.out.println("Response");
        System.out.println(response);


        String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        System.out.println(body);
        response.getWriter().println(body);
        //response.getWriter().println("CI job done");
//        Runtime rt = Runtime.getRuntime();
//        Process pr = rt.exec("gradle build");
//        Process ps = rt.exec("gradle test");

        final Process p = Runtime.getRuntime().exec("gradle build");

        new Thread(new Runnable() {
            public void run() {
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = null;

                try {
                    while ((line = input.readLine()) != null)
                        System.out.println(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        // 2nd compile the code


    }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}
