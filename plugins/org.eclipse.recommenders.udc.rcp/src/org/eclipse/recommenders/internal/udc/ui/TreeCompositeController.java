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
package org.eclipse.recommenders.internal.udc.ui;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

public class TreeCompositeController {
    private TreeComposite composite;
    private ContainerCheckedTreeViewer viewer;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    public static final String filterTextProperty = "filterText";
    public static final String checkedElementsProperty = "checkedElements";
    /**
     * Well keep track at our selected elements since there is a bug in
     * checkboxtreeviewer. it removes the check state for elements wich are not
     * selected by filters.
     */
    private final Set<Object> checkedElements = new HashSet<Object>();

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

    public TreeCompositeController(final Composite parent) {
        Checks.ensureIsNotNull(parent);
        composite = new TreeComposite(parent, SWT.None);
        init();
    }

    public TreeCompositeController() {

    }

    public Composite createControls(final Composite parent) {
        this.composite = new TreeComposite(parent, SWT.NONE);
        init();
        return this.composite;
    }

    public TreeCompositeController(final TreeComposite composite) {
        Checks.ensureIsNotNull(composite);
        this.composite = composite;
        init();
    }

    protected void init() {
        viewer = new ContainerCheckedTreeViewer(composite.getTree());
        viewer.setComparator(createViewerComparator());
        addListeners();
    }

    private ViewerComparator createViewerComparator() {
        return new ViewerComparator();
    }

    protected void addListeners() {
        addSelectAllListener();
        addDeselectAllListener();
        addFilterLostFocusListener();
        addCheckStateListener();
        addFilterTextModifiedListener();
    }

    private void addFilterTextModifiedListener() {
        composite.getFilterText().addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                if (composite.getFilterText().getText().equals(TreeComposite.FILTER_TEXT_DEFAULT)) {
                    return;
                }
                propertyChangeSupport.firePropertyChange(filterTextProperty, null, getFilterText());
            }
        });
    }

    private void addCheckStateListener() {
        viewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(final CheckStateChangedEvent event) {
                trackCheckedState(event.getElement(), event.getChecked());
                propertyChangeSupport.firePropertyChange(checkedElementsProperty, null, getCheckedElements());
            }
        });
    }

    private void addFilterLostFocusListener() {
        composite.getFilterText().addFocusListener(new FocusListener() {

            @Override
            public void focusLost(final FocusEvent e) {
                if (composite.getFilterText().getText().isEmpty()) {
                    composite.getFilterText().setText(TreeComposite.FILTER_TEXT_DEFAULT);
                }
            }

            @Override
            public void focusGained(final FocusEvent e) {
                if (composite.getFilterText().getText().equals(TreeComposite.FILTER_TEXT_DEFAULT)) {
                    composite.getFilterText().setText("");
                }
            }
        });
    }

    private void addDeselectAllListener() {
        SelectionListener listener;
        listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                setVisibleItemsChecked(false);
            }

        };
        composite.getDeselectAllButton().addSelectionListener(listener);
    }

    private void addSelectAllListener() {
        final SelectionListener listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                setVisibleItemsChecked(true);
            }
        };
        composite.getSelectAllButton().addSelectionListener(listener);
    }

    protected void setVisibleItemsChecked(final boolean checkedState) {
        final List<Object> visibleElements = new ArrayList<Object>();
        final ITreeContentProvider contentProvider = (ITreeContentProvider) viewer.getContentProvider();
        collectVisibleElements(contentProvider, contentProvider.getElements(viewer.getInput()), visibleElements);

        setElementsChecked(checkedState, visibleElements.toArray());
    }

    public void setElementsChecked(final boolean checkedState, final Object[] elements) {
        for (final Object o : elements) {
            viewer.setChecked(o, checkedState);
            trackCheckedState(o, checkedState);
        }
        propertyChangeSupport.firePropertyChange(checkedElementsProperty, null, getCheckedElements());
    }

    private void trackCheckedState(final Object element, final boolean checkedState) {
        if (checkedState) {
            checkedElements.add(element);
        } else {
            checkedElements.remove(element);
        }
    }

    public TreeComposite getComposite() {
        return composite;
    }

    public ContainerCheckedTreeViewer getViewer() {
        return viewer;
    }

    public String getFilterText() {
        final String filterText = composite.getFilterText().getText();
        if (TreeComposite.FILTER_TEXT_DEFAULT.equals(filterText)) {
            return "";
        } else {
            return filterText;
        }
    }

    public Object[] getCheckedElements() {
        return checkedElements.toArray();
    }

    private void collectVisibleElements(final ITreeContentProvider cp, final Object[] elements,
            final List<Object> result) {
        for (final Object element : elements) {
            if (!isAllowedByFilters(element)) {
                continue;
            }

            result.add(element);
            if (viewer.getExpandedState(element)) {
                collectVisibleElements(cp, cp.getChildren(element), result);
            }
        }
    }

    private boolean isAllowedByFilters(final Object element) {
        for (final ViewerFilter filter : viewer.getFilters()) {
            if (!filter.select(viewer, null, element)) {
                return false;
            }
        }
        return true;
    }

    public void setEnabled(final boolean enabled) {
        composite.getFilterText().setEnabled(enabled);
        composite.getTree().setEnabled(enabled);
        composite.getSelectAllButton().setEnabled(enabled);
        composite.getDeselectAllButton().setEnabled(enabled);
    }

}
