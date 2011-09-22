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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.internal.udc.ui.preferences.ControlDecorationDelegate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

public class RegExpTableController {
    ControlDecorationDelegate controlDecorationDelegate = new ControlDecorationDelegate();
    PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    PropertyChangeListener validateExpressionsListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            validate();
        }
    };
    public static final String regularExpressionsProperty = "regularExpressions";
    public static final String validationState = "validationState";
    String errorMsg;

    public RegExpTableController(final Table table) {
        viewer = new TableViewer(table);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.addDoubleClickListener(createDoubleClickListener());
        initializeTableColumn(table);
        propertyChangeSupport.addPropertyChangeListener(regularExpressionsProperty, validateExpressionsListener);

        updateInput();
    }

    private void initializeTableColumn(final Table table) {
        final TableViewerColumn column = new TableViewerColumn(viewer, viewer.getTable().getColumn(0));
        column.setLabelProvider(createLabelProvider());
        column.setEditingSupport(createEditingSupport());
    }

    private EditingSupport createEditingSupport() {
        return new EditingSupport(viewer) {

            TextCellEditor editor = new TextCellEditor(viewer.getTable());

            @Override
            protected void setValue(final Object element, final Object value) {
                final RegularExpression expression = (RegularExpression) element;
                expression.setRegExp(value.toString());
                if (expression.getRegExp().isEmpty()) {
                    expressions.remove(expression);
                    updateInput();
                } else {
                    viewer.update(element, null);
                }
                propertyChangeSupport.firePropertyChange(regularExpressionsProperty, null, getRegularExpressions());

            }

            @Override
            protected Object getValue(final Object element) {
                return element.toString();
            }

            @Override
            protected CellEditor getCellEditor(final Object element) {
                return editor;
            }

            @Override
            protected boolean canEdit(final Object element) {
                return element instanceof RegularExpression;
            }
        };
    }

    private IDoubleClickListener createDoubleClickListener() {
        return new IDoubleClickListener() {

            @Override
            public void doubleClick(final DoubleClickEvent event) {
                final Object selectedElement = ((IStructuredSelection) event.getSelection()).getFirstElement();
                if (selectedElement instanceof FillerElement) {
                    addNewExpression("enter regular expression");
                }
            }
        };
    }

    private CellLabelProvider createLabelProvider() {
        final StyledCellLabelProvider labelProvider = new StyledCellLabelProvider() {
            {
                setOwnerDrawEnabled(true);
            }
            Color grey = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
            Color black = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);

            @Override
            public void update(final ViewerCell cell) {
                cell.setText(cell.getElement().toString());
                if (cell.getElement() instanceof FillerElement) {
                    setTextColor(cell, grey);
                } else {
                    setTextColor(cell, black);
                }

            }

            private void setTextColor(final ViewerCell cell, final Color color) {
                final StyleRange styleRange = new StyleRange(0, cell.getText().length(), color, null);
                cell.setStyleRanges(new StyleRange[] { styleRange });
            }

        };
        return labelProvider;
    }

    TableViewer viewer;
    List<RegularExpression> expressions = new ArrayList<RegularExpression>();

    public void addExpressions(final RegularExpression... expressions) {
        Checks.ensureIsNotNull(expressions);
        if (expressions.length == 0) {
            return;
        }

        this.expressions.addAll(Arrays.asList(expressions));

        updateInput();
    }

    private void addNewExpression(final String expression) {
        RegularExpression regExp = null;
        regExp = new RegularExpression();
        regExp.setRegExp(expression);
        this.expressions.add(regExp);

        updateInput();

        viewer.editElement(regExp, 0);
    }

    public String[] getRegularExpressions() {
        final String[] result = new String[expressions.size()];
        for (int i = 0; i < result.length; i++) {
            String regExp = expressions.get(i).getRegExp().replace(".", "\\.");
            regExp = regExp.replace("*", ".*");
            result[i] = regExp;
        }
        return result;
    }

    public void setRegularExpressions(final String[] expressions) {
        for (final String expression : expressions) {
            if (expression.isEmpty()) {
                continue;
            }
            final RegularExpression regExp = new RegularExpression();
            regExp.setRegExp(expression.replace("\\.", ".").replace(".*", "*"));
            this.expressions.add(regExp);
        }
        updateInput();
    }

    private void updateInput() {
        final Object[] input = Arrays.copyOf(expressions.toArray(), expressions.size() + 1);
        input[input.length - 1] = new FillerElement();
        viewer.setInput(input);
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public RegularExpression[] getSelection() {
        final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        final List<RegularExpression> result = new ArrayList<RegularExpression>();
        for (final Object selectedElement : selection.toArray()) {
            if (selectedElement instanceof RegularExpression) {
                result.add((RegularExpression) selectedElement);
            }
        }
        return result.toArray(new RegularExpression[result.size()]);
    }

    public void removeSelectedExpressions() {
        final RegularExpression[] selection = getSelection();
        if (selection.length == 0) {
            return;
        }
        expressions.removeAll(Arrays.asList(selection));
        updateInput();
    }

    private void validate() {
        final String oldMsg = errorMsg;
        controlDecorationDelegate.clearDecorations();
        for (final RegularExpression e : expressions) {
            errorMsg = e.validate();
            if (errorMsg != null) {
                controlDecorationDelegate.setDecoratorText(viewer.getTable(), errorMsg);
                break;
            }

        }
        propertyChangeSupport.firePropertyChange(validationState, oldMsg, errorMsg);
    }

    public void setSelection(final RegularExpression... expressions) {
        viewer.setSelection(new StructuredSelection(expressions));
    }
}
