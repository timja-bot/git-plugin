package hudson.plugins.git.browser;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.FreeStyleProject;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import org.jenkinsci.plugins.gitclient.JGitTool;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class TFS2013GitRepositoryBrowserXSSTest {

    @Rule
    public final JenkinsRule r = new JenkinsRule();

    @Test
    @Issue("SECURITY-1723")
    public void testXSS() throws Exception {
        // setup scm
        GitSCM scm = new GitSCM(
                Collections.singletonList(new UserRemoteConfig("http://tfs/tfs/project/_git/repo", null, null, null)),
                new ArrayList<>(),
                null, JGitTool.MAGIC_EXENAME,
                Collections.emptyList());
        scm.setBrowser(new TFS2013GitRepositoryBrowser("<img src=x onerror=alert(232)>"));

        FreeStyleProject p = r.createFreeStyleProject();
        p.setScm(scm);

        AtomicBoolean xss = new AtomicBoolean(false);
        JenkinsRule.WebClient wc = r.createWebClient();
        wc.setAlertHandler((page, s) -> xss.set(true));
        HtmlPage page = wc.getPage(p, "configure");
        Assert.assertFalse(xss.get());
    }
}
