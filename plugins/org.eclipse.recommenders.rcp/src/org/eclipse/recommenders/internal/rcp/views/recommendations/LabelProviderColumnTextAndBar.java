/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.views.recommendations;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.recommenders.rcp.IRecommendation;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * We have to override <code>paint</code>. Therefore we have to extend
 * StyledCellLabelProvider and not just CellLabelProvider.
 */
class LabelProviderColumnTextAndBar extends StyledCellLabelProvider {

    @Override
    public void update(final ViewerCell cell) {
        final Object obj = cell.getElement();
        if (obj instanceof IRecommendation) {
            setTextForRecommendationProbability(cell, (IRecommendation) obj);
        } else {
            cell.setText("");
        }
    }

    private void setTextForRecommendationProbability(final ViewerCell cell, final IRecommendation rec) {
        final String text = String.format("%3.0f", rec.getProbability() * 100) + " %";
        cell.setText(text);
    }

    @Override
    protected void paint(final Event event, final Object element) {
        if (element instanceof IRecommendation) {
            paintLikelihoodBarForRecommendation((IRecommendation) element, event);
        }
        // The "super" call does not only "paint" stuff, but also writes the
        // text.
        // If the "super" class is made before we paint the bars, the bars get
        // painted over the
        // text.
        // Therefore: First, paint the bars, then call the "super" paint.
        super.paint(event, element);
    }

    private void paintLikelihoodBarForRecommendation(final IRecommendation recommendation, final Event event) {
        final double likelihood = recommendation.getProbability();
        final TreeItem item = (TreeItem) event.item;
        final Display display = item.getDisplay();
        final ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
        final Color c2 = colorRegistry.getColorDescriptor("org.eclipse.ui.workbench.ACTIVE_TAB_BG_END").createColor(
                display);
        final Color c1 = colorRegistry.getColorDescriptor("org.eclipse.ui.workbench.ACTIVE_TAB_BG_START").createColor(
                display);
        final GC gc = event.gc;
        gc.setForeground(c1);
        gc.setBackground(c2);
        //
        final Tree tree = (Tree) event.widget;
        final int barWidth = (int) (tree.getColumn(event.index).getWidth() * likelihood);
        final int y = event.y + 1;
        final int x = event.x + 1;
        final int height = event.height - 2;
        gc.fillGradientRectangle(x, y, barWidth, height, true);
        final Rectangle rect2 = new Rectangle(x, y, barWidth - 1, height - 1);
        gc.setForeground(c2);
        gc.drawRectangle(rect2);
    }
}
