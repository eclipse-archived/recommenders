/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - change list viewer to tree viewer
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static java.text.MessageFormat.format;
import static org.eclipse.recommenders.internal.snipmatch.rcp.SnipmatchRcpModule.REPOSITORY_CONFIGURATION_FILE;
import static org.eclipse.recommenders.rcp.SharedImages.Images.*;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.databinding.viewers.IViewerObservableList;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.recommenders.internal.snipmatch.rcp.EclipseGitSnippetRepository.SnippetRepositoryClosedEvent;
import org.eclipse.recommenders.internal.snipmatch.rcp.EclipseGitSnippetRepository.SnippetRepositoryContentChangedEvent;
import org.eclipse.recommenders.internal.snipmatch.rcp.EclipseGitSnippetRepository.SnippetRepositoryOpenedEvent;
import org.eclipse.recommenders.internal.snipmatch.rcp.editors.SnippetEditorInput;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.SharedImages.ImageResource;
import org.eclipse.recommenders.rcp.SharedImages.Images;
import org.eclipse.recommenders.rcp.model.SnippetRepositoryConfigurations;
import org.eclipse.recommenders.rcp.utils.Selections;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.name.Named;

public class SnippetsView extends ViewPart implements IRcpService {

    private static Logger LOG = LoggerFactory.getLogger(SnippetsView.class);

    private Text txtSearch;
    private TreeViewer treeViewer;
    private Tree tree;

    private IViewerObservableList selection;

    private SharedImages images;

    private final Repositories repos;
    private final SnippetRepositoryConfigurations configs;
    private final File repositoryConfigurationFile;
    private final EventBus bus;

    private Action addRepositoryAction;
    private Action removeRepositoryAction;

    private Action refreshAction;

    private Action addSnippetAction;
    private Action removeSnippetAction;
    private Action editSnippetAction;

    private boolean initializeTableData = true;

    private List<SnippetRepositoryConfiguration> availableRepositories = Lists.newArrayList();
    private ListMultimap<SnippetRepositoryConfiguration, KnownSnippet> snippetsGroupedByRepoName = LinkedListMultimap
            .create();
    private ListMultimap<SnippetRepositoryConfiguration, KnownSnippet> filteredSnippetsGroupedByRepoName = LinkedListMultimap
            .create();

    private final Function<KnownSnippet, String> toStringRepresentation = new Function<KnownSnippet, String>() {

        @Override
        public String apply(KnownSnippet input) {
            return SnippetProposal.createDisplayString(input.snippet);
        }
    };

    @Inject
    public SnippetsView(Repositories repos, SharedImages images, SnippetRepositoryConfigurations configs, EventBus bus,
            @Named(REPOSITORY_CONFIGURATION_FILE) File repositoryConfigurationFile) {
        this.repos = repos;
        this.configs = configs;
        this.images = images;
        this.bus = bus;
        this.repositoryConfigurationFile = repositoryConfigurationFile;
    }

    @Override
    public void createPartControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().applyTo(composite);

