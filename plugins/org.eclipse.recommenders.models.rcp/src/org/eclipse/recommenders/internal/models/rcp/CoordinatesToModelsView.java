/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz, Marcel Bruch - initial API and implementation
 */
package org.eclipse.recommenders.internal.models.rcp;

import static org.eclipse.recommenders.internal.models.rcp.ImageProvider.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Constants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

public class CoordinatesToModelsView extends ViewPart {

    private TreeViewer treeViewer;
    private EclipseDependencyListener dependencyListener;

    private ImageProvider imageProvider;

    private IProjectCoordinateProvider pcProvider;
    private IModelIndex modelIndex;
    private EclipseModelRepository eclipseModelRepository;

    @Inject
    public CoordinatesToModelsView(final EventBus workspaceBus,
            final EclipseDependencyListener eclipseDependencyListener, final IProjectCoordinateProvider pcProvider,
            final IModelIndex modelIndex, final EclipseModelRepository eclipseModelRepository) {
        dependencyListener = eclipseDependencyListener;
        this.pcProvider = pcProvider;
        this.modelIndex = modelIndex;
        this.eclipseModelRepository = eclipseModelRepository;
        imageProvider = new ImageProvider();
        workspaceBus.register(this);
    }

    @Override
    public void createPartControl(Composite parent) {
        Tree dependencyTree = new Tree(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL);
        dependencyTree.setHeaderVisible(true);
        dependencyTree.setLinesVisible(true);
        createColumn(dependencyTree, "Dependency", 400);
        createColumn(dependencyTree, "Project Coordinate", 200);
        createColumn(dependencyTree, "CALL", 50);
        createColumn(dependencyTree, "OVRM", 50);
        createColumn(dependencyTree, "OVRP", 50);
        createColumn(dependencyTree, "OVRD", 50);
        createColumn(dependencyTree, "SELFC", 50);
        createColumn(dependencyTree, "SELFM", 50);
        treeViewer = new TreeViewer(dependencyTree);
        treeViewer.setContentProvider(new ContentProvider());
        treeViewer.setLabelProvider(new LabelProvider());
        treeViewer.setSorter(new ViewerSorter());
        addContextMenu();
        updateContent();
    }

