/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.internal.models.rcp;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.presentInstances;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import static org.eclipse.recommenders.models.DependencyInfo.EXECUTION_ENVIRONMENT;
import static org.eclipse.recommenders.models.DependencyInfo.PROJECT_NAME;
import static org.eclipse.recommenders.rcp.SharedImages.Images.ELCL_REFRESH;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OBJ_JAR;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OBJ_JAVA_PROJECT;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OBJ_JRE;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.models.IProjectCoordinateAdvisorService;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.rcp.ModelEvents.AdvisorConfigurationChangedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ProjectCoordinateChangeEvent;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ProjectCoordinatesView extends ViewPart {

    private static final int COLUMN_LOCATION = 0;
    private static final int COLUMN_COORDINATE = 1;

    private TableViewer tableViewer;
    private ContentProvider contentProvider;

    private final EclipseDependencyListener dependencyListener;
    private final IProjectCoordinateAdvisorService pcAdvisorsService;
    private ManualProjectCoordinateAdvisor manualPcAdvisor;

    private Table table;
    private TableViewerColumn locationColumn;
    private TableViewerColumn coordinateColumn;
    private TableComparator comparator;

    private EventBus bus;
    private SharedImages images;

    @Inject
    public ProjectCoordinatesView(final EclipseDependencyListener dependencyListener,
            final IProjectCoordinateAdvisorService pcAdvisorService,
            final ManualProjectCoordinateAdvisor manualProjectCoordinateAdvisor, EventBus bus, SharedImages images) {
        this.dependencyListener = dependencyListener;
        this.pcAdvisorsService = pcAdvisorService;
        manualPcAdvisor = manualProjectCoordinateAdvisor;
        this.bus = bus;
        bus.register(this);
        this.images = images;
    }

    @Override
    public void createPartControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        TableColumnLayout tableLayout = new TableColumnLayout();
        composite.setLayout(tableLayout);

        tableViewer = new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

        contentProvider = new ContentProvider();
        tableViewer.setContentProvider(contentProvider);
        tableViewer.setInput(getViewSite());

        ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);

        locationColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        TableColumn tableColumn = locationColumn.getColumn();
        tableColumn.setText("Location");
        tableLayout.setColumnData(tableColumn, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));

        coordinateColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        coordinateColumn.setEditingSupport(new ProjectCoordinateEditing(tableViewer));
        tableColumn = coordinateColumn.getColumn();
        tableColumn.setText("Coordinate");
        tableLayout.setColumnData(tableColumn, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));

        table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        addSortingFunctionality();
        addFilterFunctionality();
        addRefreshButton();

        refreshData();
    }

    class ProjectCoordinateEditing extends EditingSupport {

        private String formerValue;
        private ComboBoxViewerCellEditor editor;

        public ProjectCoordinateEditing(TableViewer viewer) {
            super(viewer);
            editor = new ComboBoxViewerCellEditor(viewer.getTable());
            editor.setLabelProvider(new LabelProvider());
            editor.setContentProvider(ArrayContentProvider.getInstance());
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            if (element instanceof Entry) {
                Set<String> values = Sets.newHashSet();
                Collection<Optional<ProjectCoordinate>> value = extractProjectCoordinates(element);
                for (ProjectCoordinate pc : presentInstances(value)) {
                    values.add(pc.toString());
                }
                editor.setInput(values);
            }
            return editor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            if (element instanceof Entry) {
                Collection<Optional<ProjectCoordinate>> pcs = extractProjectCoordinates(element);
                Optional<ProjectCoordinate> optionalFirstMatchingCoordinate = findFirstMatchingCoordinate(pcs);
                if (optionalFirstMatchingCoordinate.isPresent()) {
                    formerValue = optionalFirstMatchingCoordinate.get().toString();
                } else {
                    formerValue = "";
                }
                return formerValue;
            }
            return null;
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (value == null) {
                if (editor.getControl() instanceof CCombo) {
                    value = ((CCombo) editor.getControl()).getText();
                }
            }
            if (equal(value, formerValue)) {
                return;
            }
            if (element instanceof Entry) {
                DependencyInfo dependencyInfo = extractDependencyInfo(element);
                if ("".equals(value)) {
                    manualPcAdvisor.removeManualMapping(dependencyInfo);
                    bus.post(new ProjectCoordinateChangeEvent(dependencyInfo));
                } else {
                    try {
                        ProjectCoordinate valueOf = ProjectCoordinate.valueOf((String) value);
                        manualPcAdvisor.setManualMapping(dependencyInfo, valueOf);
                        bus.post(new ProjectCoordinateChangeEvent(dependencyInfo));
                    } catch (Exception e) {
                        MessageDialog.openError(table.getShell(), "Input value has wrong format!",
                                String.format("The value '%s' did not have the right format.", value));
                        return;
                    }
                }
            }
            /*
             * It is needed to make a total refresh (resolve all dependencies again) because the modification of the
             * data model isn't possible here (Entry is Immutable)
             */
            refreshData();
        }
    }

    private void addSortingFunctionality() {
        comparator = new TableComparator();
        tableViewer.setComparator(comparator);
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn column = table.getColumn(i);
            column.addSelectionListener(new SelectionListener(column, i));
        }
        table.setSortDirection(SWT.UP);
        table.setSortColumn(locationColumn.getColumn());
    }

    class ContentProvider implements IStructuredContentProvider {

        private ListMultimap<DependencyInfo, Optional<ProjectCoordinate>> data;
        private List<IProjectCoordinateAdvisor> strategies = Lists.newArrayList();

        public ContentProvider() {
            Map<DependencyInfo, Collection<Optional<ProjectCoordinate>>> map = Maps.newHashMap();
            data = Multimaps.newListMultimap(map, new Supplier<List<Optional<ProjectCoordinate>>>() {
                @Override
                public List<Optional<ProjectCoordinate>> get() {
                    return Lists.newArrayList();
                }
            });
        }

        public void setData(final Set<DependencyInfo> dependencyInfos) {
            data.clear();
            new ResolvingDependenciesJob("Resolving dependencies", dependencyInfos).schedule();
        }

        private final class ResolvingDependenciesJob extends Job {

            private Set<DependencyInfo> dependencyInfos;

            public ResolvingDependenciesJob(String name, final Set<DependencyInfo> dependencyInfos) {
                super(name);
                this.dependencyInfos = dependencyInfos;
            }

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask("Resolving dependencies", dependencyInfos.size());
                strategies = pcAdvisorsService.getAdvisors();
                for (DependencyInfo dependency : dependencyInfos) {
                    monitor.subTask("Resolving: " + dependency.getFile().getName());
                    for (IProjectCoordinateAdvisor strategy : strategies) {
                        data.put(dependency, strategy.suggest(dependency));
                    }
                    monitor.worked(1);
                }
                refreshUI();
                return Status.OK_STATUS;
            }

            private void refreshUI() {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        refreshTableUI();
                    }
                });
            }

        }

        public List<IProjectCoordinateAdvisor> getStrategies() {
            return strategies;
        }

        @Override
        public void dispose() {
            // unused in this case
        }

        @Override
        public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            // unused in this case
        }

        @Override
        public Object[] getElements(final Object inputElement) {
            return data.asMap().entrySet().toArray();
        }

    }

    class TableComparator extends ViewerComparator {
        private int column = 0;
        private int direction = SWT.UP;

        public int getDirection() {
            return direction;
        }

        public void setColumn(final int column) {
            if (column == this.column) {
                switch (direction) {
                case SWT.NONE:
                    direction = SWT.UP;
                    break;
                case SWT.UP:
                    direction = SWT.DOWN;
                    break;
                default:
                    direction = SWT.NONE;
                    break;
                }
            } else {
                this.column = column;
                direction = SWT.UP;
            }
        }

        @Override
        public int compare(final Viewer viewer, final Object e1, final Object e2) {
            int result = 0;
            if (direction == SWT.NONE) {
                return 0;
            }
            if (e1 instanceof Entry && e2 instanceof Entry) {
                Entry<DependencyInfo, Collection<Optional<ProjectCoordinate>>> firstElement = cast(e1);
                Entry<DependencyInfo, Collection<Optional<ProjectCoordinate>>> secondElement = cast(e2);

                switch (column) {
                case COLUMN_LOCATION:
                    result = compareLocation(firstElement.getKey(), secondElement.getKey());
                    break;
                case COLUMN_COORDINATE:
                    result = compareCoordinate(firstElement.getValue(), secondElement.getValue());
                    break;
                default:
                    result = 0;
                    break;
                }
            }
            if (direction == SWT.DOWN) {
                return -result;
            }
            return result;
        }

        private int compareCoordinate(final Collection<Optional<ProjectCoordinate>> firstElement,
                final Collection<Optional<ProjectCoordinate>> secondElement) {
            Optional<ProjectCoordinate> optionalCoordinateFirstElement = findFirstMatchingCoordinate(firstElement);
            Optional<ProjectCoordinate> optionalCoordinateSecondElement = findFirstMatchingCoordinate(secondElement);

            if (optionalCoordinateFirstElement.isPresent()) {
                if (optionalCoordinateSecondElement.isPresent()) {
                    return optionalCoordinateFirstElement.get().toString()
                            .compareTo(optionalCoordinateSecondElement.get().toString());
                } else {
                    return -1;
                }
            } else {
                if (optionalCoordinateSecondElement.isPresent()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }

        private int compareLocation(final DependencyInfo firstElement, final DependencyInfo secondElement) {
            int compareScore = -firstElement.getType().compareTo(secondElement.getType());
            if (compareScore == 0) {
                return firstElement.getFile().getName().compareToIgnoreCase(secondElement.getFile().getName());
            }
            return compareScore;
        }

    }

    class SelectionListener extends SelectionAdapter {

        private final TableColumn tableColumn;
        private final int index;

        public SelectionListener(final TableColumn tableColumn, final int index) {
            this.tableColumn = tableColumn;
            this.index = index;
        }

        @Override
        public void widgetSelected(final SelectionEvent e) {
            comparator.setColumn(index);
            int direction = comparator.getDirection();
            tableViewer.getTable().setSortDirection(direction);
            tableViewer.getTable().setSortColumn(tableColumn);
            refreshTableUI();
        }
    }

    private void addFilterFunctionality() {
        final ViewerFilter manualAssignedFilter = new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof Entry) {
                    Collection<Optional<ProjectCoordinate>> value = extractProjectCoordinates(element);
                    return isManualMapping(value);
                }
                return false;
            }

            private boolean isManualMapping(Collection<Optional<ProjectCoordinate>> pcs) {
                int indexOfManualMapping = pcAdvisorsService.getAdvisors().indexOf(manualPcAdvisor);
                Optional<ProjectCoordinate> opc = get(pcs, indexOfManualMapping);
                return opc.isPresent();
            }
        };

        final ViewerFilter conflictingCoordinatesFilter = new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof Entry) {
                    Collection<Optional<ProjectCoordinate>> value = extractProjectCoordinates(element);
                    return newHashSet(presentInstances(value)).size() > 1;
                }
                return false;
            }
        };

        final ViewerFilter missingCoordinatesFilter = new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof Entry) {
                    Collection<Optional<ProjectCoordinate>> value = extractProjectCoordinates(element);
                    return isEmpty(presentInstances(value));
                }
                return true;
            }
        };

        IAction showAll = new Action("Show all", Action.AS_RADIO_BUTTON) {

            @Override
            public void run() {
                refreshTableUI();
            }

        };

        IAction showMissingCoord = new TableFilterAction("Show only missing coordinates", Action.AS_RADIO_BUTTON,
                missingCoordinatesFilter);
        IAction showConflictingCoord = new TableFilterAction("Show only conflicting coordinates",
                Action.AS_RADIO_BUTTON, conflictingCoordinatesFilter);
        IAction showManualAssignedCoord = new TableFilterAction("Show only manually assigned coordinates",
                Action.AS_RADIO_BUTTON, manualAssignedFilter);

        getViewSite().getActionBars().getMenuManager().add(showAll);
        getViewSite().getActionBars().getMenuManager().add(showMissingCoord);
        getViewSite().getActionBars().getMenuManager().add(showConflictingCoord);
        getViewSite().getActionBars().getMenuManager().add(showManualAssignedCoord);
        showAll.setChecked(true);

    }

    class TableFilterAction extends Action {

        private ViewerFilter filter;

        public TableFilterAction(String text, int style, ViewerFilter filter) {
            super(text, style);
            this.filter = filter;
        }

        @Override
        public void run() {
            if (isChecked()) {
                if (!isFilterAlreadyAdded()) {
                    tableViewer.addFilter(filter);
                }
            } else {
                tableViewer.removeFilter(filter);
            }
            refreshTableUI();
        }

        private boolean isFilterAlreadyAdded() {
            for (ViewerFilter viewerFilter : tableViewer.getFilters()) {
                if (viewerFilter.equals(filter)) {
                    return true;
                }
            }
            return false;
        }

    }

    private void addRefreshButton() {

        IAction refreshAction = new Action() {

            @Override
            public void run() {
                refreshData();
            }
        };
        refreshAction.setToolTipText("Refresh");
        refreshAction.setImageDescriptor(images.getDescriptor(ELCL_REFRESH));

        getViewSite().getActionBars().getToolBarManager().add(refreshAction);
    }

    private void refreshData() {
        new UIJob("Refreshing View...") {
            {
                schedule();
            }

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                contentProvider.setData(dependencyListener.getDependencies());
                refreshTableUI();
                return Status.OK_STATUS;
            }
        };
    }

    private void refreshTableUI() {
        tableViewer.setLabelProvider(new ViewLabelProvider());
        locationColumn.setLabelProvider(new LocationTooltip());
        coordinateColumn.setLabelProvider(new CoordinateTooltip());
        tableViewer.refresh();
    }

    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(final Object obj, final int index) {
            if (obj instanceof Entry) {
                Entry<DependencyInfo, Collection<Optional<ProjectCoordinate>>> entry = cast(obj);
                DependencyInfo dependencyInfo = entry.getKey();
                switch (index) {
                case COLUMN_LOCATION:
                    String name = dependencyInfo.getFile().getName();
                    switch (dependencyInfo.getType()) {
                    case JRE:
                        return dependencyInfo.getHint(EXECUTION_ENVIRONMENT).or(name);
                    case PROJECT:
                        return dependencyInfo.getHint(PROJECT_NAME).or(name);
                    default:
                        return name;
                    }
                case COLUMN_COORDINATE:
                    Optional<ProjectCoordinate> pc = findFirstMatchingCoordinate(entry.getValue());
                    if (pc.isPresent()) {
                        return pc.get().toString();
                    }
                default:
                    return "";
                }
            }

            return "";
        }

        @Override
        public Image getColumnImage(final Object obj, final int index) {
            if (obj instanceof Entry) {
                DependencyInfo dependencyInfo = extractDependencyInfo(obj);
                switch (index) {
                case COLUMN_LOCATION:
                    return getImageForDependencyTyp(dependencyInfo);
                default:
                    return null;
                }
            }
            return null;
        }

        private Image getImageForDependencyTyp(final DependencyInfo dependencyInfo) {
            switch (dependencyInfo.getType()) {
            case JRE:
                return images.getImage(OBJ_JRE);
            case JAR:
                return images.getImage(OBJ_JAR);
            case PROJECT:
                return images.getImage(OBJ_JAVA_PROJECT);
            default:
                return null;
            }
        }

    }

    abstract class ToolTipProvider extends CellLabelProvider {
        @Override
        public void update(final ViewerCell cell) {
            cell.setText(cell.getText());
        }

        @Override
        public String getToolTipText(final Object element) {
            if (element instanceof Entry) {
                Entry<DependencyInfo, Collection<Optional<ProjectCoordinate>>> entry = cast(element);
                return generateTooltip(entry);
            }
            return "";
        }

        protected abstract String generateTooltip(Entry<DependencyInfo, Collection<Optional<ProjectCoordinate>>> entry);

        @Override
        public Point getToolTipShift(final Object object) {
            return new Point(5, 5);
        }

        @Override
        public int getToolTipDisplayDelayTime(final Object object) {
            return 100;
        }

        @Override
        public int getToolTipTimeDisplayed(final Object object) {
            return 10000;
        }

    }

    class LocationTooltip extends ToolTipProvider {

        @Override
        protected String generateTooltip(final Entry<DependencyInfo, Collection<Optional<ProjectCoordinate>>> entry) {
            DependencyInfo dependencyInfo = entry.getKey();
            StringBuilder sb = new StringBuilder();
            sb.append("Location: ");
            if (dependencyInfo.getType() == DependencyType.PROJECT) {
                sb.append(dependencyInfo.getFile().getPath());
            } else {
                sb.append(dependencyInfo.getFile().getAbsolutePath());
            }
            sb.append(LINE_SEPARATOR);

            sb.append("Type: ");
            sb.append(dependencyInfo.getType().toString());

            Map<String, String> hints = dependencyInfo.getHints();
            if (hints != null && !hints.isEmpty()) {
                sb.append(LINE_SEPARATOR);
                sb.append("Hints: ");
                for (Entry<String, String> hint : hints.entrySet()) {
                    sb.append(LINE_SEPARATOR);
                    sb.append("  ");
                    sb.append(hint.getKey());
                    sb.append(": ");
                    sb.append(hint.getValue());
                }
            }

            return sb.toString();
        }

    }

    class CoordinateTooltip extends ToolTipProvider {

        @Override
        protected String generateTooltip(final Entry<DependencyInfo, Collection<Optional<ProjectCoordinate>>> entry) {
            DependencyInfo dependencyInfo = entry.getKey();
            StringBuilder sb = new StringBuilder();
            List<IProjectCoordinateAdvisor> strategies = contentProvider.getStrategies();
            List<Optional<ProjectCoordinate>> coordinates = Lists.newArrayList(entry.getValue());

            for (int i = 0; i < strategies.size(); i++) {
                IProjectCoordinateAdvisor strategy = strategies.get(i);
                Optional<ProjectCoordinate> coordinate = coordinates.get(i);
                if (i != 0) {
                    sb.append(LINE_SEPARATOR);
                }
                sb.append(strategy.getClass().getSimpleName());
                sb.append(": ");
                Optional<ProjectCoordinate> optionalCoordinate = strategy.suggest(dependencyInfo);
                if (optionalCoordinate.isPresent()) {
                    sb.append(optionalCoordinate.get().toString());
                } else {
                    if (coordinate.isPresent()) {
                        sb.append(coordinate.get().toString());
                    } else {
                        sb.append("unknown");
                    }
                }
            }
            return sb.toString();
        }

    }

    @Override
    public void setFocus() {
        tableViewer.getControl().setFocus();
    }

    @Subscribe
    public void onEvent(AdvisorConfigurationChangedEvent e) throws IOException {
        refreshData();
    }

    private Optional<ProjectCoordinate> findFirstMatchingCoordinate(Collection<Optional<ProjectCoordinate>> pcs) {
        return fromNullable(getFirst(presentInstances(pcs), null));
    }

    private DependencyInfo extractDependencyInfo(final Object obj) {
        Entry<DependencyInfo, Collection<Optional<ProjectCoordinate>>> entry = cast(obj);
        return entry.getKey();
    }

    private Collection<Optional<ProjectCoordinate>> extractProjectCoordinates(final Object obj) {
        Entry<DependencyInfo, Collection<Optional<ProjectCoordinate>>> entry = cast(obj);
        return entry.getValue();
    }
}
