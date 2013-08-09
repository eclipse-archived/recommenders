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

import static org.eclipse.recommenders.models.DependencyInfo.*;
import static org.eclipse.recommenders.utils.IOUtils.LINE_SEPARATOR;
import static org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.advisors.ProjectCoordinateAdvisorService;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelIndexOpenedEvent;
import org.eclipse.recommenders.rcp.JavaModelEvents.JarPackageFragmentRootAdded;
import org.eclipse.recommenders.rcp.JavaModelEvents.JarPackageFragmentRootRemoved;
import org.eclipse.recommenders.rcp.JavaModelEvents.JavaProjectClosed;
import org.eclipse.recommenders.rcp.JavaModelEvents.JavaProjectOpened;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class ProjectCoordinatesView extends ViewPart {

    private static final int COLUMN_LOCATION = 0;
    private static final int COLUMN_COORDINATE = 1;

    private Image IMG_JRE = loadImage("icons/cview16/classpath.gif");
    private Image IMG_JAR = loadImage("icons/cview16/jar_obj.gif");
    private Image IMG_PROJECT = loadImage("icons/cview16/projects.gif");

    private Composite parent;
    private TableViewer tableViewer;
    private ContentProvider contentProvider;

    private final EclipseDependencyListener dependencyListener;
    private final ProjectCoordinateAdvisorService pcAdvisors;

    private TableViewerColumn locationColumn;
    private TableViewerColumn coordinateColumn;
    private TableComparator comparator;
    private Table table;
    private EventBus bus;

    @Inject
    public ProjectCoordinatesView(final EventBus bus, final EclipseDependencyListener eclipseDependencyListener,
            final ProjectCoordinateAdvisorService pcAdvisorService) {
        this.bus = bus;
        dependencyListener = eclipseDependencyListener;
        pcAdvisors = pcAdvisorService;

    }

    private Image loadImage(final String pathToFile) {
        ImageDescriptor imageDescriptor = imageDescriptorFromPlugin(Constants.BUNDLE_ID, pathToFile);
        if (imageDescriptor != null) {
            Image image = imageDescriptor.createImage();
            return image;
        }
        return null;
    }

    @Subscribe
    public void onEvent(final JavaProjectOpened e) {
        checkForDependencyUpdates();
    }

    @Subscribe
    public void onEvent(final JavaProjectClosed e) {
        checkForDependencyUpdates();
    }

    @Subscribe
    public void onEvent(final JarPackageFragmentRootAdded e) {
        checkForDependencyUpdates();
    }

    @Subscribe
    public void onEvent(final JarPackageFragmentRootRemoved e) {
        checkForDependencyUpdates();
    }

    @Subscribe
    public void onEvent(ModelIndexOpenedEvent e) {
        checkForDependencyUpdates();
    }

    protected void checkForDependencyUpdates() {
        if (parent != null) {
            parent.getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    contentProvider.setData(dependencyListener.getDependencies());
                    refreshTable();
                }

            });
        }
    }

    protected void setLabelProviderForTooltips() {
        locationColumn.setLabelProvider(new LocationTooltip());
    }

    private void refreshTable() {
        tableViewer.setLabelProvider(new ViewLabelProvider());
        locationColumn.setLabelProvider(new LocationTooltip());
        coordinateColumn.setLabelProvider(new CoordinateTooltip());
        tableViewer.refresh();
    }

    @Override
    public void createPartControl(final Composite parent) {
        bus.register(this);

        this.parent = parent;

        Composite composite = new Composite(parent, SWT.NONE);
        TableColumnLayout tableLayout = new TableColumnLayout();
        composite.setLayout(tableLayout);

        tableViewer = new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

        tableViewer.setLabelProvider(new ViewLabelProvider());
        contentProvider = new ContentProvider();
        tableViewer.setContentProvider(contentProvider);
        tableViewer.setInput(getViewSite());
        comparator = new TableComparator();
        tableViewer.setComparator(comparator);

        ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);

        locationColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        TableColumn tableColumn = locationColumn.getColumn();
        tableColumn.setText("Location");
        // tableColumn.setWidth(200);
        tableColumn.addSelectionListener(new SelectionListener(tableColumn, 0));
        tableLayout.setColumnData(tableColumn, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));

        coordinateColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        tableColumn = coordinateColumn.getColumn();
        tableColumn.setText("Coordinate");
        // tableColumn.setWidth(450);
        tableColumn.addSelectionListener(new SelectionListener(tableColumn, 1));
        tableLayout.setColumnData(tableColumn, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));

        table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setSortDirection(SWT.UP);
        table.setSortColumn(locationColumn.getColumn());

        checkForDependencyUpdates();
    }

    @Override
    public void dispose() {
        bus.unregister(this);
        IMG_JAR.dispose();
        IMG_PROJECT.dispose();
        IMG_JRE.dispose();
        super.dispose();
    }

    @Override
    public void setFocus() {
        tableViewer.getControl().setFocus();
    }

    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(final Object obj, final int index) {
            if (obj instanceof DependencyInfo) {
                DependencyInfo dependencyInfo = (DependencyInfo) obj;
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
                    Optional<ProjectCoordinate> optionalProjectCoordinate = pcAdvisors.suggest(dependencyInfo);
                    if (optionalProjectCoordinate.isPresent()) {
                        return optionalProjectCoordinate.get().toString();
                    }
                default:
                    return "";
                }
            }
            return "";
        }

        @Override
        public Image getColumnImage(final Object obj, final int index) {
            if (obj instanceof DependencyInfo) {
                DependencyInfo dependencyInfo = (DependencyInfo) obj;
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
                return IMG_JRE;
            case JAR:
                return IMG_JAR;
            case PROJECT:
                return IMG_PROJECT;
            default:
                return null;
            }
        }

    }

    class ContentProvider implements IStructuredContentProvider {

        private final List<DependencyInfo> data = new ArrayList<DependencyInfo>();

        public void setData(final Set<DependencyInfo> dependencyInfos) {
            data.clear();
            data.addAll(dependencyInfos);
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
            return data.toArray();
        }

    }

    abstract class ToolTipProvider extends CellLabelProvider {
        @Override
        public void update(final ViewerCell cell) {
            cell.setText(cell.getText());
        }

        @Override
        public String getToolTipText(final Object element) {
            if (element instanceof DependencyInfo) {
                DependencyInfo dependencyInfo = (DependencyInfo) element;
                return generateTooltip(dependencyInfo);
            }
            return "";
        }

        protected abstract String generateTooltip(DependencyInfo dependencyInfo);

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
        protected String generateTooltip(final DependencyInfo dependencyInfo) {
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
                for (Entry<String, String> entry : hints.entrySet()) {
                    sb.append(LINE_SEPARATOR);
                    sb.append("  ");
                    sb.append(entry.getKey());
                    sb.append(": ");
                    sb.append(entry.getValue());
                }
            }

            return sb.toString();
        }

    }

    class CoordinateTooltip extends ToolTipProvider {

        @Override
        protected String generateTooltip(final DependencyInfo dependencyInfo) {
            StringBuilder sb = new StringBuilder();
            List<IProjectCoordinateAdvisor> strategies = pcAdvisors.getAdvisors();

            for (IProjectCoordinateAdvisor strategy : strategies) {
                if (strategies.indexOf(strategy) != 0) {
                    sb.append(System.getProperty("line.separator"));
                }
                sb.append(strategy.getClass().getSimpleName());
                sb.append(": ");
                Optional<ProjectCoordinate> optionalCoordinate = strategy.suggest(dependencyInfo);
                if (optionalCoordinate.isPresent()) {
                    sb.append(optionalCoordinate.get().toString());
                } else {
                    sb.append("n/a");
                }
            }
            return sb.toString();
        }

    }

    public class TableComparator extends ViewerComparator {
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
            if (e1 instanceof DependencyInfo && e2 instanceof DependencyInfo) {
                DependencyInfo firstElement = (DependencyInfo) e1;
                DependencyInfo secondElement = (DependencyInfo) e2;

                switch (column) {
                case COLUMN_LOCATION:
                    result = compareLocation(firstElement, secondElement);
                    break;
                case COLUMN_COORDINATE:
                    result = compareCoordinate(firstElement, secondElement);
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

        private int compareCoordinate(final DependencyInfo firstElement, final DependencyInfo secondElement) {
            Optional<ProjectCoordinate> optionalCoordinateFirstElement = pcAdvisors.suggest(firstElement);
            Optional<ProjectCoordinate> optionalCoordinateSecondElement = pcAdvisors.suggest(secondElement);

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
            refreshTable();
        }
    };

}
