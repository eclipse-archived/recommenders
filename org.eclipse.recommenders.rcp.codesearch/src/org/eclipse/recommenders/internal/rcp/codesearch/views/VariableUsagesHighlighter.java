/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.views;

import java.util.Iterator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.recommenders.commons.codesearch.Proposal;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.rcp.utils.ast.HeuristicUsedTypesAndMethodsLocationFinder;
import org.eclipse.recommenders.rcp.utils.ast.UsedTypesAndMethodsLocationFinder;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class VariableUsagesHighlighter implements ITextPresentationListener {
    Proposal proposal;
    SourceViewer sourceViewer;
    private final Request request;
    private final String searchData;

    public VariableUsagesHighlighter(final SourceViewer viewer, final Request request, final Proposal example,
            final String searchData) {
        this.request = request;
        this.proposal = example;
        this.sourceViewer = viewer;
        this.searchData = searchData;
    }

    // when the listener gets invoked for the first time
    // it receives a text presentation with all style ranges of the whole class
    // with every other invocation the listener gets a text presentation with
    // only the influenced
    // from the mouse click (or whatever invoked the listener) style ranges.
    @Override
    public void applyTextPresentation(final TextPresentation textPresentation) {
        final UsedTypesAndMethodsLocationFinder finder = UsedTypesAndMethodsLocationFinder.find(proposal.ast,
                request.usedTypes, request.calledMethods);
        final Color foreground = JavaUI.getColorManager().getColor(new RGB(255, 0, 0));
        final Color background = JavaUI.getColorManager().getColor(new RGB(255, 255, 128));
        final Color heuristic = JavaUI.getColorManager().getColor(new RGB(220, 245, 139));
        final IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
        resetAnnotations(annotationModel);
        for (final ASTNode node : finder.getTypeSimpleNames()) {
            setAnnotation(annotationModel, node, "recommendation.type");
            setHighlightStyleForNode(textPresentation, foreground, background, node);
        }
        for (final ASTNode node : finder.getMethodSimpleNames()) {
            setAnnotation(annotationModel, node, "recommendation.type");
            setHighlightStyleForNode(textPresentation, foreground, background, node);
        }
        for (final ASTNode node : HeuristicUsedTypesAndMethodsLocationFinder.find(proposal.ast, request.usedTypes,
                request.calledMethods)) {
            setAnnotation(annotationModel, node, "recommendation.heuristic.type");
            setHighlightStyleForNode(textPresentation, null, heuristic, node);
        }
    }

    private void resetAnnotations(final IAnnotationModel annotationModel) {
        final Iterator<Annotation> iter = annotationModel.getAnnotationIterator();
        while (iter.hasNext()) {
            final Annotation a = iter.next();
            annotationModel.removeAnnotation(a);
        }
    }

    private void setAnnotation(final IAnnotationModel model, final ASTNode node, final String type) {
        if (model != null) {
            if (searchData.isEmpty()) {
                final Annotation annotation = new Annotation(type, false, node.getParent().toString());
                final Position position = new Position(node.getStartPosition(), node.getLength());
                model.addAnnotation(annotation, position);
            } else if (node.toString().toLowerCase().contains(searchData.toLowerCase())) {
                final Annotation annotation = new Annotation(type, false, node.getParent().toString());
                final Position position = new Position(node.getStartPosition(), node.getLength());
                model.addAnnotation(annotation, position);
            }
        }
    }

    private void setHighlightStyleForNode(final TextPresentation textPresentation, final Color foreground,
            final Color background, final ASTNode node) {
        final int start = node.getStartPosition();
        final int length = node.getLength();
        calculateRanges(textPresentation, start, length, foreground, background);
    }

    private void calculateRanges(final TextPresentation textPresentation, final int start, final int length,
            final Color foreground, final Color background) {
        final Iterator<StyleRange> srIterator = textPresentation.getAllStyleRangeIterator();
        while (srIterator.hasNext()) {
            final StyleRange current = srIterator.next();
            if (current.start > start && current.start + current.length > start + length
                    && current.start < start + length) {
                textPresentation.mergeStyleRange(new StyleRange(current.start, start + length - current.start,
                        foreground, background));
            } else if (current.start >= start && current.start + current.length <= start + length) {
                current.background = background;
                current.foreground = foreground;
            } else if (current.start < start && current.start + current.length > start + length) {
                textPresentation.mergeStyleRange(new StyleRange(start, length, foreground, background));
            } else if (current.start < start && current.length + current.start < start + length
                    && current.start + current.length > start) {
                textPresentation.mergeStyleRange(new StyleRange(start, start + length - current.start + current.length,
                        foreground, background));
            }
        }
    }
}
