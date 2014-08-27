package org.eclipse.recommenders.subwords.rcp.it;

import static java.util.Arrays.asList;
import static org.eclipse.recommenders.testing.CodeBuilder.*;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.completion.rcp.it.MockedIntelligentCompletionProposalComputer;
import org.eclipse.recommenders.completion.rcp.processable.BaseRelevanceSessionProcessor;
import org.eclipse.recommenders.completion.rcp.processable.IntelligentCompletionProposalComputer;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessorDescriptor;
import org.eclipse.recommenders.internal.completion.rcp.CompletionRcpPreferences;
import org.eclipse.recommenders.internal.rcp.CachingAstProvider;
import org.eclipse.recommenders.internal.subwords.rcp.SubwordsRcpPreferences;
import org.eclipse.recommenders.internal.subwords.rcp.SubwordsSessionProcessor;
import org.eclipse.recommenders.testing.jdt.JavaProjectFixture;
import org.eclipse.recommenders.testing.rcp.jdt.JavaContentAssistContextMock;
import org.eclipse.recommenders.utils.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
@RunWith(Parameterized.class)
public class SubwordsCompletionProposalComputerIntegrationTest {

    private static final int MIN_SUBWORDS_MATCH_RELEVANCE = Integer.MIN_VALUE;
    private static final int MAX_SUBWORDS_MATCH_RELEVANCE = -1;
    private static final int MIN_PREFIX_MATCH_RELEVANCE = 0;
    private static final int MAX_PREFIX_MATCH_RELEVANCE = Integer.MAX_VALUE;

    private static final SubwordsRcpPreferences COMPREHENSIVE = new SubwordsRcpPreferences();

