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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.resources.IProject;
import org.eclipse.recommenders.internal.udc.PreferenceUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class PackageSelectionController implements PropertyChangeListener {
    PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    class MoveExpressionSelectionListener extends SelectionAdapter {
        public MoveExpressionSelectionListener(final RegExpTableController source, final RegExpTableController target) {
            super();
            this.source = source;
            this.target = target;
        }

        RegExpTableController source, target;

        @Override
        public void widgetSelected(final SelectionEvent e) {
            final RegularExpression[] selectedExpressions = source.getSelection();
            if (selectedExpressions.length == 0) {
                return;
            }
            source.removeSelectedExpressions();
            target.addExpressions(selectedExpressions);
            target.setSelection(selectedExpressions);
        }
    }

    PackageSelectionComposite packageSelectionComposite;
    RegExpTableController includeTableController, excludeTableController;
    PackageTreeController treeController;

    public void createControls(final Composite parent) {
        packageSelectionComposite = new PackageSelectionComposite(parent, SWT.NONE);

        createControllers();

        addListenersToButtons();

        initializeFromProperties();

    }

    private void createControllers() {
        treeController = new PackageTreeController(packageSelectionComposite.getPreviewTree());
        includeTableController = new RegExpTableController(packageSelectionComposite.getIncludedPackagesTable());
        includeTableController.addPropertyChangeListener(this);
        excludeTableController = new RegExpTableController(packageSelectionComposite.getExcludedPackagesTable());
        excludeTableController.addPropertyChangeListener(this);
    }

    private void addListenersToButtons() {
        packageSelectionComposite.getMoveToExcludesButton().addSelectionListener(
                new MoveExpressionSelectionListener(includeTableController, excludeTableController));
        packageSelectionComposite.getMoveToIncludesButton().addSelectionListener(
                new MoveExpressionSelectionListener(excludeTableController, includeTableController));
    }

    private void updatePreview() {
        treeController.updateExcludesPackages(excludeTableController.getRegularExpressions());
        treeController.updateIncludedPackages(includeTableController.getRegularExpressions());
        treeController.updateFilter();
    }

    public void setProjects(final IProject[] projects) {
        treeController.setProjects(projects);
    }

    private void initializeFromProperties() {
        final String[] exclude = PreferenceUtil.getExcludeExpressions();
        String[] include = PreferenceUtil.getIncludeExpressions();
        if (exclude.length == 0 && include.length == 0) {
            include = new String[] { ".*" };
        }
        excludeTableController.setRegularExpressions(exclude);
        includeTableController.setRegularExpressions(include);
        updatePreview();
    }

    public String[] getIncludeExpressions() {
        return includeTableController.getRegularExpressions();
    }

    public String[] getExcludeExpressions() {
        return excludeTableController.getRegularExpressions();
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RegExpTableController.regularExpressionsProperty)) {
            updatePreview();
        }
        propertyChangeSupport.firePropertyChange(evt);
    }

    public Control getControl() {
        return packageSelectionComposite;
    }

    public void setIncludeExpressions(final String[] expressions) {
        includeTableController.setRegularExpressions(expressions);
    }

    public void setExcludeExpressions(final String[] expressions) {
        excludeTableController.setRegularExpressions(expressions);
    }
}
