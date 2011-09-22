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
package org.eclipse.recommenders.internal.udc.ui.packageselection;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.recommenders.commons.utils.Checks;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class PackageTreeContentProvider implements ITreeContentProvider {
    IProject[] projects;
    Multimap<Package, Package> packageHirarchy = HashMultimap.create();
    Package rootPackage;
    List<Package> lazyLeafs;

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        if (newInput == null) {
            return;
        }
        if (!(newInput instanceof IProject[])) {
            throw new IllegalArgumentException("The input elements must be of type IProject[]");
        }
        this.projects = (IProject[]) newInput;
        packageHirarchy.clear();
        rootPackage = new Package("", null);
        lazyLeafs = null;
    }

    @Override
    public Package[] getElements(final Object inputElement) {
        if (packageHirarchy.containsKey(rootPackage)) {
            return packageHirarchy.get(rootPackage).toArray(new Package[0]);
        }
        for (final IProject project : projects) {
            addRecommendersDataFolder(rootPackage, project);
        }
        initializeChildPackages(rootPackage);
        return packageHirarchy.get(rootPackage).toArray(new Package[0]);
    }

    private void addRecommendersDataFolder(final Package p, final IProject project) {
        final IFolder recommendersFolder = project.getFolder(".recommenders");
        if (!recommendersFolder.exists()) {
            return;
        }
        final IFolder dataFolder = recommendersFolder.getFolder("data");
        if (!dataFolder.exists()) {
            return;
        }
        try {
            for (final IResource res : dataFolder.members()) {
                if (res instanceof IFolder) {
                    p.getRelatedFolders().add((IFolder) res);
                }
            }
        } catch (final CoreException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Package[] getChildren(final Object parentElement) {
        Checks.ensureIsInstanceOf(parentElement, Package.class);
        final Package parentPackage = (Package) parentElement;
        if (packageHirarchy.containsKey(parentPackage)) {
            return packageHirarchy.get(parentPackage).toArray(new Package[0]);
        }

        initializeChildPackages(parentPackage);
        return packageHirarchy.get(parentPackage).toArray(new Package[0]);
    }

    private void initializeChildPackages(final Package parentPackage) {
        final List<IFolder> subFolders = getSubFolders(parentPackage);
        for (final IFolder folder : subFolders) {
            final Package childPackage = getPackage(parentPackage, folder.getName());
            childPackage.getRelatedFolders().add(folder);
        }
    }

    private Package getPackage(final Package parent, final String name) {
        final Collection<Package> children = packageHirarchy.get(parent);
        for (final Package child : children) {
            if (child.getPackageName().equals(name)) {
                return child;
            }
        }
        final Package newPackage = new Package(name, parent);
        packageHirarchy.put(parent, newPackage);
        return newPackage;
    }

    private List<IFolder> getSubFolders(final Package p) {
        final List<IFolder> result = new ArrayList<IFolder>();
        for (final IFolder folder : p.getRelatedFolders()) {
            result.addAll(getSubFolders(folder));
        }
        return result;
    }

    private Collection<IFolder> getSubFolders(final IFolder folder) {
        final List<IFolder> result = new ArrayList<IFolder>();
        try {
            for (final IResource child : folder.members()) {
                if (child instanceof IFolder) {
                    final IFolder subFolder = (IFolder) child;
                    if (subFolder.getName().endsWith(".java")) {
                        continue;
                    }
                    result.add(subFolder);
                }
            }
        } catch (final CoreException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public Object getParent(final Object element) {
        final Package p = (Package) element;
        return p.parentPackage;
    }

    @Override
    public boolean hasChildren(final Object element) {
        return getChildren(element).length > 0;
    }

    public Package getPackage(final String packageIdentifier) {
        final String[] pathElements = packageIdentifier.split("\\.");
        Package[] currentPackages = getElements(rootPackage);
        for (int i = 0; i < pathElements.length; i++) {
            final Package currentElement = findNamedPackage(currentPackages, pathElements[i]);
            if (currentElement == null) {
                return null;
            }
            if (currentElement.getPackageIdentifier().equals(packageIdentifier)) {
                return currentElement;
            }
            currentPackages = getChildren(currentElement);
        }
        return null;
    }

    private Package findNamedPackage(final Package[] packages, final String packageName) {
        for (final Package element : packages) {
            if (element.getPackageName().equals(packageName)) {
                return element;
            }
        }
        return null;
    }

    public Package[] getLeafs() {
        if (lazyLeafs == null) {
            lazyLeafs = new ArrayList<Package>();
            for (final Package p : getElements(null)) {
                addLeafsToList(p);
            }
        }
        return lazyLeafs.toArray(new Package[lazyLeafs.size()]);

    }

    private void addLeafsToList(final Package p) {
        if (!hasChildren(p)) {
            lazyLeafs.add(p);
            return;
        }
        for (final Package child : getChildren(p)) {
            addLeafsToList(child);
        }
    }
}