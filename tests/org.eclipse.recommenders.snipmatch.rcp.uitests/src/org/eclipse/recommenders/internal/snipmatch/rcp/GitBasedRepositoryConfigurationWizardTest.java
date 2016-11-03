package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.recommenders.internal.snipmatch.rcp.GitBasedRepositoryConfigurationWizard.*;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.Messages;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.rcp.model.EclipseGitSnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.rcp.model.impl.EclipseGitSnippetRepositoryConfigurationImpl;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Sets;

public class GitBasedRepositoryConfigurationWizardTest {

    @Test
    public void testDefaultPushBranchPrefix() {
        Repositories repositories = mock(Repositories.class);
        when(repositories.getRepositories()).thenReturn(Collections.<ISnippetRepository>emptySet());
        GitBasedRepositoryConfigurationWizard sut = new GitBasedRepositoryConfigurationWizard(repositories);

        SWTBot bot = createBot(sut);

        setName(bot, "Example");
        setFetchUrl(bot, "http://example.org/fetch/");
        setPushUrl(bot, "http://example.org/push/");

        SWTBotText pushBranchPrefixText = getPushBranchPrefixText(bot);
        assertThat(pushBranchPrefixText.isEnabled(), is(false));

        SWTBotButton finishButton = bot.button("Finish");
        assertThat(finishButton.isEnabled(), is(true));

        finishButton.click();
        EclipseGitSnippetRepositoryConfigurationImpl configuration = cast(sut.getConfiguration());

        assertThat(configuration.getName(), is(equalTo("Example")));
        assertThat(configuration.getUrl(), is(equalTo("http://example.org/fetch/")));
        assertThat(configuration.getPushUrl(), is(equalTo("http://example.org/push/")));
        assertThat(configuration.getPushBranchPrefix(), is(equalTo("refs/heads")));
    }

    @Test
    public void testGitPushBranchPrefix() {
        Repositories repositories = mock(Repositories.class);
        when(repositories.getRepositories()).thenReturn(Collections.<ISnippetRepository>emptySet());
        GitBasedRepositoryConfigurationWizard sut = new GitBasedRepositoryConfigurationWizard(repositories);

        SWTBot bot = createBot(sut);

        setName(bot, "Example");
        setFetchUrl(bot, "http://example.org/fetch/");
        setPushUrl(bot, "http://example.org/push/");
        setGitPushBranchPrefixCombo(bot);

        SWTBotText pushBranchPrefixText = getPushBranchPrefixText(bot);
        assertThat(pushBranchPrefixText.isEnabled(), is(false));

        SWTBotButton finishButton = bot.button("Finish");
        assertThat(finishButton.isEnabled(), is(true));

        finishButton.click();
        EclipseGitSnippetRepositoryConfigurationImpl configuration = cast(sut.getConfiguration());

        assertThat(configuration.getName(), is(equalTo("Example")));
        assertThat(configuration.getUrl(), is(equalTo("http://example.org/fetch/")));
        assertThat(configuration.getPushUrl(), is(equalTo("http://example.org/push/")));
        assertThat(configuration.getPushBranchPrefix(), is(equalTo("refs/heads")));
    }

    @Test
    public void testGerritPushBranchPrefix() {
        Repositories repositories = mock(Repositories.class);
        when(repositories.getRepositories()).thenReturn(Collections.<ISnippetRepository>emptySet());
        GitBasedRepositoryConfigurationWizard sut = new GitBasedRepositoryConfigurationWizard(repositories);

        SWTBot bot = createBot(sut);

        setName(bot, "Example");
        setFetchUrl(bot, "http://example.org/fetch/");
        setPushUrl(bot, "http://example.org/push/");
        setGerritPushBranchPrefixCombo(bot);

        SWTBotText pushBranchPrefixText = getPushBranchPrefixText(bot);
        assertThat(pushBranchPrefixText.isEnabled(), is(false));

        SWTBotButton finishButton = bot.button("Finish");
        assertThat(finishButton.isEnabled(), is(true));

        finishButton.click();
        EclipseGitSnippetRepositoryConfigurationImpl configuration = cast(sut.getConfiguration());

        assertThat(configuration.getName(), is(equalTo("Example")));
        assertThat(configuration.getUrl(), is(equalTo("http://example.org/fetch/")));
        assertThat(configuration.getPushUrl(), is(equalTo("http://example.org/push/")));
        assertThat(configuration.getPushBranchPrefix(), is(equalTo("refs/for")));
    }

