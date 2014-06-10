package org.eclipse.recommenders.completion.rcp.it;

import static org.eclipse.jdt.ui.PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext;
import org.eclipse.recommenders.internal.rcp.CachingAstProvider;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Pair;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Test that Content Assist Favorites, i.e., static members that are always considered to be imported, are collected as
 * proposals.
 *
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=437045">Bug 437045</a>
 */
@RunWith(Parameterized.class)
public class Bug437045Test {

    private final String expected;
    private final String preference;

    private String previousPreference;

    public Bug437045Test(String expected, String preference) {
        this.expected = expected;
        this.preference = preference;
    }

    @Parameters(name = "{index}: favorites {2} => {1}")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario("sqrt", "java.lang.Math.sqrt"));
        scenarios.add(scenario("sqrt", "java.lang.Math.*"));

        scenarios.add(scenario("currentTimeMillis", "java.lang.System.currentTimeMillis", "java.lang.Math.sqrt"));
        scenarios.add(scenario("currentTimeMillis", "java.lang.Math.sqrt", "java.lang.System.currentTimeMillis"));

        scenarios.add(scenario("currentTimeMillis", "java.lang.System.currentTimeMillis", "java.lang.Math.*"));
        scenarios.add(scenario("currentTimeMillis", "java.lang.Math.*", "java.lang.System.currentTimeMillis"));

        return scenarios;
    }

    private static Object[] scenario(String expected, String... favorites) {
        return new Object[] { expected, Joiner.on(';').join(favorites) };
    }

    @Before
    public void setUp() {
        previousPreference = PreferenceConstants.getPreferenceStore().getString(CODEASSIST_FAVORITE_STATIC_MEMBERS);
        PreferenceConstants.getPreferenceStore().setValue(CODEASSIST_FAVORITE_STATIC_MEMBERS, preference);
    }

    @After
    public void tearDown() {
        PreferenceConstants.getPreferenceStore().setValue(CODEASSIST_FAVORITE_STATIC_MEMBERS, previousPreference);
    }

    @Test
    public void testReceiverTypeOfInstanceMethod() throws Exception {
        CharSequence code = CodeBuilder.method("$");

        IRecommendersCompletionContext sut = exercise(code);
        Set<IJavaCompletionProposal> proposals = sut.getProposals().keySet();

        assertThat(proposals, hasItem(hasDisplayString(startsWith(expected))));
    }

    private IRecommendersCompletionContext exercise(CharSequence code) throws CoreException {
        JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "test");
        Pair<ICompilationUnit, Set<Integer>> struct = fixture.createFileAndParseWithMarkers(code.toString());
        ICompilationUnit cu = struct.getFirst();
        int completionIndex = struct.getSecond().iterator().next();
        JavaContentAssistInvocationContext ctx = new JavaContentAssistContextMock(cu, completionIndex);

        return new RecommendersCompletionContext(ctx, new CachingAstProvider());
    }

    private static Matcher<IJavaCompletionProposal> hasDisplayString(Matcher<String> matcher) {
        return hasProperty("displayString", matcher);
    }
}
