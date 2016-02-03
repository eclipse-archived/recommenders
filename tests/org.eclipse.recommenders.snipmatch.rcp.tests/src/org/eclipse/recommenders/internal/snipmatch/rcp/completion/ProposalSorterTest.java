package org.eclipse.recommenders.internal.snipmatch.rcp.completion;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.junit.Before;
import org.junit.Test;

public class ProposalSorterTest {

    private ProposalSorter sut;

    @Before
    public void setUp() {
        sut = new ProposalSorter();
    }

    @Test
    public void testCompareSnippetsByRepositoryPriority() throws Exception {
        SnippetProposal first = mockSnippetProposal(0, 1.0, "snippet");
        SnippetProposal second = mockSnippetProposal(1, 1.0, "snippet");

        assertThatFirstProposalComesFirst(first, second);
    }

    @Test
    public void testCompareSnippetsByRelevance() throws Exception {
        SnippetProposal first = mockSnippetProposal(0, 1.0, "snippet");
        SnippetProposal second = mockSnippetProposal(0, 0.5, "snippet");

        assertThatFirstProposalComesFirst(first, second);
    }

    @Test
    public void testCompareSnippetsByName() throws Exception {
        SnippetProposal first = mockSnippetProposal(0, 1.0, "snippet1");
        SnippetProposal second = mockSnippetProposal(0, 1.0, "snippet2");

        assertThatFirstProposalComesFirst(first, second);
    }

    @Test
    public void testCompareRepositories() throws Exception {
        RepositoryProposal first = mockRepositoryProposal(0, "repository");
        RepositoryProposal second = mockRepositoryProposal(1, "repository");

        assertThatFirstProposalComesFirst(first, second);
    }

    @Test
    public void testCompareRepositoryWithOwnSnippet() throws Exception {
        RepositoryProposal first = mockRepositoryProposal(0, "repository");
        SnippetProposal second = mockSnippetProposal(0, 1.0, "snippet");

        assertThatFirstProposalComesFirst(first, second);
    }

    @Test
    public void testCompareRepositoryWithSnippetFromHigherPriorityRepository() throws Exception {
        RepositoryProposal first = mockRepositoryProposal(0, "repository");
        SnippetProposal second = mockSnippetProposal(1, 1.0, "snippet");

        assertThatFirstProposalComesFirst(first, second);
    }

    @Test
    public void testCompareRepositoryWithSnippetFromLowPriorityRepository() throws Exception {
        SnippetProposal first = mockSnippetProposal(0, 1.0, "snippet");
        RepositoryProposal second = mockRepositoryProposal(1, "repository");

        assertThatFirstProposalComesFirst(first, second);
    }

    private void assertThatFirstProposalComesFirst(ICompletionProposal first, ICompletionProposal second) {
        assertThat(sut.compare(first, second), is(lessThan(0)));
        assertThat(sut.compare(second, first), is(greaterThan(0)));
    }

    @SuppressWarnings("unchecked")
    private SnippetProposal mockSnippetProposal(int repositoryPriority, double relevance, String name) throws Exception {
        ISnippet snippet = mock(ISnippet.class);
        when(snippet.getName()).thenReturn(name);
        Recommendation<ISnippet> recommendation = mock(Recommendation.class);
        when(recommendation.getProposal()).thenReturn(snippet);
        when(recommendation.getRelevance()).thenReturn(relevance);

        Template template = mock(Template.class);
        TemplateContext context = mock(TemplateContext.class);
        Region region = mock(Region.class);
        Device device = mock(Device.class);
        Image image = new Image(device, 1, 1);

        return SnippetProposal.newSnippetProposal(recommendation, repositoryPriority, template, context, region, image);
    }

    private RepositoryProposal mockRepositoryProposal(int repositoryPriority, String name) {
        SnippetRepositoryConfiguration config = mock(SnippetRepositoryConfiguration.class);
        when(config.getName()).thenReturn(name);

        return new RepositoryProposal(config, repositoryPriority, 1);
    }
}