    @Test
    public void testCustomPushBranchPrefix() {
        Repositories repositories = mock(Repositories.class);
        when(repositories.getRepositories()).thenReturn(Collections.<ISnippetRepository>emptySet());
        GitBasedRepositoryConfigurationWizard sut = new GitBasedRepositoryConfigurationWizard(repositories);

        SWTBot bot = createBot(sut);

        setName(bot, "Example");
        setFetchUrl(bot, "http://example.org/fetch/");
        setPushUrl(bot, "http://example.org/push/");
        setCustomPushBranchPrefixCombo(bot);

        SWTBotText pushBranchPrefixText = getPushBranchPrefixText(bot);
        assertThat(pushBranchPrefixText.isEnabled(), is(true));

        setPushBranchPrefixText(bot, "custom");

        SWTBotButton finishButton = bot.button("Finish");
        assertThat(finishButton.isEnabled(), is(true));

        finishButton.click();
        EclipseGitSnippetRepositoryConfigurationImpl configuration = cast(sut.getConfiguration());

        assertThat(configuration.getName(), is(equalTo("Example")));
        assertThat(configuration.getUrl(), is(equalTo("http://example.org/fetch/")));
        assertThat(configuration.getPushUrl(), is(equalTo("http://example.org/push/")));
        assertThat(configuration.getPushBranchPrefix(), is(equalTo("custom")));
    }

    @Test
    public void testInvalidCustomPushBranchPrefix() {
        Repositories repositories = mock(Repositories.class);
        when(repositories.getRepositories()).thenReturn(Collections.<ISnippetRepository>emptySet());
        GitBasedRepositoryConfigurationWizard sut = new GitBasedRepositoryConfigurationWizard(repositories);

        SWTBot bot = createBot(sut);

        setName(bot, "Example");
        setFetchUrl(bot, "http://example.org/fetch/");
        setPushUrl(bot, "http://example.org/push/");
        setCustomPushBranchPrefixCombo(bot);

        SWTBotText pushBranchPrefixText = getPushBranchPrefixText(bot);
        assertThat(pushBranchPrefixText.isEnabled(), is(true));

        setPushBranchPrefixText(bot, "with some spaces");

        SWTBotButton finishButton = bot.button("Finish");
        assertThat(finishButton.isEnabled(), is(false));

        bot.button("Cancel").click();
    }

    @Test
    public void testEmptyName() {
        Repositories repositories = mock(Repositories.class);
        when(repositories.getRepositories()).thenReturn(Collections.<ISnippetRepository>emptySet());
        GitBasedRepositoryConfigurationWizard sut = new GitBasedRepositoryConfigurationWizard(repositories);

        SWTBot bot = createBot(sut);

        setName(bot, "");
        setFetchUrl(bot, "http://example.org/fetch/");
        setPushUrl(bot, "http://example.org/push/");

        SWTBotText pushBranchPrefixText = getPushBranchPrefixText(bot);
        assertThat(pushBranchPrefixText.isEnabled(), is(false));

        SWTBotButton finishButton = bot.button("Finish");
        assertThat(finishButton.isEnabled(), is(false));

        bot.button("Cancel").click();
    }

