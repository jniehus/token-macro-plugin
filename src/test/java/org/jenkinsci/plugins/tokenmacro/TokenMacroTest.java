package org.jenkinsci.plugins.tokenmacro;

import com.google.common.collect.ListMultimap;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestExtension;

import java.io.IOException;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class TokenMacroTest extends HudsonTestCase {
    private StreamTaskListener listener;

    public void testBasics() throws Exception {
        FreeStyleProject p = createFreeStyleProject("foo");
        FreeStyleBuild b = p.scheduleBuild2(0).get();

        listener = new StreamTaskListener(System.out);
        assertEquals(hudson.getRootUrl()+"job/foo/1/",TokenMacro.expand(b, listener,"${BUILD_URL}"));
        assertEquals(hudson.getRootUrl()+"job/foo/1/",TokenMacro.expand(b, listener,"$BUILD_URL"));

        assertEquals("{abc=[def, ghi], jkl=[true]}",TokenMacro.expand(b,listener,"${TEST,abc=\"def\",abc=\"ghi\",jkl=true}"));
    }

    @TestExtension
    public static class TestMacro extends TokenMacro {
        @Override
        public boolean acceptsMacroName(String macroName) {
            return macroName.equals("TEST");
        }

        @Override
        public String evaluate(AbstractBuild<?, ?> context, TaskListener listener, String macroName, Map<String, String> arguments, ListMultimap<String, String> argumentMultimap) throws MacroEvaluationException, IOException, InterruptedException {
            return argumentMultimap.toString();
        }
    }
}
