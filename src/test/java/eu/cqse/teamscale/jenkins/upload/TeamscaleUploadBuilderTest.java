package eu.cqse.teamscale.jenkins.upload;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SingleFileSCM;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
public class TeamscaleUploadBuilderTest {

    private final String url = "localhost:8100";
    private final String teamscaleProject = "jenkinsplugin";
    private final String partition = "simple";
    private final String uploadMessage = "Uploaded simple coverage";
    private final String fileFormat = "**/*.simple";
    private final String reportFormatId = "SIMPLE";

    private JenkinsRule jenkins;

    @BeforeEach
    public void setUp(JenkinsRule r) {
        this.jenkins = r;
    }

    @ParameterizedTest
    @ValueSource(strings = {"a"})
    @NullSource
    public void testConfigRoundtrip(String repository) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        TeamscaleUploadBuilder teamscaleUpload1 = new TeamscaleUploadBuilder(
                url, "teamscale_id", teamscaleProject, partition, uploadMessage, fileFormat, reportFormatId, "");
        teamscaleUpload1.setRepository(repository);
        project.getPublishersList().add(teamscaleUpload1);
        project = jenkins.configRoundtrip(project);
        TeamscaleUploadBuilder teamscaleUpload2 = new TeamscaleUploadBuilder(
                url, "teamscale_id", teamscaleProject, partition, uploadMessage, fileFormat, reportFormatId, "");
        teamscaleUpload2.setRepository(repository);
        jenkins.assertEqualDataBoundBeans(
                teamscaleUpload2, project.getPublishersList().get(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a"})
    @NullSource
    public void testPipelineWithoutCredentials(String repository) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setScm(new SingleFileSCM("test.simple", "RunExec.java\n8-10"));
        TeamscaleUploadBuilder publisher = new TeamscaleUploadBuilder(
                url, "teamscale_id", teamscaleProject, partition, uploadMessage, fileFormat, reportFormatId, "");
        publisher.setRepository(repository);
        project.getPublishersList().add(publisher);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("credentials are null", build);
    }
}