        txtSearch = new Text(composite, SWT.BORDER | SWT.ICON_SEARCH | SWT.SEARCH | SWT.CANCEL);
        txtSearch.setMessage(Messages.SEARCH_PLACEHOLDER_SEARCH_TEXT);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(1, 1).applyTo(txtSearch);
        txtSearch.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                Job refreshJob = new UIJob(Messages.JOB_REFRESHING_SNIPPETS_VIEW) {
                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        updateData();
                        refreshTable();
                        return Status.OK_STATUS;
                    }
                };
                refreshJob.schedule();
            }
        });

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.ARROW_DOWN && tree.getItemCount() != 0) {
                    tree.setFocus();
                    tree.setSelection(tree.getTopItem());
                }
            }
        });

        Composite treeComposite = new Composite(composite, SWT.NONE);

        TreeColumnLayout treeLayout = new TreeColumnLayout();
        treeComposite.setLayout(treeLayout);
        treeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        treeViewer = new TreeViewer(treeComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
        ColumnViewerToolTipSupport.enableFor(treeViewer);
        tree = treeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        TreeViewerColumn snippetViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        TreeColumn snippetColumn = snippetViewerColumn.getColumn();
        treeLayout.setColumnData(snippetColumn, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
        snippetColumn.setText(Messages.TABLE_COLUMN_TITLE_SNIPPETS);
        snippetViewerColumn.setLabelProvider(new StyledCellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                Object element = cell.getElement();
                StyledString text = new StyledString();
                if (element instanceof SnippetRepositoryConfiguration) {
                    SnippetRepositoryConfiguration config = (SnippetRepositoryConfiguration) element;
                    text.append(config.getName());
                    text.append(" "); //$NON-NLS-1$
                    text.append(format(Messages.TABLE_CELL_SUFFIX_SNIPPETS, fetchNumberOfSnippets(config)),
                            StyledString.COUNTER_STYLER);
                    cell.setImage(images.getImage(Images.OBJ_REPOSITORY));
                }
                if (element instanceof KnownSnippet) {
                    KnownSnippet knownSnippet = (KnownSnippet) element;
                    text.append(toStringRepresentation.apply(knownSnippet));
                }
                cell.setText(text.toString());
                cell.setStyleRanges(text.getStyleRanges());
                super.update(cell);
            }
        });

        treeViewer.addOpenListener(new IOpenListener() {

            @Override
            public void open(OpenEvent event) {
                doOpen();
            }
        });
        treeViewer.setUseHashlookup(true);
        treeViewer.setContentProvider(new ILazyTreeContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public void updateElement(Object parent, int index) {
                if (parent instanceof IViewSite) {
                    SnippetRepositoryConfiguration config = availableRepositories.get(index);
                    treeViewer.replace(parent, index, config);
                    treeViewer.setChildCount(config, getChildren(config).length);
                } else if (parent instanceof SnippetRepositoryConfiguration) {
                    treeViewer.replace(parent, index, getChildren(parent)[index]);
                }
            }

            private Object[] getChildren(Object element) {
                if (element instanceof SnippetRepositoryConfiguration) {
                    return filteredSnippetsGroupedByRepoName.get((SnippetRepositoryConfiguration) element).toArray();
                }
                return new Object[0];
            }

            @Override
            public void updateChildCount(Object element, int currentChildCount) {
                int count = 0;

                if (element instanceof IViewSite) {
                    count = availableRepositories.size();
                }

                if (availableRepositories.contains(element)) {
                    count = getChildren(element).length;
                }

                if (count != currentChildCount) {
                    treeViewer.setChildCount(element, count);
                }
            }

            @Override
            public Object getParent(Object element) {
                if (element instanceof KnownSnippet) {
                    KnownSnippet knownSnippet = (KnownSnippet) element;
                    return knownSnippet.config;
                }
                return null;
            }
        });

        createActions(parent);
        addToolBar(parent);
        addContextMenu();

        refreshUI();
        selection = ViewersObservables.observeMultiSelection(treeViewer);
    }

    private int fetchNumberOfSnippets(SnippetRepositoryConfiguration config) {
        return snippetsGroupedByRepoName.get(config).size();
    }

    private void createActions(final Composite parent) {
        addRepositoryAction = new Action() {

            @Override
            public void run() {
                List<WizardDescriptor> availableWizards = WizardDescriptors.loadAvailableWizards();
                if (!availableWizards.isEmpty()) {
                    SnippetRepositoryTypeSelectionWizard newWizard = new SnippetRepositoryTypeSelectionWizard();
                    WizardDialog dialog = new WizardDialog(parent.getShell(), newWizard);
                    if (dialog.open() == Window.OK) {
                        SnippetRepositoryConfiguration newConfiguration = newWizard.getConfiguration();
                        newConfiguration.setId(RepositoryConfigurations.fetchHighestUsedId(configs.getRepos()) + 1);
                        configs.getRepos().add(newConfiguration);
                        RepositoryConfigurations.storeConfigurations(configs, repositoryConfigurationFile);
                        bus.post(new Repositories.SnippetRepositoryConfigurationChangedEvent());
                    }
                }
            }
        };

        removeRepositoryAction = new Action() {
            @Override
            public void run() {
                final Optional<SnippetRepositoryConfiguration> config = Selections.getFirstSelected(treeViewer
                        .getSelection());
                if (config.isPresent()) {

                    MessageDialogWithToggle confirmDialog = MessageDialogWithToggle.openOkCancelConfirm(
                            parent.getShell(), Messages.CONFIRM_DIALOG_DELETE_REPOSITORY_TITLE,
                            Messages.CONFIRM_DIALOG_DELETE_REPOSITORY_MESSAGE,
                            Messages.CONFIRM_DIALOG_DELETE_REPOSITORY_TOGGLE_MESSAGE, true, null, null);

                    boolean confirmed = confirmDialog.getReturnCode() == Status.OK;
                    if (!confirmed) {
                        return;
                    }

                    boolean delete = confirmDialog.getToggleState();
                    if (delete) {
                        ISnippetRepository repo = repos.getRepository(config.get().getId()).orNull();
                        if (repo != null) {
                            repo.delete();
                        }
                    }

                    configs.getRepos().remove(config.get());
                    RepositoryConfigurations.storeConfigurations(configs, repositoryConfigurationFile);
                    bus.post(new Repositories.SnippetRepositoryConfigurationChangedEvent());
                    updateData();
                    refreshUI();
                }
            }
        };

        refreshAction = new Action() {
            @Override
            public void run() {
                Job reconnectJob = new Job(Messages.JOB_RECONNECTING_SNIPPET_REPOSITORY) {

                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                refreshAction.setEnabled(false);
                            }
                        });
                        try {
                            repos.close();
                        } catch (IOException e) {
                            // Snipmatch's default repositories cannot throw an IOException here
                            LOG.error(e.getMessage(), e);
                        }
                        try {
                            repos.open();
                        } catch (IOException e) {
                            // Snipmatch's default repositories cannot throw an IOException here
                            LOG.error(e.getMessage(), e);
                        }
                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                refreshAction.setEnabled(true);
                            }
                        });
                        return Status.OK_STATUS;
                    }
                };
                reconnectJob.schedule();
            }
        };

        addSnippetAction = new Action() {
            @Override
            public void run() {
                ISnippetRepository selectedRepository = SelectRepositoryDialog.openSelectRepositoryDialog(
                        parent.getShell(), repos, configs).orNull();
                if (selectedRepository != null) {
                    doAdd(selectedRepository);
                }
            }
        };

        removeSnippetAction = new Action() {
            public void run() {
                Object selectedItem = selection.get(0);
                if (selectedItem instanceof KnownSnippet) {
                    KnownSnippet knownSnippet = cast(selectedItem);

                    boolean confirmed = MessageDialog.openConfirm(parent.getShell(),
                            Messages.CONFIRM_DIALOG_DELETE_SNIPPET_TITLE,
                            Messages.CONFIRM_DIALOG_DELETE_SNIPPET_MESSAGE);

                    if (!confirmed) {
                        return;
                    }

                    try {
                        for (ISnippetRepository repo : repos.getRepositories()) {
                            repo.delete(knownSnippet.snippet.getUuid());
                        }
                    } catch (Exception e) {
                        Throwables.propagate(e);
                    }
                }
            }
        };

        editSnippetAction = new Action() {
            @Override
            public void run() {
                doOpen();
            }
        };

        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                boolean isConfigurationSelected = isValidType(treeViewer.getSelection(),
                        SnippetRepositoryConfiguration.class);
                removeRepositoryAction.setEnabled(isConfigurationSelected);

                boolean isSnippetSelected = isValidType(treeViewer.getSelection(), KnownSnippet.class);
                removeSnippetAction.setEnabled(isSnippetSelected);
                editSnippetAction.setEnabled(isSnippetSelected);
            }
        });
    }

    private boolean isValidType(ISelection selection, Class<?> expectedType) {
        return Selections.safeFirstElement(treeViewer.getSelection(), expectedType).isPresent();
    }

    private void addToolBar(final Composite parent) {
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

        addAction(Messages.SNIPPETS_VIEW_MENUITEM_ADD_SNIPPET, ELCL_ADD_SNIPPET, toolBarManager, addSnippetAction);

        addActions(toolBarManager);

        toolBarManager.add(new Separator());

        addAction(Messages.TOOLBAR_TOOLTIP_EXPAND_ALL, ELCL_EXPAND_ALL, toolBarManager, new Action() {
            @Override
            public void run() {
                for (int i = 0; i < treeViewer.getTree().getItemCount(); i++) {
                    treeViewer.getTree().getItem(i).setExpanded(true);
                }
            }

        });

        addAction(Messages.TOOLBAR_TOOLTIP_COLLAPSE_ALL, ELCL_COLLAPSE_ALL, toolBarManager, new Action() {
            @Override
            public void run() {
                for (int i = 0; i < treeViewer.getTree().getItemCount(); i++) {
                    treeViewer.getTree().getItem(i).setExpanded(false);
                }
            }
        });

        toolBarManager.add(new Separator());

        addAction(Messages.SNIPPETS_VIEW_MENUITEM_REFRESH, ELCL_REFRESH, toolBarManager, refreshAction);
    }

    private void addActions(IContributionManager contributionManager) {
        addAction(Messages.SNIPPETS_VIEW_MENUITEM_REMOVE_SNIPPET, ELCL_REMOVE_SNIPPET, contributionManager,
                removeSnippetAction);

        addAction(Messages.SNIPPETS_VIEW_MENUITEM_EDIT_SNIPPET, ELCL_EDIT_SNIPPET, contributionManager,
                editSnippetAction);

        contributionManager.add(new Separator());

        addAction(Messages.SNIPPETS_VIEW_MENUITEM_ADD_REPOSITORY, ELCL_ADD_REPOSITORY, contributionManager,
                addRepositoryAction);
        addAction(Messages.SNIPPETS_VIEW_MENUITEM_REMOVE_REPOSITORY, ELCL_REMOVE_REPOSITORY,
                ELCL_REMOVE_REPOSITORY_DISABLED, contributionManager, removeRepositoryAction);
    }

    private void addContextMenu() {
        final MenuManager menuManager = new MenuManager();
        Menu contextMenu = menuManager.createContextMenu(tree);
        menuManager.setRemoveAllWhenShown(true);
        tree.setMenu(contextMenu);

        menuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                boolean addedAddSnippetToRepoAction = false;

                boolean isSingleSelectionOfConfiguration = selection.size() == 1 && selection.get(0) instanceof SnippetRepositoryConfiguration;
                if (isSingleSelectionOfConfiguration) {
                    SnippetRepositoryConfiguration config = cast(selection.get(0));
                    final ISnippetRepository repo = repos.getRepository(config.getId()).orNull();
                    if (repo != null) {
                        addAction(format(Messages.SNIPPETS_VIEW_MENUITEM_ADD_SNIPPET_TO_REPOSITORY, config.getName()),
                                ELCL_ADD_SNIPPET, manager, new Action() {
                                    @Override
                                    public void run() {
                                        doAdd(repo);
                                    }
                                });
                        addedAddSnippetToRepoAction = true;
                    }
                }

                if (!addedAddSnippetToRepoAction) {
                    addAction(Messages.SNIPPETS_VIEW_MENUITEM_ADD_SNIPPET, ELCL_ADD_SNIPPET, manager, addSnippetAction);
                }
                addActions(manager);
            }

        });
    }

    private void addAction(String text, ImageResource imageResource, IContributionManager contributionManager,
            IAction action) {
        action.setImageDescriptor(images.getDescriptor(imageResource));
        action.setText(text);
        action.setToolTipText(text);
        contributionManager.add(action);
    }

    private void addAction(String text, Images imageResource, Images imageResourceDisabled,
            IContributionManager contributionManager, Action action) {
        action.setDisabledImageDescriptor(images.getDescriptor(imageResourceDisabled));
        addAction(text, imageResource, contributionManager, action);
    }

    private void updateData() {
        if (txtSearch.isDisposed()) {
            return;
        }
        snippetsGroupedByRepoName = searchSnippets(""); //$NON-NLS-1$
        filteredSnippetsGroupedByRepoName = searchSnippets(txtSearch.getText());
        availableRepositories = configs.getRepos();
    }

    private ListMultimap<SnippetRepositoryConfiguration, KnownSnippet> searchSnippets(String searchTerm) {
        ListMultimap<SnippetRepositoryConfiguration, KnownSnippet> snippetsGroupedByRepositoryName = LinkedListMultimap
                .create();

        for (SnippetRepositoryConfiguration config : configs.getRepos()) {
            ISnippetRepository repo = repos.getRepository(config.getId()).orNull();
            if (repo == null) {
                continue;
            }
            Set<KnownSnippet> knownSnippets = Sets.newHashSet();
            for (Recommendation<ISnippet> recommendation : repo.search(searchTerm)) {
                knownSnippets.add(new KnownSnippet(config, recommendation.getProposal()));
            }
            List<KnownSnippet> sorted = Ordering.from(String.CASE_INSENSITIVE_ORDER).onResultOf(toStringRepresentation)
                    .sortedCopy(knownSnippets);
            snippetsGroupedByRepositoryName.putAll(config, sorted);
        }

        return snippetsGroupedByRepositoryName;
    }

    @Subscribe
    public void onEvent(SnippetRepositoryOpenedEvent e) throws IOException {
        refreshUI();
    }

    @Subscribe
    public void onEvent(SnippetRepositoryClosedEvent e) throws IOException {
        refreshUI();
    }

    private boolean isImportSupported() {
        for (ISnippetRepository repo : repos.getRepositories()) {
            if (repo.isImportSupported()) {
                return true;
            }
        }
        return false;
    }

    @Subscribe
    public void onEvent(SnippetRepositoryContentChangedEvent e) throws IOException {
        refreshUI();
    }

    private void refreshUI() {

        Job refreshJob = new UIJob(Messages.JOB_REFRESHING_SNIPPETS_VIEW) {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!treeViewer.getControl().isDisposed()) {
                    updateData();
                    if (initializeTableData) {
                        treeViewer.setInput(getViewSite());
                        initializeTableData = false;
                    } else {
                        refreshTable();
                    }
                }
                addSnippetAction.setEnabled(isImportSupported());
                editSnippetAction.setEnabled(false);
                removeSnippetAction.setEnabled(false);
                removeRepositoryAction.setEnabled(false);
                return Status.OK_STATUS;
            }
        };
        refreshJob.schedule();
    }

    private void refreshTable() {
        int numberOfVisibleElements = treeViewer.getTree().getSize().y / treeViewer.getTree().getItemHeight() + 1;
        treeViewer.refresh();
        int replacedElementsCount = 0;
        for (int i = 0; i < availableRepositories.size(); i++) {
            SnippetRepositoryConfiguration config = availableRepositories.get(i);
            List<KnownSnippet> elements = filteredSnippetsGroupedByRepoName.get(config);
            treeViewer.setChildCount(config, elements.size());
            for (int j = 0; j < elements.size() && replacedElementsCount < numberOfVisibleElements; j++) {
                treeViewer.replace(config, j, elements.get(j));
                replacedElementsCount++;
            }
            treeViewer.getTree().getItem(i).setExpanded(true);
        }
    }

    private void doAdd(ISnippetRepository repo) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        try {
            ISnippet snippet = new Snippet(UUID.randomUUID(), "", "", Collections.<String>emptyList(), //$NON-NLS-1$ //$NON-NLS-2$
                    Collections.<String>emptyList(), ""); //$NON-NLS-1$

            final SnippetEditorInput input = new SnippetEditorInput(snippet, repo);
            page.openEditor(input, "org.eclipse.recommenders.snipmatch.rcp.editors.snippet"); //$NON-NLS-1$
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    private void doOpen() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        for (int i = 0; i < selection.size(); i++) {
            Object selectedItem = selection.get(i);
            if (selectedItem instanceof KnownSnippet) {
                KnownSnippet knownSnippet = cast(selectedItem);
                try {
                    ISnippet snippet = knownSnippet.snippet;
                    ISnippetRepository repository = findRepoForOriginalSnippet(snippet);

                    final SnippetEditorInput input = new SnippetEditorInput(snippet, repository);
                    page.openEditor(input, "org.eclipse.recommenders.snipmatch.rcp.editors.snippet"); //$NON-NLS-1$
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
            }
        }
    }

    private ISnippetRepository findRepoForOriginalSnippet(ISnippet snippet) {
        for (ISnippetRepository repo : repos.getRepositories()) {
            if (repo.hasSnippet(snippet.getUuid())) {
                return repo;
            }
        }
        return null;
    }

    @Override
    public void setFocus() {
        treeViewer.getControl().setFocus();
    }

    public class KnownSnippet {
        public ISnippet snippet;
        public SnippetRepositoryConfiguration config;

        public KnownSnippet(SnippetRepositoryConfiguration config, ISnippet snippet) {
            this.config = config;
            this.snippet = snippet;
        }
    }

}
