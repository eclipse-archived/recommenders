package org.eclipse.recommenders.subwords.rcp.it;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.recommenders.tests.CodeBuilder.*;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

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
import org.eclipse.recommenders.completion.rcp.it.JavaContentAssistContextMock;
import org.eclipse.recommenders.completion.rcp.it.MockedIntelligentCompletionProposalComputer;
import org.eclipse.recommenders.completion.rcp.processable.IntelligentCompletionProposalComputer;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessorDescriptor;
import org.eclipse.recommenders.internal.completion.rcp.CompletionRcpPreferences;
import org.eclipse.recommenders.internal.rcp.CachingAstProvider;
import org.eclipse.recommenders.internal.subwords.rcp.SubwordsRcpPreferences;
import org.eclipse.recommenders.internal.subwords.rcp.SubwordsSessionProcessor;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Pair;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
@RunWith(Parameterized.class)
public class SubwordsCompletionProposalComputerIntegrationTest {

    private static final SubwordsRcpPreferences COMPREHENSIVE = new SubwordsRcpPreferences();

    private final JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "test");

    private final CharSequence code;
    private final SubwordsRcpPreferences preferences;
    private final List<String> expectedProposals;

    public SubwordsCompletionProposalComputerIntegrationTest(String description, CharSequence code,
            SubwordsRcpPreferences preferences, List<String> expectedProposals) {
        this.code = code;
        this.preferences = preferences;
        this.expectedProposals = expectedProposals;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario("Methods of local variable", method("Object obj = null; obj.hc$"), COMPREHENSIVE,
                "hashCode"));

        scenarios.add(scenario("Methods of local variable (upper-case)", method("Object obj = null; obj.C$"),
                COMPREHENSIVE, "hashCode", "getClass"));

        scenarios.add(scenario("Methods of local variable's supertype", method("InputStream in = null; in.hc$"),
                COMPREHENSIVE, "hashCode"));

        scenarios.add(scenario("Constructors in initialization expression", method("InputStream in = new Ziut$"),
                COMPREHENSIVE, "ZipInputStream(" /* InputStream */, "ZipInputStream(" /* InputStream, Charset */));

        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=435745
        scenarios.add(scenario("Constructors in standalone expression", method("new Ziut$"), COMPREHENSIVE,
                "ZipInputStream("));

        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=435745
        scenarios.add(scenario("Return types of method declaration", classbody("public Ziut$ method() { }"),
                COMPREHENSIVE, "ZipInputStream"));

        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=435745
        scenarios.add(scenario("Parameter types of method declaration", classbody("public void method(Ziut$) { }"),
                COMPREHENSIVE, "ZipInputStream"));

        scenarios.add(scenario("Generated getters/setters", classbody("ZipInputStream zipInputStream; ziut$"),
                COMPREHENSIVE, "getZipInputStream()", "setZipInputStream(ZipInputStream)"));

        return scenarios;
    }

    private static Object[] scenario(String description, CharSequence code, SubwordsRcpPreferences preferences,
            String... expectedProposals) {
        return new Object[] { description, code, preferences, asList(expectedProposals) };
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test() throws Exception {
        warmup(code, preferences);

        List<IJavaCompletionProposal> proposals = exercise(code, preferences);

        for (String expectedProposal : expectedProposals) {
            assertThat(proposals, hasItems(Matchers.<IJavaCompletionProposal>hasProperty("displayString",
                    startsWith(expectedProposal))));
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
        CompletionRcpPreferences prefs = Mockito.mock(CompletionRcpPreferences.class);
        Mockito.when(prefs.getSessionProcessors())
                .thenReturn(
                        singletonList(new SessionProcessorDescriptor("subwords", "name", "desc", null, 0, true, "",
                                processor)));

        IntelligentCompletionProposalComputer sut = new MockedIntelligentCompletionProposalComputer(processor, prefs);
        sut.sessionStarted();

        List<ICompletionProposal> actual = sut.computeCompletionProposals(ctx, new NullProgressMonitor());

        return cast(actual);
    }
}
