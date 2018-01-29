package org.eclipse.recommenders.internal.snipmatch.rcp.completion;

import static org.eclipse.recommenders.coordinates.DependencyInfo.PROJECT_NAME;
import static org.eclipse.recommenders.internal.snipmatch.rcp.completion.RepositoryProposalMatcher.repository;
import static org.eclipse.recommenders.internal.snipmatch.rcp.util.SearchContextMatcher.context;
import static org.eclipse.recommenders.internal.snipmatch.rcp.util.SnippetProposalMatcher.snippet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.DependencyType;
import org.eclipse.recommenders.coordinates.IDependencyListener;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.internal.snipmatch.rcp.Repositories;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.Location;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.recommenders.snipmatch.rcp.model.EclipseGitSnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.rcp.model.SnipmatchRcpModelFactory;
import org.eclipse.recommenders.snipmatch.rcp.model.SnippetRepositoryConfigurations;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Result;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class JavaContentAssistProcessorTest {

    private static final String REPO_NAME_1 = "my repo 1";
    private static final String REPO_NAME_2 = "my repo 2";
    private static final String SNIPPET_CODE = "code";
    private static final String ANY_SEARCH_TERM = "";
    private static final String SEARCH_TERM = "searchTerm";
    private static final List<String> NO_EXTRA_SEARCH_TERMS = Collections.emptyList();
    private static final List<String> NO_TAGS = Collections.emptyList();
    private static final List<String> NO_FILENAME_RESTRICTIONS = Collections.emptyList();
    private static final ImmutableSet<DependencyInfo> NO_DEPENDENCIES = ImmutableSet.of();
    private static final String NO_SELECTION = null;

    private static final Document DOCUMENT = new Document("Document");

    private static final ProjectCoordinate EXAMPLE_COORDINATE = new ProjectCoordinate("org.example", "example",
            "1.0.0");
    private static final Set<ProjectCoordinate> NO_PROJECT_COORDINATES = ImmutableSet.of();
    private static final Set<ProjectCoordinate> PROJECT_COORDINATES = ImmutableSet.of(EXAMPLE_COORDINATE);

    private static final File PROJECT_DIR = new File("/tmp/example");
    private static final DependencyInfo PROJECT_INFO = new DependencyInfo(PROJECT_DIR, DependencyType.PROJECT,
            ImmutableMap.of(PROJECT_NAME, "example"));

    private static final ImmutableSet<DependencyInfo> DEPENDENCIES = ImmutableSet.of(PROJECT_INFO);

    private SnippetRepositoryConfigurations configs;
    private Repositories repos;
    private JavaContentAssistProcessor sut;
    private ITextViewer viewer;

    public void setUp(Document document, Point selectedRange, ImmutableSet<DependencyInfo> dependencies)
            throws Exception {
        IDependencyListener dependencyListener = mock(IDependencyListener.class);

        IProjectCoordinateProvider pcProvider = mock(IProjectCoordinateProvider.class);

        IJavaProject javaProject = mock(IJavaProject.class, RETURNS_DEEP_STUBS);
        when(javaProject.getProject().getLocation().toFile()).thenReturn(PROJECT_DIR);
        when(javaProject.getElementName()).thenReturn("example");
        IClasspathEntry[] resolvedClasspath = new IClasspathEntry[0];
        when(javaProject.getResolvedClasspath(Mockito.anyBoolean())).thenReturn(resolvedClasspath);

        when(pcProvider.tryResolve(PROJECT_INFO)).thenReturn(Result.of(EXAMPLE_COORDINATE));
        when(dependencyListener.getDependenciesForProject(PROJECT_INFO)).thenReturn(dependencies);

        ICompilationUnit compilationUnit = mock(ICompilationUnit.class);
        when(compilationUnit.getJavaProject()).thenReturn(javaProject);
        IResource resource = mock(IResource.class);
        when(compilationUnit.getResource()).thenReturn(resource);

        configs = SnipmatchRcpModelFactory.eINSTANCE.createSnippetRepositoryConfigurations();

        repos = mock(Repositories.class);

        viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(document);
        when(viewer.getSelectedRange()).thenReturn(selectedRange);

        JavaContentAssistInvocationContext context = spy(
                new JavaContentAssistInvocationContext(viewer, 0, mock(IEditorPart.class)));
        doReturn(compilationUnit).when(context).getCompilationUnit();

        sut = new JavaContentAssistProcessor(configs, repos, pcProvider, dependencyListener, new SharedImages());
        sut.setFilename("Test.java");

        sut.setContext(context);
    }

    @Test
    public void testEmptySearchText() throws Exception {
        setUp(DOCUMENT, new Point(2, 0), NO_DEPENDENCIES);

        ISnippetRepository repo = mockRepository(REPO_NAME_1, 10, ANY_SEARCH_TERM, Location.JAVA_FILE,
                NO_PROJECT_COORDINATES, "snippet");

        sut.setTerms("");

        verifyZeroInteractions(repo);
        List<ICompletionProposal> result = Arrays.asList(sut.computeCompletionProposals(viewer, 0));
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void testSnippetIsFound() throws Exception {
        setUp(DOCUMENT, new Point(2, 0), NO_DEPENDENCIES);

        ISnippetRepository repo = mockRepository(REPO_NAME_1, 10, SEARCH_TERM, Location.JAVA_FILE,
                NO_PROJECT_COORDINATES, "snippet");

        sut.setTerms(SEARCH_TERM);

        List<ICompletionProposal> result = Arrays.asList(sut.computeCompletionProposals(viewer, 0));

        verify(repo).search(argThat(context(SEARCH_TERM, Location.JAVA_FILE, NO_PROJECT_COORDINATES)));
        assertThat(result, hasItem(repository(REPO_NAME_1, 1, 0)));
        assertThat(result, hasItem(snippet("snippet", 0, NO_SELECTION)));
        assertThat(result.size(), is(2));
    }

    @Test
    public void testSnippetsAreFound() throws Exception {
        setUp(DOCUMENT, new Point(2, 0), NO_DEPENDENCIES);

        ISnippetRepository repo = mockRepository(REPO_NAME_1, 10, SEARCH_TERM, Location.JAVA_FILE,
                NO_PROJECT_COORDINATES, "snippet1", "snippet2");

        sut.setTerms(SEARCH_TERM);

        List<ICompletionProposal> result = Arrays.asList(sut.computeCompletionProposals(viewer, 0));

        verify(repo).search(argThat(context(SEARCH_TERM, Location.JAVA_FILE, NO_PROJECT_COORDINATES)));
        assertThat(result, hasItem(repository(REPO_NAME_1, 2, 0)));
        assertThat(result, hasItem(snippet("snippet1", 0, NO_SELECTION)));
        assertThat(result, hasItem(snippet("snippet2", 0, NO_SELECTION)));
        assertThat(result.size(), is(3));
    }

    @Test
    public void testSnippetsInTwoRepos() throws Exception {
        setUp(DOCUMENT, new Point(2, 0), NO_DEPENDENCIES);

        ISnippetRepository repo1 = mockRepository(REPO_NAME_1, 20, SEARCH_TERM, Location.JAVA_FILE,
                NO_PROJECT_COORDINATES, "snippet1");
        ISnippetRepository repo2 = mockRepository(REPO_NAME_2, 10, SEARCH_TERM, Location.JAVA_FILE,
                NO_PROJECT_COORDINATES, "snippet2");

        sut.setTerms(SEARCH_TERM);

        List<ICompletionProposal> result = Arrays.asList(sut.computeCompletionProposals(viewer, 0));

        verify(repo1).search(argThat(context(SEARCH_TERM, Location.JAVA_FILE, NO_PROJECT_COORDINATES)));
        verify(repo2).search(argThat(context(SEARCH_TERM, Location.JAVA_FILE, NO_PROJECT_COORDINATES)));
        assertThat(result, hasItem(repository(REPO_NAME_1, 1, 1)));
        assertThat(result, hasItem(snippet("snippet1", 1, NO_SELECTION)));
        assertThat(result, hasItem(repository(REPO_NAME_2, 1, 0)));
        assertThat(result, hasItem(snippet("snippet2", 0, NO_SELECTION)));
        assertThat(result.size(), is(4));
    }

    @Test
    public void testLocationIsPassedToSearch() throws Exception {
        setUp(DOCUMENT, new Point(2, 0), NO_DEPENDENCIES);

        ISnippetRepository repo1 = mockRepository(REPO_NAME_1, 10, SEARCH_TERM, Location.JAVADOC,
                NO_PROJECT_COORDINATES, "snippet1");
        ISnippetRepository repo2 = mockRepository(REPO_NAME_2, 20, SEARCH_TERM, Location.JAVA_FILE,
                NO_PROJECT_COORDINATES, "snippet2");

        sut.setTerms(SEARCH_TERM);

        List<ICompletionProposal> result = Arrays.asList(sut.computeCompletionProposals(viewer, 0));

        verify(repo1).search(argThat(context(SEARCH_TERM, Location.JAVA_FILE, NO_PROJECT_COORDINATES)));
        verify(repo2).search(argThat(context(SEARCH_TERM, Location.JAVA_FILE, NO_PROJECT_COORDINATES)));
        assertThat(result, hasItem(repository(REPO_NAME_2, 1, 1)));
        assertThat(result, hasItem(snippet("snippet2", 1, NO_SELECTION)));
        assertThat(result.size(), is(2));
    }

    @Test
    public void testSelection() throws Exception {
        setUp(DOCUMENT, new Point(2, 2), NO_DEPENDENCIES);

        ISnippetRepository repo = mockRepository(REPO_NAME_1, 10, SEARCH_TERM, Location.JAVA_FILE,
                NO_PROJECT_COORDINATES, "snippet");

        sut.setTerms(SEARCH_TERM);

        List<ICompletionProposal> result = Arrays.asList(sut.computeCompletionProposals(viewer, 0));

        verify(repo).search(argThat(context(SEARCH_TERM, Location.JAVA_FILE, NO_PROJECT_COORDINATES)));
        assertThat(result, hasItem(repository(REPO_NAME_1, 1, 0)));
        assertThat(result, hasItem(snippet("snippet", 0, "cu")));
        assertThat(result.size(), is(2));
    }

    @Test
    public void testDependency() throws Exception {
        Document document = spy(DOCUMENT);
        doReturn(IJavaPartitions.JAVA_DOC).when(document).getContentType(eq("___java_partitioning"), eq(0), eq(true));
        setUp(document, new Point(2, 0), DEPENDENCIES);

        ISnippetRepository repo = mockRepository(REPO_NAME_1, 10, SEARCH_TERM, Location.JAVADOC, PROJECT_COORDINATES,
                "snippet");

        sut.setTerms(SEARCH_TERM);

        List<ICompletionProposal> result = Arrays.asList(sut.computeCompletionProposals(viewer, 0));

        verify(repo).search(argThat(context(SEARCH_TERM, Location.JAVADOC, PROJECT_COORDINATES)));
        assertThat(result, hasItem(repository(REPO_NAME_1, 1, 0)));
        assertThat(result, hasItem(snippet("snippet", 0, NO_SELECTION)));
        assertThat(result.size(), is(2));
    }

    private ISnippetRepository mockRepository(String name, int priority, String searchTerm, Location location,
            Set<ProjectCoordinate> dependencies, String... snippetNames) {
        ArrayList<Recommendation<ISnippet>> recommendations = new ArrayList<>();
        for (String snippetName : snippetNames) {
            recommendations.add(createRecommendation(snippetName));
        }
        ISnippetRepository repo = mock(ISnippetRepository.class);

        when(repo.search(argThat(context(searchTerm, location, dependencies)))).thenReturn(recommendations);

        String id = UUID.randomUUID().toString();
        when(repos.getRepository(id)).thenReturn(Optional.of(repo));

        EclipseGitSnippetRepositoryConfiguration config = SnipmatchRcpModelFactory.eINSTANCE
                .createEclipseGitSnippetRepositoryConfiguration();
        config.setPriority(priority);
        config.setId(id);
        config.setName(name);
        configs.getRepos().add(config);

        return repo;
    }

    private Recommendation<ISnippet> createRecommendation(String name) {
        ISnippet snippet = new Snippet(UUID.randomUUID(), name, ANY_SEARCH_TERM, NO_EXTRA_SEARCH_TERMS, NO_TAGS,
                SNIPPET_CODE, Location.JAVA_FILE, NO_FILENAME_RESTRICTIONS, Collections.<ProjectCoordinate>emptySet());
        return Recommendation.newRecommendation(snippet, 1.0);
    }
}