    @Test
    public void testFetchUrl() {
        Repositories repositories = mock(Repositories.class);
        when(repositories.getRepositories()).thenReturn(Collections.<ISnippetRepository>emptySet());
        GitBasedRepositoryConfigurationWizard sut = new GitBasedRepositoryConfigurationWizard(repositories);

        SWTBot bot = createBot(sut);

        setName(bot, "Example");
        setFetchUrl(bot, "");
        setPushUrl(bot, "http://example.org/push/");

        SWTBotText pushBranchPrefixText = getPushBranchPrefixText(bot);
        assertThat(pushBranchPrefixText.isEnabled(), is(false));

        SWTBotButton finishButton = bot.button("Finish");
        assertThat(finishButton.isEnabled(), is(false));

        setFetchUrl(bot, "udp://example.org/fetch/");
        assertThat(finishButton.isEnabled(), is(false));

        setFetchUrl(bot, "git+ssh://example.org/fetch/");
        assertThat(finishButton.isEnabled(), is(true));

        finishButton.click();
        EclipseGitSnippetRepositoryConfigurationImpl configuration = cast(sut.getConfiguration());

        assertThat(configuration.getName(), is(equalTo("Example")));
        assertThat(configuration.getUrl(), is(equalTo("git+ssh://example.org/fetch/")));
        assertThat(configuration.getPushUrl(), is(equalTo("http://example.org/push/")));
        assertThat(configuration.getPushBranchPrefix(), is(equalTo("refs/heads")));
    }

    @Test
    public void testPushUrl() {
        Repositories repositories = mock(Repositories.class);
        when(repositories.getRepositories()).thenReturn(Collections.<ISnippetRepository>emptySet());
        GitBasedRepositoryConfigurationWizard sut = new GitBasedRepositoryConfigurationWizard(repositories);

        SWTBot bot = createBot(sut);

        setName(bot, "Example");
        setFetchUrl(bot, "http://example.org/fetch/");
        setPushUrl(bot, "");

        SWTBotText pushBranchPrefixText = getPushBranchPrefixText(bot);
        assertThat(pushBranchPrefixText.isEnabled(), is(false));

        SWTBotButton finishButton = bot.button("Finish");
        assertThat(finishButton.isEnabled(), is(false));

        setPushUrl(bot, "udp://example.org/push/");
        assertThat(finishButton.isEnabled(), is(false));

        setPushUrl(bot, "git+ssh://example.org/push/");
        assertThat(finishButton.isEnabled(), is(true));

        finishButton.click();
        EclipseGitSnippetRepositoryConfigurationImpl configuration = cast(sut.getConfiguration());

        assertThat(configuration.getName(), is(equalTo("Example")));
        assertThat(configuration.getUrl(), is(equalTo("http://example.org/fetch/")));
        assertThat(configuration.getPushUrl(), is(equalTo("git+ssh://example.org/push/")));
        assertThat(configuration.getPushBranchPrefix(), is(equalTo("refs/heads")));
    }

    @Test
    public void testNewRepositoryUsingExistingFetchUriAsNewPushUri() {
        ISnippetRepository snippetRepository = mock(ISnippetRepository.class);
        when(snippetRepository.getRepositoryLocation()).thenReturn("http://example.org/push/");

        Repositories repositories = mock(Repositories.class);
        when(repositories.getRepositories()).thenReturn(Collections.singleton(snippetRepository));

        GitBasedRepositoryConfigurationWizard sut = new GitBasedRepositoryConfigurationWizard(repositories);

        SWTBot bot = createBot(sut);

        setName(bot, "Example");
        setFetchUrl(bot, "http://example.org/fetch/");
        setPushUrl(bot, "http://example.org/push/");

        SWTBotText pushBranchPrefixText = getPushBranchPrefixText(bot);
        assertThat(pushBranchPrefixText.isEnabled(), is(false));

        SWTBotButton finishButton = bot.button("Finish");
        assertThat(finishButton.isEnabled(), is(true));

        finishButton.click();
        EclipseGitSnippetRepositoryConfigurationImpl configuration = cast(sut.getConfiguration());

        assertThat(configuration.getName(), is(equalTo("Example")));
        assertThat(configuration.getUrl(), is(equalTo("http://example.org/fetch/")));
        assertThat(configuration.getPushUrl(), is(equalTo("http://example.org/push/")));
        assertThat(configuration.getPushBranchPrefix(), is(equalTo("refs/heads")));
    }

