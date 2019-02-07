package ciserver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.*;
import java.util.stream.Collectors;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.json.*;

import java.io.IOException;

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
     * @Return {url, ref}
     */
    public String[] parseJSON(HttpServletRequest request) {
        String[] parsedData = new String[2];
        try {
            JSONObject obj = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
            try {
                parsedData[0] = obj.getJSONObject("repository").getString("url");
            } catch (JSONException e) {
                throw new java.lang.Error("Error occured parsing the GitHub url");
            }
            try {
                parsedData[1] = obj.getString("ref");
            } catch (JSONException e) {
                throw new java.lang.Error("Error occured parsing the branch name");
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

        String[] parsedData = parseJSON(request);
        if (parsedData == null)
            return;
        String url = parsedData[0];
        String branch = parsedData[1];

        String cloneDir = "~/CI";
        cloneRepo(url, cloneDir);
        pullBranch(cloneDir, branch);

        try { 
            GradleBuildOutput output = runGradle(cloneDir);
            System.out.println(output.result);
            System.out.println(output.log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8001);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}
