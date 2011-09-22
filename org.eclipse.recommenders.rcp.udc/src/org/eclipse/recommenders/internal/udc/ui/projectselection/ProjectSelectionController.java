/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.projectselection;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.recommenders.internal.udc.ProjectProvider;
import org.eclipse.recommenders.internal.udc.ui.TreeCompositeController;
import org.eclipse.recommenders.internal.udc.ui.preferences.ProjectPreferenceUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

public class ProjectSelectionController {
    ProjectSelectionComposite composite;
    PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public static final String selectedProjectsProperty = "selectedProjects";
    TreeCompositeController treeController;
    IProject[] selectedProjects;
    ProjectFilter projectNameFilter;
    OnlyNewProjectsFilter onlyNewProjectsFilter;
    RecommenderProjectsContentProvider contentProvider;

    public Composite createControls(final Composite parent) {
        composite = new ProjectSelectionComposite(parent, SWT.NONE);
        addOnlyNewProjectsSelectedListener();
        initializeTreeController();
        initializeContent();
        return composite;
    }

    private void addOnlyNewProjectsSelectedListener() {
        composite.getOnlyNewButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                updateOnlyNewProjectsFilter();
            }
        });
    }

    protected void updateOnlyNewProjectsFilter() {
        if (composite.getOnlyNewButton().getSelection()) {
            onlyNewProjectsFilter = new OnlyNewProjectsFilter();
        } else {
            onlyNewProjectsFilter = null;
        }
        updateFilters();
    }

    private void initializeTreeController() {
        treeController = new TreeCompositeController(composite.getTreeComposite());
        treeController.addPropertyChangeListener(TreeCompositeController.filterTextProperty,
                createFilterTextChangedListener());
        treeController.addPropertyChangeListener(TreeCompositeController.checkedElementsProperty,
                createSelectedElementsChangedListener());
        treeController.getViewer().setContentProvider(contentProvider = new RecommenderProjectsContentProvider());
        treeController.getViewer().setLabelProvider(createLabelProvider());
    }

    public void initializeContent() {
        treeController.getViewer().setInput("");
    }

    private LabelProvider createLabelProvider() {
        return new LabelProvider() {
            Image projectImage = PlatformUI.getWorkbench().getSharedImages().getImage("IMG_OBJ_PROJECT");

            @Override
            public String getText(final Object element) {
                return ((IProject) element).getName();
            }

            @Override
            public Image getImage(final Object element) {
                return projectImage;
            }
        };
    }

    private PropertyChangeListener createSelectedElementsChangedListener() {
        return new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                updateSelectedProjects((Object[]) evt.getNewValue());
            }
        };
    }

    private PropertyChangeListener createFilterTextChangedListener() {
        return new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                updateFilterText(evt.getNewValue().toString());
            }
        };
    }

    protected void updateSelectedProjects(final Object[] selectedElements) {
        selectedProjects = Arrays.copyOf(selectedElements, selectedElements.length, IProject[].class);
        propertyChangeSupport.firePropertyChange(selectedProjectsProperty, null, selectedProjects);
    }

    protected void updateFilterText(final String filterString) {
        if (filterString.isEmpty()) {
            projectNameFilter = null;
        } else {
            projectNameFilter = new ProjectFilter(filterString);
        }
        updateFilters();
    }

    private void updateFilters() {
        treeController.getViewer().setFilters(new ViewerFilter[] {});
        if (onlyNewProjectsFilter != null) {
            treeController.getViewer().addFilter(onlyNewProjectsFilter);
        }
        if (projectNameFilter != null) {
            treeController.getViewer().addFilter(projectNameFilter);
        }

        treeController.getViewer().setCheckedElements(treeController.getCheckedElements());
    }

    public IProject[] getSelectedProjects() {
        return selectedProjects;
    }

    public void selectProjectsFromProperties() {
        final ArrayList<IProject> exportedProjects = new ArrayList<IProject>();
        for (final IProject project : new ProjectProvider().getAllRecommenderProjectsInWorkspace()) {
            final Boolean enabled = ProjectPreferenceUtil.isExportEnabled(project);
            if (enabled != null && enabled) {
                exportedProjects.add(project);
            }
        }
        selectProjects(exportedProjects.toArray(new IProject[exportedProjects.size()]));
    }

    public void selectProjects(final IProject[] projects) {
        treeController.setElementsChecked(true, projects);
    }

    public void setOnlyNewProjectsVisible(final boolean visible) {
        composite.getOnlyNewButton().setSelection(visible);
        updateOnlyNewProjectsFilter();
    }
}
