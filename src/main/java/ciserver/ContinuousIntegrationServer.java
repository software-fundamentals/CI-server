package ciserver;

import javax.servlet.ServletInputStream;
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

import org.json.*;
import org.json.JSONArray;

import java.util.Arrays;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

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


        System.out.println("Request method:");
        System.out.println(request.getMethod());

        JSONObject obj = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
        String url = obj.getJSONObject("repository").getString("url");
        String branch = obj.getString("ref");

        try {
            Git repo = Git.cloneRepository()
            .setURI(url)
            .setDirectory(new File("~/CI"))
            .call();
            System.out.println(repo.getRepository());
        } catch (Exception e) {
            e.printStackTrace();
        }


//        EXAMPLE: This is how to get strings and object from the payload
//        System.out.println(obj.getJSONObject("repository").getString("ssh_url"));

        response.getWriter().println("CI up and running");

        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        // 2nd compile the code


//        TODO: Make the gradle build code below a complete and standalone function
//        final Process p = Runtime.getRuntime().exec("gradle build");
//
//        new Thread(new Runnable() {
//            public void run() {
//                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                String line = null;
//
//                try {
//                    while ((line = input.readLine()) != null)
//                        System.out.println(line);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//
//        try {
//            p.waitFor();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


    }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8989);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}
