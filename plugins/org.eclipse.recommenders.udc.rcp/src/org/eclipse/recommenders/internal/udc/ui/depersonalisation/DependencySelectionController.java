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
package org.eclipse.recommenders.internal.udc.ui.depersonalisation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.recommenders.internal.udc.PreferenceUtil;
import org.eclipse.recommenders.internal.udc.ui.TreeComposite;
import org.eclipse.recommenders.internal.udc.ui.TreeCompositeController;
import org.eclipse.swt.graphics.Image;

import com.google.common.collect.Sets;

public class DependencySelectionController extends TreeCompositeController {

    @Inject
    private Set<File> dependencies;

    public DependencySelectionController(final TreeComposite composite) {
        super(composite);
    }

    public DependencySelectionController() {
    }

    @Override
    protected void init() {
        super.init();
        getViewer().setContentProvider(createContentProvider());
        getViewer().setLabelProvider(createLabelProvider());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        this.addPropertyChangeListener(DependencySelectionController.filterTextProperty,
                createFilterTextChangedListener());
    }

    private PropertyChangeListener createFilterTextChangedListener() {

        return new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                updateFilter(evt.getNewValue().toString());
            }

        };
    }

    protected void updateFilter(final String filterText) {
        getViewer().setFilters(new ViewerFilter[] { new FileFilter(filterText) });
    }

    private IContentProvider createContentProvider() {
        return new ITreeContentProvider() {

            @Override
            public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public boolean hasChildren(final Object element) {
                return false;
            }

            @Override
            public Object getParent(final Object element) {
                return null;
            }

            @Override
            public Object[] getElements(final Object inputElement) {
                return (Object[]) inputElement;
            }

            @Override
            public Object[] getChildren(final Object parentElement) {
                return null;
            }
        };
    }

    private CellLabelProvider createLabelProvider() {

        return new CellLabelProvider() {
            Image libraryImg = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);

            @Override
            public void update(final ViewerCell cell) {
                cell.setText(cell.getElement().toString());
                cell.setImage(libraryImg);
            }
        };
    }

    public void setProjects(final IProject[] projects) {
        dependencies = getDependenciesForProjects(projects);

        getViewer().setInput(dependencies.toArray(new File[dependencies.size()]));
    }

    private Set<File> getDependenciesForProjects(final IProject[] projects) {
        final Set<File> dependencies = Sets.newHashSet();
        for (final IProject project : projects) {
            if (!JavaProject.hasJavaNature(project)) {
                continue;
            }
            final IJavaProject javaProject = JavaCore.create(project);
            if (javaProject == null) {
                continue;
            }
            addLibraryDependencies(projectServices.getModelFacade(javaProject).getDependencyLocations(), dependencies);
        }
        return dependencies;
    }

    private void addLibraryDependencies(final File[] dependencies, final Set<File> result) {
        for (final File file : dependencies) {
            if (file.isDirectory()) {
                continue;
            }
            result.add(file);
        }
    }

    public void selectDependenciesFromPreferences() {
        final Set<String> preferenceFiles = getEnabledDependenciesFromPreferences();
        final Set<File> selectedFiles = new HashSet<File>();
        for (final String absolutPath : preferenceFiles) {
            final File file = new File(absolutPath);
            if (dependencies.contains(file)) {
                selectedFiles.add(file);
            }
        }
        setElementsChecked(true, selectedFiles.toArray());
    }

    private Set<String> getEnabledDependenciesFromPreferences() {
        final Set<String> result = Sets.newHashSet(PreferenceUtil.getEnabledLibraries());
        return result;
    }

    public String[] getDependencies() {
        final Object[] selectedElements = getCheckedElements();
        final String[] result = new String[selectedElements.length];
        for (int i = 0; i < result.length; i++) {
            final File selectedFile = (File) selectedElements[i];
            result[i] = selectedFile.getAbsolutePath();
        }
        return result;
    }
}