    @Test
    public void testNewRepositoryUsingExistingFetchUriAsNewFetchUri() {
        ISnippetRepository snippetRepository = mock(ISnippetRepository.class);
        when(snippetRepository.getRepositoryLocation()).thenReturn("http://example.org/fetch/");

        Repositories repositories = mock(Repositories.class);
        when(repositories.getRepositories()).thenReturn(Collections.singleton(snippetRepository));

        GitBasedRepositoryConfigurationWizard sut = new GitBasedRepositoryConfigurationWizard(repositories);
        SWTBot bot = createBot(sut);

        setName(bot, "Example");
        setFetchUrl(bot, "http://example.org/fetch/");
        setPushUrl(bot, "http://example.org/push/");

        SWTBotText pushBranchPrefixText = getPushBranchPrefixText(bot);
        assertThat(pushBranchPrefixText.isEnabled(), is(false));

        SWTBotButton finishButton = bot.button("Finish");
        assertThat(finishButton.isEnabled(), is(false));

        bot.button("Cancel").click();
    }

    @Test
    public void testEditRepository() {
        EclipseGitSnippetRepositoryConfiguration configurationToEdit = Mockito
                .mock(EclipseGitSnippetRepositoryConfiguration.class);
        when(configurationToEdit.getName()).thenReturn("Example");
        when(configurationToEdit.getUrl()).thenReturn("http://example.org/old-fetch/");
        when(configurationToEdit.getPushUrl()).thenReturn("http://example.org/push/");
        when(configurationToEdit.getPushBranchPrefix()).thenReturn("refs/heads");

        ISnippetRepository snippetRepositoryToEdit = mock(ISnippetRepository.class);
        when(snippetRepositoryToEdit.getRepositoryLocation()).thenReturn("http://example.org/old-fetch/");

        ISnippetRepository anotherSnippetRepository = mock(ISnippetRepository.class);
        when(anotherSnippetRepository.getRepositoryLocation()).thenReturn("http://example.org/another-fetch/");

        Repositories repositories = mock(Repositories.class);
        when(repositories.getRepositories())
                .thenReturn(Sets.newHashSet(snippetRepositoryToEdit, anotherSnippetRepository));

        GitBasedRepositoryConfigurationWizard sut = new GitBasedRepositoryConfigurationWizard(repositories);
        sut.setConfiguration(configurationToEdit);

        SWTBot bot = createBot(sut);

        SWTBotButton finishButton = bot.button("Finish");
        assertThat(finishButton.isEnabled(), is(true));

        SWTBotText pushBranchPrefixText = getPushBranchPrefixText(bot);
        assertThat(pushBranchPrefixText.isEnabled(), is(false));

        setFetchUrl(bot, "http://example.org/another-fetch/");
        assertThat(finishButton.isEnabled(), is(false));

        setFetchUrl(bot, "http://example.org/old-fetch/");
        assertThat(finishButton.isEnabled(), is(true));

        setFetchUrl(bot, "http://example.org/new-fetch/");
        assertThat(finishButton.isEnabled(), is(true));

        finishButton.click();
        EclipseGitSnippetRepositoryConfigurationImpl configuration = cast(sut.getConfiguration());

        assertThat(configuration.getName(), is(equalTo("Example")));
        assertThat(configuration.getUrl(), is(equalTo("http://example.org/new-fetch/")));
        assertThat(configuration.getPushUrl(), is(equalTo("http://example.org/push/")));
        assertThat(configuration.getPushBranchPrefix(), is(equalTo("refs/heads")));
    }

