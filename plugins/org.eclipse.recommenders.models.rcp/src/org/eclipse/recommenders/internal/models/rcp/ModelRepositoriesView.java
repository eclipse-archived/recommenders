/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - change to tree viewer.
 */
package org.eclipse.recommenders.internal.models.rcp;

import static java.text.MessageFormat.format;
import static org.apache.commons.io.IOCase.INSENSITIVE;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.eclipse.recommenders.internal.models.rcp.Constants.*;
import static org.eclipse.recommenders.internal.models.rcp.ModelsRcpModule.MODEL_CLASSIFIER;
import static org.eclipse.recommenders.rcp.SharedImages.Images.*;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.internal.models.rcp.l10n.LogMessages;
import org.eclipse.recommenders.internal.models.rcp.l10n.Messages;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ModelCoordinates;
import org.eclipse.recommenders.models.rcp.ModelEvents.AdvisorConfigurationChangedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelArchiveDownloadedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelIndexOpenedEvent;
import org.eclipse.recommenders.models.rcp.actions.TriggerModelDownloadForModelCoordinatesAction;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.SharedImages.ImageResource;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Uris;
import org.eclipse.recommenders.utils.rcp.Selections;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ModelRepositoriesView extends ViewPart {

    private DataBindingContext bindingContext;

    private Tree tree;

    private Action addRemoteRepositoryAction;

    private final IModelIndex index;

    private final SharedImages images;

    private final EclipseModelRepository repo;

    private final ModelsRcpPreferences preferences;

    private final List<String> modelClassifiers;

    private final EventBus bus;

    private TreeViewer treeViewer;

    private Text txtSearch;

    private boolean initializeFilter = true;

    private ListMultimap<String, KnownCoordinate> coordinatesGroupedByRepo = LinkedListMultimap.create();
    private ListMultimap<String, KnownCoordinate> filteredCoordinatesGroupedByRepo = LinkedListMultimap.create();

    private final Function<ModelRepositoriesView.KnownCoordinate, String> toStringRepresentation = new Function<ModelRepositoriesView.KnownCoordinate, String>() {

        @Override
        public String apply(KnownCoordinate input) {
            return input.pc.toString();
        }
    };

    @Inject
    public ModelRepositoriesView(IModelIndex index, SharedImages images, EclipseModelRepository repo,
            ModelsRcpPreferences preferences, @Named(MODEL_CLASSIFIER) ImmutableSet<String> modelClassifiers,
            EventBus bus) {
        this.index = index;
        this.images = images;
        this.repo = repo;
        this.preferences = preferences;
        this.modelClassifiers = new ArrayList<>(modelClassifiers);
        Collections.sort(this.modelClassifiers);
        this.bus = bus;
    }

    @Override
    public void createPartControl(Composite parent) {
        bus.register(this);
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        txtSearch = new Text(container, SWT.BORDER | SWT.ICON_SEARCH | SWT.SEARCH | SWT.CANCEL);
        txtSearch.setMessage(Messages.SEARCH_PLACEHOLDER_FILTER_TEXT);
        txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.ARROW_DOWN && tree.getItemCount() != 0) {
                    tree.setFocus();
                    tree.setSelection(tree.getTopItem());
                }
            }
        });

        Composite composite = new Composite(container, SWT.NONE);

        TreeColumnLayout treeLayout = new TreeColumnLayout();
        composite.setLayout(treeLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        treeViewer = new TreeViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
        ColumnViewerToolTipSupport.enableFor(treeViewer);
        tree = treeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        TreeViewerColumn repositoryViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        TreeColumn repositoryColumn = repositoryViewerColumn.getColumn();
        treeLayout.setColumnData(repositoryColumn, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
        repositoryColumn.setText(Messages.COLUMN_LABEL_REPOSITORY);
        repositoryViewerColumn.setLabelProvider(new StyledCellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                Object element = cell.getElement();
                StyledString text = new StyledString();
                if (element instanceof String) {
                    String url = (String) element;
                    text.append(Uris.toStringWithMaskedPassword(Uris.toUri(url), '*'));
                    text.append(" "); //$NON-NLS-1$
                    text.append(format(Messages.TABLE_CELL_SUFFIX_KNOWN_COORDINATES, fetchNumberOfModels(url)),
                            StyledString.COUNTER_STYLER);
                    cell.setImage(getCellImage(url));
                }
                if (element instanceof KnownCoordinate) {
                    KnownCoordinate v = (KnownCoordinate) element;
                    text.append(v.pc.toString());
                }
                cell.setText(text.toString());
                cell.setStyleRanges(text.getStyleRanges());
                super.update(cell);
            }

            private Image getCellImage(String url) {
                URI uri = Uris.parseURI(url).orNull();
                if (uri == null) {
                    return images.getImage(OBJ_REPOSITORY);
                } else if (Uris.isPasswordProtected(uri)) {
                    return images.getImage(OBJ_LOCKED_REPOSITORY);
                } else if (preferences.hasPassword(url)) {
                    return images.getImage(SharedImages.Images.OBJ_LOCKED_REPOSITORY);
                } else {
                    return images.getImage(SharedImages.Images.OBJ_REPOSITORY);
                }
            }
        });

        int minWidth = calculateMinColumnWidthForClassifier();
        for (String classifier : modelClassifiers) {
            newColumn(treeLayout, classifier, minWidth);
        }
        treeViewer.setUseHashlookup(true);
        treeViewer.setContentProvider(new ILazyTreeContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                filteredCoordinatesGroupedByRepo = ArrayListMultimap.create(coordinatesGroupedByRepo);
            }

            @Override
            public void dispose() {
            }

            @Override
            public void updateElement(Object parent, int index) {
                if (parent instanceof IViewSite) {
                    String element = preferences.remotes[index];
                    treeViewer.replace(parent, index, element);
                    treeViewer.setChildCount(element, getChildren(element).length);
                } else if (parent instanceof String) {
                    treeViewer.replace(parent, index, getChildren(parent)[index]);
                }
            }

            private Object[] getChildren(Object element) {
                if (element instanceof String) {
                    return filteredCoordinatesGroupedByRepo.get((String) element).toArray();
                }
                return new Object[0];
            }

            @Override
            public void updateChildCount(Object element, int currentChildCount) {
                int count = 0;

                if (element instanceof IViewSite) {
                    count = preferences.remotes.length;
                }

                if (contains(preferences.remotes, element)) {
                    count = getChildren(element).length;
                }

                if (count != currentChildCount) {
                    treeViewer.setChildCount(element, count);
                }
            }

            @Override
            public Object getParent(Object element) {
                if (element instanceof KnownCoordinate) {
                    KnownCoordinate v = (KnownCoordinate) element;
                    return v.url;
                }
                return null;
            }
        });

        treeViewer.setInput(getViewSite());
        refreshData();

        addRemoteRepositoryAction = new Action() {
            @Override
            public void run() {
                addRemoteRepository();
            }
        };

        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
        addAction(Messages.TOOLBAR_TOOLTIP_ADD_REPOSITORY, ELCL_ADD_REPOSITORY, toolBarManager,
                addRemoteRepositoryAction);

        addAction(Messages.TOOLBAR_TOOLTIP_REFRESH, ELCL_REFRESH, toolBarManager, new Action() {
            @Override
            public void run() {
                refreshData();
            }
        });

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

        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        addAction(Messages.MENUITEM_DELETE_MODELS, ELCL_DELETE, menuManager, new Action() {
            @Override
            public void run() {
                deleteCacheAndRefresh();
            }
        });

        addContextMenu();
        bindingContext = initDataBindings();
    }

    private int calculateMinColumnWidthForClassifier() {
        GC gc = new GC(tree);
        int maxLength = 0;
        for (String classifier : modelClassifiers) {
            int extent = gc.textExtent(classifier.toUpperCase()).x;
            if (extent > maxLength) {
                maxLength = extent;
            }
        }
        gc.dispose();
        return maxLength;
    }

    private void refreshData() {
        new UIJob(Messages.JOB_NAME_REFRESHING_MODEL_REPOSITORIES_VIEW) {
            {
                schedule();
            }

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                Multimap<String, ModelCoordinate> fetchedCoordinates = fetchCoordinatesGroupedByRepo();

                coordinatesGroupedByRepo.clear();
                for (Entry<String, Collection<ModelCoordinate>> entry : fetchedCoordinates.asMap().entrySet()) {
                    coordinatesGroupedByRepo.putAll(entry.getKey(),
                            createCoordiantes(entry.getKey(), entry.getValue()));
                }

                treeViewer.setInput(getViewSite());
                return Status.OK_STATUS;
            }
        };
    }

    private Multimap<String, ModelCoordinate> fetchCoordinatesGroupedByRepo() {
        Multimap<String, ModelCoordinate> coordinatesGroupedByRepo = LinkedListMultimap.create();

        for (String classifier : modelClassifiers) {
            addCoordinateToIndex(coordinatesGroupedByRepo, classifier);
        }

        return coordinatesGroupedByRepo;
    }

    private void addCoordinateToIndex(Multimap<String, ModelCoordinate> coordinatesGroupedByRepo, String classifier) {
        for (ModelCoordinate mc : index.getKnownModels(classifier)) {
            Optional<String> hint = mc.getHint(ModelCoordinate.HINT_REPOSITORY_URL);
            if (hint.isPresent()) {
                coordinatesGroupedByRepo.put(hint.get(), mc);
            }
        }
    }

    private List<KnownCoordinate> createCoordiantes(String url, Collection<ModelCoordinate> modelCoordinates) {
        Multimap<ProjectCoordinate, ModelCoordinate> coordinatesGroupedByProjectCoordinate = groupByProjectCoordinate(
                modelCoordinates);

        List<KnownCoordinate> coordinates = new ArrayList<>();
        for (ProjectCoordinate pc : coordinatesGroupedByProjectCoordinate.keySet()) {
            coordinates.add(new KnownCoordinate(url, pc, coordinatesGroupedByProjectCoordinate.get(pc)));
        }
        return Ordering.natural().onResultOf(toStringRepresentation).sortedCopy(coordinates);
    }

    private Multimap<ProjectCoordinate, ModelCoordinate> groupByProjectCoordinate(
            Collection<ModelCoordinate> modelCoordinates) {
        Multimap<ProjectCoordinate, ModelCoordinate> coordinatesGroupedByProjectCoordinate = LinkedListMultimap
                .create();

        for (ModelCoordinate modelCoordinate : modelCoordinates) {
            coordinatesGroupedByProjectCoordinate.put(ModelCoordinates.toProjectCoordinate(modelCoordinate),
                    modelCoordinate);
        }
        return coordinatesGroupedByProjectCoordinate;
    }

    protected int fetchNumberOfModels(String url) {
        return coordinatesGroupedByRepo.get(url).size();
    }

    public class KnownCoordinate {
        public String url;
        public ProjectCoordinate pc;
        public Collection<ModelCoordinate> mcs;

        public KnownCoordinate(String url, ProjectCoordinate pc, Collection<ModelCoordinate> mcs) {
            this.url = url;
            this.pc = pc;
            this.mcs = mcs;
        }

        private Optional<ModelCoordinate> searchModelCoordinate(String classifier) {
            for (ModelCoordinate mc : mcs) {
                if (mc.getClassifier().equals(classifier)) {
                    return Optional.of(mc);
                }
            }
            return Optional.absent();
        }

        public boolean isDownloaded(String classifier) {
            Optional<ModelCoordinate> omc = searchModelCoordinate(classifier);
            if (omc.isPresent()) {
                return repo.getLocation(omc.get(), false).isPresent();
            }
            return false;
        }

        public boolean hasModelCoordinate(String classifier) {
            return searchModelCoordinate(classifier).isPresent();
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this, "mcs"); //$NON-NLS-1$
        }

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj, "mcs"); //$NON-NLS-1$
        }

    }

    private static final class GlobMatcher implements Predicate<KnownCoordinate> {

        private final String glob;

        private GlobMatcher(String glob) {
            this.glob = "*" + Objects.requireNonNull(glob) + "*"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public boolean apply(KnownCoordinate kc) {
            return FilenameUtils.wildcardMatch(kc.pc.toString(), glob, INSENSITIVE);
        }
    }

    private void addAction(String text, ImageResource imageResource, IContributionManager contributionManager,
            IAction action) {
        action.setImageDescriptor(images.getDescriptor(imageResource));
        action.setText(text);
        action.setToolTipText(text);
        contributionManager.add(action);
    }

    private void addContextMenu() {
        final MenuManager menuManager = new MenuManager();
        Menu contextMenu = menuManager.createContextMenu(tree);
        menuManager.setRemoveAllWhenShown(true);
        tree.setMenu(contextMenu);

        menuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                if (isValidType(treeViewer.getSelection(), KnownCoordinate.class)) {
                    Set<KnownCoordinate> selectedValues = Selections.toSet(treeViewer.getSelection());
                    Set<ModelCoordinate> selectedModelCoordinates = new HashSet<>();
                    for (KnownCoordinate value : selectedValues) {
                        Collection<ModelCoordinate> mcs = value.mcs;
                        selectedModelCoordinates.addAll(mcs);
                    }
                    if (!selectedValues.isEmpty()) {
                        TriggerModelDownloadForModelCoordinatesAction action = new TriggerModelDownloadForModelCoordinatesAction(
                                Messages.MENUITEM_DOWNLOAD_MODELS, selectedModelCoordinates, repo, bus);
                        menuManager.add(action);
                    }
                }

                if (isValidType(treeViewer.getSelection(), String.class)) {
                    addAction(Messages.MENUITEM_ADD_REPOSITORY, ELCL_ADD_REPOSITORY, menuManager,
                            addRemoteRepositoryAction);

                    final Optional<String> url = Selections.getFirstSelected(treeViewer.getSelection());
                    if (url.isPresent()) {
                        addAction(Messages.MENUITEM_EDIT_REPOSITORY, ELCL_EDIT_REPOSITORY, menuManager, new Action() {
                            @Override
                            public void run() {
                                editRepository(url.get());
                                refreshData();
                            }
                        });
                        addAction(Messages.MENUITEM_REMOVE_REPOSITORY, ELCL_REMOVE_REPOSITORY, menuManager,
                                new Action() {
                                    @Override
                                    public void run() {
                                        deleteRepository(url.get());
                                        refreshData();
                                    }
                                });
                    }
                }
            }

            private boolean isValidType(ISelection selection, Class<?> expectedType) {
                return Selections.safeFirstElement(treeViewer.getSelection(), expectedType).isPresent();
            }
        });
    }

    private void newColumn(TreeColumnLayout treeLayout, final String classifier, int minWidth) {
        TreeViewerColumn classifierViewerColumn = new TreeViewerColumn(treeViewer, SWT.CENTER);
        TreeColumn classifierColumn = classifierViewerColumn.getColumn();
        classifierColumn.setMoveable(true);
        classifierColumn.setResizable(false);
        treeLayout.setColumnData(classifierColumn, new ColumnPixelData(minWidth, false, true));
        classifierColumn.setText(classifier.toUpperCase());
        classifierColumn.pack();
        classifierViewerColumn.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                return null;
            }

            @Override
            public String getToolTipText(Object element) {
                if (element instanceof KnownCoordinate) {
                    KnownCoordinate v = (KnownCoordinate) element;

                    if (!v.hasModelCoordinate(classifier)) {
                        return Messages.TABLE_CELL_TOOLTIP_UNAVAILABLE;
                    } else if (v.isDownloaded(classifier)) {
                        return Messages.TABLE_CELL_TOOLTIP_AVAILABLE_LOCALLY;
                    } else {
                        return Messages.TABLE_CELL_TOOLTIP_AVAILABLE_REMOTELY;
                    }
                }
                return null;
            }

            @Override
            public Image getImage(Object element) {
                if (element instanceof KnownCoordinate) {
                    KnownCoordinate v = (KnownCoordinate) element;
                    if (!v.hasModelCoordinate(classifier)) {
                        return images.getImage(OBJ_CROSS_RED);
                    } else if (v.isDownloaded(classifier)) {
                        return images.getImage(OBJ_CHECK_GREEN);
                    } else {
                        return images.getImage(OBJ_BULLET_BLUE);
                    }
                } else {
                    // Return an "icon" for every row to work around Bug 525676; otherwise, no icons are shown in the
                    // classifier columns.
                    // See <https://bugs.eclipse.org/bugs/show_bug.cgi?id=525676> and
                    // <https://bugs.eclipse.org/bugs/show_bug.cgi?id=525678>
                    return images.getImage(OBJ_TRANSPARENT);
                }
            }
        });
    }

    @Override
    public void setFocus() {
        tree.setFocus();
    }

    // needs to be public to work with PojoProperties
    public void setFilter(final String filter) {
        if (initializeFilter && filter.isEmpty()) {
            initializeFilter = false;
            return;
        }
        new UIJob(Messages.JOB_NAME_REFRESHING_MODEL_REPOSITORIES_VIEW) {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!filter.isEmpty()) {
                    filteredCoordinatesGroupedByRepo.clear();
                    for (String key : coordinatesGroupedByRepo.keySet()) {
                        List<KnownCoordinate> unfiltered = coordinatesGroupedByRepo.get(key);

                        Iterable<KnownCoordinate> filtered = Iterables.filter(unfiltered, new GlobMatcher(filter));
                        filteredCoordinatesGroupedByRepo.putAll(key, filtered);
                    }
                } else {
                    filteredCoordinatesGroupedByRepo = ArrayListMultimap.create(coordinatesGroupedByRepo);
                }
                refreshUI();
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    private void refreshUI() {
        int numberOfVisibleElements = treeViewer.getTree().getSize().y / treeViewer.getTree().getItemHeight() + 1;
        treeViewer.refresh();
        int replacedElementsCount = 0;
        for (int i = 0; i < preferences.remotes.length; i++) {
            String url = preferences.remotes[i];
            List<KnownCoordinate> elements = filteredCoordinatesGroupedByRepo.get(url);
            treeViewer.setChildCount(url, elements.size());
            for (int j = 0; j < elements.size() && replacedElementsCount < numberOfVisibleElements; j++) {
                treeViewer.replace(url, j, elements.get(j));
                replacedElementsCount++;
            }
            treeViewer.getTree().getItem(i).setExpanded(true);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        bindingContext.dispose();
        bus.unregister(this);
    }

    @Subscribe
    public void onModelIndexOpened(ModelIndexOpenedEvent e) {
        refreshData();
    }

    @Subscribe
    public void onAdvisorConfigurationChanged(AdvisorConfigurationChangedEvent e) throws IOException {
        refreshData();
    }

    @Subscribe
    public void onModelArchiveDownloaded(final ModelArchiveDownloadedEvent e) {
        new UIJob(Messages.JOB_NAME_REFRESHING_MODEL_REPOSITORIES_VIEW) {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                KnownCoordinate key = createKey(e.model);
                if (key != null) {
                    KnownCoordinate element = Iterables
                            .tryFind(coordinatesGroupedByRepo.get(key.url), Predicates.equalTo(key)).orNull();
                    if (element != null) {
                        treeViewer.update(element, null);
                    }
                }
                return Status.OK_STATUS;
            }

            private KnownCoordinate createKey(ModelCoordinate mc) {
                Optional<String> remoteUrl = mc.getHint(ModelCoordinate.HINT_REPOSITORY_URL);
                if (remoteUrl.isPresent()) {
                    return new KnownCoordinate(remoteUrl.get(), ModelCoordinates.toProjectCoordinate(mc),
                            Collections.<ModelCoordinate>emptyList());
                }
                return null;
            }
        }.schedule();
    }

    private void addRemoteRepository() {
        RepositoryDetailsDialog newRepositoryDialog = new RepositoryDetailsDialog(null, null,
                Lists.newArrayList(preferences.remotes), preferences);
        if (newRepositoryDialog.open() == Window.OK) {
            addRepository(newRepositoryDialog.getRepositoryUrl());
        }
    }

    private void deleteRepository(String remoteUrl) {
        ArrayList<String> newRemotes = Lists.newArrayList(preferences.remotes);
        newRemotes.remove(remoteUrl);
        storeRepositories(newRemotes);
    }

    private void addRepository(String remoteUrl) {
        ArrayList<String> newRemotes = Lists.newArrayList(preferences.remotes);
        newRemotes.add(remoteUrl);
        storeRepositories(newRemotes);
    }

    private void editRepository(String remoteUrl) {
        ArrayList<String> newRemotes = Lists.newArrayList(preferences.remotes);

        RepositoryDetailsDialog editRepositoryDialog = new RepositoryDetailsDialog(null, remoteUrl, newRemotes,
                preferences);
        if (editRepositoryDialog.open() == Window.OK) {
            String updatedRepositoryUrl = editRepositoryDialog.getRepositoryUrl();

            int indexOfOriginalRepository = newRemotes.indexOf(remoteUrl);
            newRemotes.remove(indexOfOriginalRepository);
            newRemotes.add(indexOfOriginalRepository, updatedRepositoryUrl);
            storeRepositories(newRemotes);
        }
    }

    private void storeRepositories(List<String> newRemotes) {
        try {
            IEclipsePreferences s = InstanceScope.INSTANCE.getNode(BUNDLE_ID);
            s.put(PREF_REPOSITORY_URL_LIST, ModelsRcpPreferences.joinRemoteRepositoriesToString(newRemotes));
            s.flush();
        } catch (BackingStoreException e) {
            Logs.log(LogMessages.ERROR_FAILED_TO_STORE_REMOTE_REPOSITORY_PREFERENCES, e);
        }
    }

    private void deleteCacheAndRefresh() {
        new Job(Messages.JOB_NAME_DELETING_MODEL_CACHE) {
            {
                schedule();
            }

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    repo.deleteModels();
                    refreshData();
                    return Status.OK_STATUS;
                } catch (IOException e) {
                    return new Status(Status.ERROR, org.eclipse.recommenders.internal.models.rcp.Constants.BUNDLE_ID,
                            Messages.LOG_ERROR_FAILED_TO_DELETE_MODEL_CACHE);
                }
            }
        };
    }

    protected DataBindingContext initDataBindings() {
        DataBindingContext bindingContext = new DataBindingContext();
        IObservableValue search = WidgetProperties.text(SWT.Modify).observeDelayed(400, txtSearch);
        IObservableValue filter = PojoProperties.value("filter").observe(this); //$NON-NLS-1$
        bindingContext.bindValue(filter, search, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        return bindingContext;
    }
}
