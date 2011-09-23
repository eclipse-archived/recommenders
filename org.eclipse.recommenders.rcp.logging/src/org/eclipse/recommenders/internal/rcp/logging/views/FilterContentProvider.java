/**
/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.logging.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.recommenders.internal.rcp.logging.model.EventFilter;
import org.eclipse.swt.graphics.Image;

import ch.qos.logback.classic.boolex.JaninoEventEvaluator;
import ch.qos.logback.core.filter.EvaluatorFilter;

public class FilterContentProvider extends LabelProvider implements ITableLabelProvider, IStructuredContentProvider {

    private static FilterContentProvider provider = new FilterContentProvider();

    private TableViewer viewer;

    public static FilterContentProvider getProvider() {
        return provider;
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        return EventFilter.getAllFilters().toArray();
    }

    public void setViewer(final TableViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    }

    @Override
    public Image getColumnImage(final Object element, final int columnIndex) {
        return null;
    }

    @Override
    public String getColumnText(final Object element, final int columnIndex) {
        final EvaluatorFilter filter = (EvaluatorFilter) element;
        final JaninoEventEvaluator eval = (JaninoEventEvaluator) filter.getEvaluator();
        switch (columnIndex) {
        case 0:
            return eval.getExpression();
        case 1:
            return filter.getOnMatch().toString();
        case 2:
            return filter.getOnMismatch().toString();
        }

        return null;
    }

    public void remove(final EvaluatorFilter filter) {
        EventFilter.remove(filter);
        viewer.remove(filter);
        viewer.refresh();
    }

    public EvaluatorFilter createNewFilter() {
        final EvaluatorFilter current = new EvaluatorFilter();
        current.setContext(EventFilter.getContext());
        current.setName("filtername");
        final JaninoEventEvaluator eval = new JaninoEventEvaluator();
        eval.setContext(EventFilter.getContext());
        eval.setName("evalfiltername");
        current.setEvaluator(eval);
        return current;
    }

}
