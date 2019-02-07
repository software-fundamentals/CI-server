package ciserver;

public class GradleBuildOutput {

    public boolean result;
    public String log;

    GradleBuildOutput(boolean result, String log) {
        this.result = result;
        this.log = log;
    }
}
