package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.recommenders.utils.Checks.cast;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.EclipseGitSnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnipmatchFactory;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnippetRepositoryConfigurations;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class RepositoryConfigurationsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File file;

    @Before
    public void init() throws IOException {
        file = folder.newFile("configurations.config");
    }

    @Test
    public void testLoadingFromNotExistingFile() {
        SnippetRepositoryConfigurations actual = RepositoryConfigurations.loadConfigurations(file);
        assertThat(actual.getRepos().size(), is(0));
    }

    @Test
    public void testStoringAndLoadingWithEmptyConfigurationList() {
        SnippetRepositoryConfigurations storedConfigurations = SnipmatchFactory.eINSTANCE
                .createSnippetRepositoryConfigurations();
        RepositoryConfigurations.storeConfigurations(storedConfigurations, file);

        SnippetRepositoryConfigurations loadedConfiguriations = RepositoryConfigurations.loadConfigurations(file);

        assertThat(loadedConfiguriations.getRepos(), equalTo(storedConfigurations.getRepos()));
    }

    @Test
    public void testStoringAndLoadingWithOneConfiguration() {
        SnippetRepositoryConfigurations configurations = SnipmatchFactory.eINSTANCE
                .createSnippetRepositoryConfigurations();
        EclipseGitSnippetRepositoryConfiguration expected = createMockedConfiguration("TestConfig1",
                "TestConfig1Description", true, "http://www.example.com/repo1");
        configurations.getRepos().addAll(Lists.newArrayList(expected));

        RepositoryConfigurations.storeConfigurations(configurations, file);

        EclipseGitSnippetRepositoryConfiguration actual = cast(Iterables.getOnlyElement(RepositoryConfigurations
                .loadConfigurations(file).getRepos()));

        assertThat(equal(actual, expected), is(true));
        assertThat(equal(actual, expected), is(true));
    }

    @Test
    public void testStoringAndLoadingForMoreConfigurations() {
        SnippetRepositoryConfigurations configurations = SnipmatchFactory.eINSTANCE
                .createSnippetRepositoryConfigurations();
        EclipseGitSnippetRepositoryConfiguration expected1 = createMockedConfiguration("TestConfig1",
                "TestConfig1Description", true, "http://www.example.com/repo1");
        EclipseGitSnippetRepositoryConfiguration expected2 = createMockedConfiguration("TestConfig2",
                "TestConfig2Description", false, "http://www.example.com/repo2");

        configurations.getRepos().addAll(Lists.newArrayList(expected1, expected2));

        RepositoryConfigurations.storeConfigurations(configurations, file);

        Collection<EclipseGitSnippetRepositoryConfiguration> loadedConfigurations = cast(RepositoryConfigurations
                .loadConfigurations(file).getRepos());

        assertThat(contains(expected1, loadedConfigurations), is(true));
        assertThat(contains(expected2, loadedConfigurations), is(true));
    }

    private static EclipseGitSnippetRepositoryConfiguration createMockedConfiguration(String name, String description,
            boolean enabled, String url) {
        EclipseGitSnippetRepositoryConfiguration configuration = SnipmatchFactory.eINSTANCE
                .createEclipseGitSnippetRepositoryConfiguration();
        configuration.setName(name);
        configuration.setDescription(description);
        configuration.setEnabled(enabled);
        configuration.setUrl(url);
        return configuration;
    }

    private static boolean equal(EclipseGitSnippetRepositoryConfiguration o1,
            EclipseGitSnippetRepositoryConfiguration o2) {
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(o1.getName(), o2.getName());
        builder.append(o1.getDescription(), o2.getDescription());
        builder.append(o1.getUrl(), o2.getUrl());
        builder.append(o1.isEnabled(), o2.isEnabled());

        return builder.build();
    }

    private static boolean contains(EclipseGitSnippetRepositoryConfiguration configuration,
            Collection<EclipseGitSnippetRepositoryConfiguration> collection) {
        for (EclipseGitSnippetRepositoryConfiguration eclipseGitSnippetRepositoryConfiguration : collection) {
            if (equal(configuration, eclipseGitSnippetRepositoryConfiguration)) {
                return true;
            }
        }
        return false;
    }

}