    private void addContextMenu() {

        final MenuManager menuManager = new MenuManager();
        Menu contextMenu = menuManager.createContextMenu(treeViewer.getTree());
        menuManager.setRemoveAllWhenShown(true);
        treeViewer.getControl().setMenu(contextMenu);

        menuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                IStructuredSelection selection = Checks.cast(treeViewer.getSelection());
                Set<DependencyInfo> deps = extractSelectedDependencies(selection);
                if (!deps.isEmpty()) {
                    menuManager.add(new TriggerModelDownloadAction("Download models", deps));
                }
            }
        });
    }

    private Set<DependencyInfo> extractSelectedDependencies(IStructuredSelection selection) {
        final Set<DependencyInfo> selectedDependencies = Sets.newHashSet();

        for (Object element : selection.toList()) {
            if (element instanceof Dependency) {
                Dependency dependency = (Dependency) element;
                selectedDependencies.add(dependency.info);
            } else if (element instanceof Project) {
                Project project = (Project) element;
                for (Dependency dependency : project.dependencies) {
                    selectedDependencies.add(dependency.info);
                }
            }
        }
        return selectedDependencies;
    }

    private void createColumn(Tree dependencyTree, String label, int width) {
        TreeColumn column = new TreeColumn(dependencyTree, SWT.LEFT);
        column.setText(label);
        column.setWidth(width);
    }

    private void updateContent() {
        List<Project> projects = createModel();
        treeViewer.setInput(projects);
    }

    private List<Project> createModel() {
        List<Project> result = new ArrayList<Project>();
        Set<DependencyInfo> projectsDependencyInfos = dependencyListener.getProjects();
        for (DependencyInfo projectDI : projectsDependencyInfos) {
            Project project = new Project(projectDI);
            List<Dependency> dependencies = new ArrayList<Dependency>();
            Set<DependencyInfo> dependenciesForProject = dependencyListener.getDependenciesForProject(projectDI);
            for (DependencyInfo dependencyInfo : dependenciesForProject) {
                if (!dependencyInfo.equals(projectDI)) {
                    dependencies.add(new Dependency(dependencyInfo, project));
                }
            }
            project.dependencies = dependencies;
            result.add(project);
        }
        return result;
    }

    @Override
    public void setFocus() {
        treeViewer.getControl().setFocus();
    }

    public class Project {
        List<Dependency> dependencies;
        DependencyInfo info;

        public Project(DependencyInfo projectDependencyInfo) {
            info = projectDependencyInfo;
        }

        @Override
        public String toString() {
            return info.getFile().getName();
        }
    }

    public class Dependency {
        public DependencyInfo info;
        public Project parent;

        public Dependency(DependencyInfo dependencyInfo, Project parent) {
            info = dependencyInfo;
            this.parent = parent;
        }

        @Override
        public String toString() {
            return info.getFile().getName();
        }
    }

    public class LabelProvider extends org.eclipse.jface.viewers.LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            switch (columnIndex) {
            case 0:
                if (element instanceof Project) {
                    return imageProvider.provideImage(IMG_PROJECT);
                }
                if (element instanceof Dependency) {
                    Dependency dependency = (Dependency) element;
                    return getImageForDependencyTyp(dependency.info);
                }
            }
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            switch (columnIndex) {
            case 0:
                if (element instanceof Project) {
                    return ((Project) element).info.getFile().getName();
                }
                if (element instanceof Dependency) {
                    return ((Dependency) element).info.getFile().getName();
                }
            case 1:
                if (element instanceof Dependency) {
                    Dependency dependency = (Dependency) element;
                    ProjectCoordinate pc = pcProvider.resolve(dependency.info).orNull();
                    return pc == null ? null : pc.toString();
                }
            case 2:
                if (element instanceof Dependency) {
                    return findModelCoordinateVersion((Dependency) element, Constants.CLASS_CALL_MODELS);
                }
            case 3:
                if (element instanceof Dependency) {
                    return findModelCoordinateVersion((Dependency) element, Constants.CLASS_OVRM_MODEL);
                }
            case 4:
                if (element instanceof Dependency) {
                    return findModelCoordinateVersion((Dependency) element, Constants.CLASS_OVRP_MODEL);
                }
            case 5:
                if (element instanceof Dependency) {
                    return findModelCoordinateVersion((Dependency) element, Constants.CLASS_OVRD_MODEL);
                }
            case 6:
                if (element instanceof Dependency) {
                    return findModelCoordinateVersion((Dependency) element, Constants.CLASS_SELFC_MODEL);
                }
            case 7:
                if (element instanceof Dependency) {
                    return findModelCoordinateVersion((Dependency) element, Constants.CLASS_SELFM_MODEL);
                }
            }
            return null;
        }

        private String findModelCoordinateVersion(Dependency dependency, String modelType) {
            ProjectCoordinate pc = pcProvider.resolve(dependency.info).orNull();
            if (pc != null) {
                ModelCoordinate mc = modelIndex.suggest(pc, modelType).orNull();
                return mc == null ? "" : mc.getVersion();
            }
            return "";
        }

        private Image getImageForDependencyTyp(final DependencyInfo dependencyInfo) {
            switch (dependencyInfo.getType()) {
            case JRE:
                return imageProvider.provideImage(IMG_JRE);
            case JAR:
                return imageProvider.provideImage(IMG_JAR);
            case PROJECT:
                return imageProvider.provideImage(IMG_PROJECT);
            default:
                return null;
            }
        }
    }

    public class ContentProvider extends ArrayContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof Project) {
                return ((Project) parentElement).dependencies.toArray();
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            return element instanceof Dependency ? ((Dependency) element).parent : null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof List) {
                return !((List<?>) element).isEmpty();
            }
            if (element instanceof Project) {
                return !((Project) element).dependencies.isEmpty();
            }
            return false;
        }
    }

    private final class TriggerModelDownloadAction extends Action {
        private final Set<DependencyInfo> deps;
        private final String[] modelTypes = { Constants.CLASS_CALL_MODELS, Constants.CLASS_OVRM_MODEL,
                Constants.CLASS_OVRP_MODEL, Constants.CLASS_OVRD_MODEL, Constants.CLASS_SELFC_MODEL,
                Constants.CLASS_SELFM_MODEL };

        private TriggerModelDownloadAction(String text, Set<DependencyInfo> deps) {
            super(text);
            this.deps = deps;
        }

        @Override
        public void run() {
            Set<ModelCoordinate> mcs = Sets.newHashSet();
            for (DependencyInfo dep : deps) {
                ProjectCoordinate pc = pcProvider.resolve(dep).orNull();
                if (pc == null) {
                    continue;
                }
                for (String modelType : modelTypes) {
                    ModelCoordinate mc = modelIndex.suggest(pc, modelType).orNull();
                    if (mc != null) {
                        mcs.add(mc);
                    }
                }
            }
            new DownloadMultipleModelArchivesJob(eclipseModelRepository, mcs).schedule();
        }
    }
}
