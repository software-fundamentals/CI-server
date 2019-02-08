package ciserver;

/*
 * The struct containing the result (success - true, fail - false) and the message log from the gradle build.
 */
public class GradleBuildOutput {

    public boolean result;
    public String log;

    GradleBuildOutput(boolean result, String log) {
        this.result = result;
        this.log = log;
    }
}