    @Test
    public void testChangeUserCredentialsOfRepository() {
        EclipseGitSnippetRepositoryConfiguration configurationToEdit = Mockito
                .mock(EclipseGitSnippetRepositoryConfiguration.class);
        when(configurationToEdit.getName()).thenReturn("Example");
        when(configurationToEdit.getUrl()).thenReturn("http://user:pass@example.org/old-fetch/");
        when(configurationToEdit.getPushUrl()).thenReturn("http://user:pass@example.org/push/");
        when(configurationToEdit.getPushBranchPrefix()).thenReturn("refs/heads");

        ISnippetRepository snippetRepositoryToEdit = mock(ISnippetRepository.class);
        when(snippetRepositoryToEdit.getRepositoryLocation()).thenReturn("http://user:pass@example.org/old-fetch/");

        ISnippetRepository anotherSnippetRepository = mock(ISnippetRepository.class);
        when(anotherSnippetRepository.getRepositoryLocation()).thenReturn("http://example.org/another-fetch/");

        Repositories repositories = mock(Repositories.class);
        when(repositories.getRepositories())
                .thenReturn(Sets.newHashSet(snippetRepositoryToEdit, anotherSnippetRepository));

        GitBasedRepositoryConfigurationWizard sut = new GitBasedRepositoryConfigurationWizard(repositories);
        sut.setConfiguration(configurationToEdit);

        SWTBot bot = createBot(sut);

        SWTBotButton finishButton = bot.button("Finish");
        assertThat(finishButton.isEnabled(), is(true));

        SWTBotText pushBranchPrefixText = getPushBranchPrefixText(bot);
        assertThat(pushBranchPrefixText.isEnabled(), is(false));

        setFetchUrl(bot, "http://user:pass@example.org/another-fetch/");
        assertThat(finishButton.isEnabled(), is(false));

        setFetchUrl(bot, "http://user:pass2@example.org/old-fetch/");
        assertThat(finishButton.isEnabled(), is(true));

        finishButton.click();
        EclipseGitSnippetRepositoryConfigurationImpl configuration = cast(sut.getConfiguration());

        assertThat(configuration.getName(), is(equalTo("Example")));
        assertThat(configuration.getUrl(), is(equalTo("http://user:pass2@example.org/old-fetch/")));
        assertThat(configuration.getPushUrl(), is(equalTo("http://user:pass@example.org/push/")));
        assertThat(configuration.getPushBranchPrefix(), is(equalTo("refs/heads")));
    }

    private void setName(SWTBot bot, String text) {
        bot.textWithLabel(Messages.WIZARD_GIT_REPOSITORY_LABEL_NAME).setText(text);
    }

    private void setFetchUrl(SWTBot bot, String text) {
        bot.textWithLabel(Messages.WIZARD_GIT_REPOSITORY_LABEL_FETCH_URL).setText(text);
    }

    private void setPushUrl(SWTBot bot, String text) {
        bot.textWithLabel(Messages.WIZARD_GIT_REPOSITORY_LABEL_PUSH_URL).setText(text);
    }

    private void setPushBranchPrefixText(SWTBot bot, String text) {
        bot.textWithId(PUSH_BRANCH_PREFIX_TEXT_KEY, PUSH_BRANCH_PREFIX_TEXT_VALUE).setText(text);
    }

    private void setGitPushBranchPrefixCombo(SWTBot bot) {
        bot.comboBoxWithLabel(Messages.WIZARD_GIT_REPOSITORY_LABEL_PUSH_BRANCH_PREFIX)
                .setSelection(Messages.WIZARD_GIT_REPOSITORY_OPTION_GIT_PUSH_BRANCH_PREFIX);
    }

    private void setGerritPushBranchPrefixCombo(SWTBot bot) {
        bot.comboBoxWithLabel(Messages.WIZARD_GIT_REPOSITORY_LABEL_PUSH_BRANCH_PREFIX)
                .setSelection(Messages.WIZARD_GIT_REPOSITORY_OPTION_GERRIT_PUSH_BRANCH_PREFIX);
    }

    private void setCustomPushBranchPrefixCombo(SWTBot bot) {
        bot.comboBoxWithLabel(Messages.WIZARD_GIT_REPOSITORY_LABEL_PUSH_BRANCH_PREFIX)
                .setSelection(Messages.WIZARD_GIT_REPOSITORY_OPTION_OTHER_PUSH_BRANCH_PREFIX);
    }

    private SWTBotText getPushBranchPrefixText(SWTBot bot) {
        return bot.textWithId(PUSH_BRANCH_PREFIX_TEXT_KEY, PUSH_BRANCH_PREFIX_TEXT_VALUE);
    }

    private SWTBot createBot(GitBasedRepositoryConfigurationWizard sut) {
        final WizardDialog dialog = new WizardDialog(null, sut);
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                dialog.setBlockOnOpen(false);
                dialog.open();
            }
        });
        SWTBot bot = new SWTBot();
        return bot.shell(Messages.WIZARD_GIT_REPOSITORY_WINDOW_TITLE).bot();
    }
}
