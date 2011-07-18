/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.extdoc.features;

import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Preconditions;

public final class CommentsComposite {

    private static Image commentsIcon = ExtDocPlugin.getIcon("eview16/comments.png");
    private static DateFormat dateFormat = DateFormat.getDateInstance();

    private IJavaElement element;
    private IProvider provider;
    private IUserFeedbackServer server;
    private List<IComment> comments;

    private Composite composite;

    /**
     * @wbp.parser.entryPoint
     */
    public static CommentsComposite create(final Composite parent, final IJavaElement element,
            final IProvider provider, final IUserFeedbackServer server) {
        final CommentsComposite composite = new CommentsComposite();

        composite.provider = provider;
        composite.server = Preconditions.checkNotNull(server);
        composite.element = element;
        composite.comments = new LinkedList<IComment>(server.getUserFeedback(element, provider).getComments());

        composite.createCommentsArea(parent);
        return composite;
    }

    private void createCommentsArea(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 5, 0, 0);

        SwtFactory.createLink(composite, "Show / Add Comments (" + comments.size() + ")", commentsIcon,
                new MouseListener() {
                    @Override
                    public void mouseUp(final MouseEvent e) {
                        displayComments();
                    }

                    @Override
                    public void mouseDown(final MouseEvent e) {
                    }

                    @Override
                    public void mouseDoubleClick(final MouseEvent e) {
                    }
                });
    }

    private void displayComments() {
        for (final Control child : composite.getChildren()) {
            child.dispose();
        }

        for (final IComment comment : comments) {
            final String headLine = String
                    .format("%s, %s", dateFormat.format(comment.getDate()), comment.getUsername());
            SwtFactory.createCLabel(composite, headLine, true, commentsIcon);
            SwtFactory.createLabel(composite, comment.getText());
        }

        displayAddComment();
        composite.layout(true);
        if (composite.getParent().getParent() != null) {
            composite.getParent().getParent().getParent().layout(true);
        }
    }

    private void displayAddComment() {
        SwtFactory.createLabel(composite, "");
        final Text text = SwtFactory.createText(composite, "", 45, 0);

        SwtFactory.createButton(composite, "Add Comment", new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                addComment(text.getText());
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        });
    }

    private void addComment(final String text) {
        final IComment comment = server.addComment(text, element, provider);
        comments.add(comment);
        displayComments();
    }
}
