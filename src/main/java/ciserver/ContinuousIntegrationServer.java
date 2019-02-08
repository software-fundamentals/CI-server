package ciserver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.*;
import java.io.IOException;

import java.util.HashMap;
import java.util.stream.Collectors;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.json.*;

import notification.Notification;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;

/**
 * Skeleton of a ContinuousIntegrationServer which acts as webhook
 * See the Jetty documentation for API documentation of those classes.
 */
public class ContinuousIntegrationServer extends AbstractHandler
{
    public ContinuousIntegrationServer() {}

    /**
     * @param request HttpServletRequest from github, webhook.
     * @return parsedData, a hashmap of strings containing authorName, authorUrl, sha & compare
     */
    public HashMap<String, String> parseJSON(HttpServletRequest request) {
        HashMap<String, String> parsedData = new HashMap<String, String>();
        try {
            JSONObject obj = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
            try {
                parsedData.put("repository_url", obj.getJSONObject("repository").getString("url"));
            } catch (JSONException e) {
                throw new java.lang.Error("Error occured parsing the GitHub url");
            }
            try {
                parsedData.put("branch", obj.getString("ref"));
            } catch (JSONException e) {
                throw new java.lang.Error("Error occured parsing the branch name");
            }
            try {
                JSONObject sender = obj.getJSONObject("sender");
                parsedData.put("authorName", sender.getString("login"));
                parsedData.put("authorUrl", sender.getString("url"));
                parsedData.put("sha", obj.getString("after"));
                parsedData.put("compareUrl", obj.getString("compare"));
            } catch (JSONException e) {
                throw new java.lang.Error("Error occured parsing the sender data");
            }
        } catch (JSONException e) {
            throw new java.lang.Error("Error occured parsing the request");
        } catch (IOException e) {
            throw new java.lang.Error("Error occured parsing the request");
        } catch (NullPointerException e) {
            throw new java.lang.Error("Request was empty");
        }
        return parsedData;
    }

    /**
     * The CI-handler, executing the following tasks:
     *  1. Clones repo and checkout branch
     *  2. Run gradle build in the branch including its corresponding tests
     *  3. Emits notification status and message to slack and github
     * @param target
     * @param baseRequest
     * @param request
     * @param response
     */
    public void handle(String target,
            Request baseRequest,
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        try {
            response.getWriter().println("CI up and running");
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String, String> parsedData = parseJSON(request);
        if (parsedData == null)
            return;
        String url = parsedData.get("repository_url");
        String branch = parsedData.get("branch");

        String cloneDir = "~/CI";
        cloneRepo(url, cloneDir);
        pullBranch(cloneDir, branch);

        try {
            GradleBuildOutput output = runGradle(cloneDir);
            System.out.println(output.result);
            System.out.println(output.log);
            Notification.createNotification(parsedData.get("authorName"), parsedData.get("authorUrl"),
                                            parsedData.get("branch"), parsedData.get("compareUrl"),
                                            parsedData.get("sha"), output.result, output.log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clones the repo from the given url.
     * @param url the repo to clone
     * @param cloneDir specifying where to clone
     */
    private void cloneRepo(String url, String cloneDir) {
        try {
            Git repo = Git.cloneRepository()
                .setURI(url)
                .setDirectory(new File(cloneDir))
                .call();
            System.out.println(repo.getRepository());
            System.out.println("Successfully cloned");
        } catch (JGitInternalException e) {
            if (e.getMessage().endsWith(" already exists and is not an empty directory"))
                System.out.println("Repo already exists, clone not needed.");
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pulls the given branch.
     * @param gitDir the cloned directory
     * @param branch the name of the branch
     */
    private void pullBranch(String gitDir, String branch) {
        try {
            Repository fr = new FileRepositoryBuilder()
                .setGitDir(new File(gitDir + "/.git"))
                .readEnvironment()
                .findGitDir()
                .build();
            Git git = new Git(fr);
            git.checkout().setName("master").call();
            git.pull().call();
            System.out.println("Successfully pulled from origin");
            String shortBranchName = branch.split("refs/heads/")[1];
            git.checkout().setName("origin/" + shortBranchName).call();
            System.out.println("Successfully checked out branch " + shortBranchName);
        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes the command gradle build in the given directory.
     * @param dir The name of the directory to build gradle in
     * @return A struct containing the result of the build (boolean) and the corresponding message/log
     */
    private GradleBuildOutput runGradle(String dir) throws IOException {
        final Process p = Runtime.getRuntime().exec("gradle build -b " + dir.replace("~", "\\~") + "/build.gradle");
        StringBuilder successData = new StringBuilder();
        new Thread(new Runnable() {
            public void run() {
                String line = null;
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                try {
                    while ((line = input.readLine()) != null) {
                        successData.append(line + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            p.waitFor();
            if (p.exitValue() != 0) {
                StringBuilder errorData = new StringBuilder();
                BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line = null;
                try {
                    while ((line = error.readLine()) != null) {
                        errorData.append(line + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new GradleBuildOutput(false, errorData.toString());
            } else {
                return new GradleBuildOutput(true, successData.toString());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new GradleBuildOutput(false, "Fatal error occurred.");
        }
    }

    /**
     * Used to start the CI server in command line
     */
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8001);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}