    private final JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "test");

    private final CharSequence code;
    private final SubwordsRcpPreferences preferences;
    private final int minRelevance;
    private final int maxRelevance;
    private final List<String> expectedProposals;

    public SubwordsCompletionProposalComputerIntegrationTest(String description, CharSequence code,
            SubwordsRcpPreferences preferences, int minRelevance, int maxRelevance, List<String> expectedProposals) {
        this.code = code;
        this.preferences = preferences;
        this.minRelevance = minRelevance;
        this.maxRelevance = maxRelevance;
        this.expectedProposals = expectedProposals;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario("Methods of local variable", method("Object obj = null; obj.hc$"), COMPREHENSIVE,
                MIN_SUBWORDS_MATCH_RELEVANCE, MAX_SUBWORDS_MATCH_RELEVANCE, "hashCode"));

        scenarios.add(scenario("Methods of local variable (upper-case)", method("Object obj = null; obj.C$"),
                COMPREHENSIVE, MIN_SUBWORDS_MATCH_RELEVANCE, MAX_SUBWORDS_MATCH_RELEVANCE, "hashCode", "getClass"));

        scenarios.add(scenario("Methods of local variable's supertype", method("InputStream in = null; in.hc$"),
                COMPREHENSIVE, MIN_SUBWORDS_MATCH_RELEVANCE, MAX_SUBWORDS_MATCH_RELEVANCE, "hashCode"));

        scenarios.add(scenario("Constructors in initialization expression", method("InputStream in = new Ziut$"),
                COMPREHENSIVE, MIN_SUBWORDS_MATCH_RELEVANCE, MAX_SUBWORDS_MATCH_RELEVANCE,
                "ZipInputStream(" /* InputStream */, "ZipInputStream(" /* InputStream, Charset */));

        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=435745
        scenarios.add(scenario("Constructors in standalone expression", method("new Ziut$"), COMPREHENSIVE,
                MIN_SUBWORDS_MATCH_RELEVANCE, MAX_SUBWORDS_MATCH_RELEVANCE, "ZipInputStream("));

        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=435745
        scenarios.add(scenario("Return types of method declaration", classbody("public Ziut$ method() { }"),
                COMPREHENSIVE, MIN_SUBWORDS_MATCH_RELEVANCE, MAX_SUBWORDS_MATCH_RELEVANCE, "ZipInputStream"));

        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=435745
        scenarios.add(scenario("Parameter types of method declaration", classbody("public void method(Ziut$) { }"),
                COMPREHENSIVE, MIN_SUBWORDS_MATCH_RELEVANCE, MAX_SUBWORDS_MATCH_RELEVANCE, "ZipInputStream"));

        scenarios.add(scenario("Generated getters/setters", classbody("ZipInputStream zipInputStream; ziut$"),
                COMPREHENSIVE, MIN_SUBWORDS_MATCH_RELEVANCE, MAX_SUBWORDS_MATCH_RELEVANCE, "getZipInputStream()",
                "setZipInputStream(ZipInputStream)"));

        scenarios.add(scenario("Subwords Type match", classbody("AaaXyzAaa", "public void method() { Xyz$ }"),
                COMPREHENSIVE, MIN_SUBWORDS_MATCH_RELEVANCE, MAX_SUBWORDS_MATCH_RELEVANCE, "AaaXyzAaa"));

        scenarios.add(scenario("Package subwords match", method("Sys$"), COMPREHENSIVE, MIN_SUBWORDS_MATCH_RELEVANCE,
                MAX_SUBWORDS_MATCH_RELEVANCE, "sun.security.internal.spec"));

        scenarios.add(scenario("Exact Prefix match", classbody("BbbXyzBbb", "public void method() { Bbb$ }"),
                COMPREHENSIVE, MIN_PREFIX_MATCH_RELEVANCE, MAX_PREFIX_MATCH_RELEVANCE, "BbbXyzBbb"));

        return scenarios;
    }

    private static Object[] scenario(String description, CharSequence code, SubwordsRcpPreferences preferences,
            int minRelevance, int maxRelevance, String... expectedProposals) {
        return new Object[] { description, code, preferences, minRelevance, maxRelevance, asList(expectedProposals) };
    }

    @Test
    public void test() throws Exception {
        warmup(code, preferences);

        List<IJavaCompletionProposal> proposals = exercise(code, preferences);
        for (String expectedProposal : expectedProposals) {
            boolean found = false;
            for (IJavaCompletionProposal proposal : proposals) {
                if (!proposal.getDisplayString().startsWith(expectedProposal)) {
                    continue;
                }
                found = true;

                int relevance = proposal.getRelevance();
                if (relevance >= minRelevance && relevance <= maxRelevance) {
                    break;
                }
                fail(String.format(
                        "Encountered proposal %s with a relevance %d. Expected a relevance between %d and %d",
                        expectedProposal, relevance, minRelevance, maxRelevance));
            }
            if (!found) {
                fail(String.format("Expected proposal %s not found", expectedProposal));
            }
        }
    }

    @Before
    public void setUp() throws CoreException {
        fixture.clear();
    }

    private void warmup(CharSequence code, SubwordsRcpPreferences preferences) throws CoreException {
        exercise(code, preferences);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<IJavaCompletionProposal> exercise(CharSequence code, SubwordsRcpPreferences preferences)
            throws CoreException {
        Pair<ICompilationUnit, Set<Integer>> struct = fixture.createFileAndParseWithMarkers(code.toString());
        ICompilationUnit cu = struct.getFirst();
        JavaUI.openInEditor(cu);

        Integer completionIndex = Iterables.getOnlyElement(struct.getSecond());

        JavaContentAssistContextMock ctx = new JavaContentAssistContextMock(cu, completionIndex);
        SessionProcessor processor = new SubwordsSessionProcessor(new CachingAstProvider(), preferences);
        SessionProcessor baseRelevanceSessionProcessor = new BaseRelevanceSessionProcessor();

        CompletionRcpPreferences prefs = Mockito.mock(CompletionRcpPreferences.class);
        Mockito.when(prefs.getEnabledSessionProcessors()).thenReturn(
                ImmutableSet.of(new SessionProcessorDescriptor("base", "base", "desc", null, 0, true, "",
                        baseRelevanceSessionProcessor), new SessionProcessorDescriptor("subwords", "name", "desc",
                        null, 0, true, "", processor)));

        IntelligentCompletionProposalComputer sut = new MockedIntelligentCompletionProposalComputer(processor, prefs);
        sut.sessionStarted();

        List<ICompletionProposal> actual = sut.computeCompletionProposals(ctx, new NullProgressMonitor());

        return cast(actual);
    }
}
