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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.recommenders.internal.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IDeletionProvider;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.google.common.base.Preconditions;

public final class FeaturesComposite {

    private static final Image ICON_COMMENTS = ExtDocPlugin.getIcon("eview16/comments.png");
    private static final Image ICON_EDIT = ExtDocPlugin.getIcon("eview16/edit.png");
    private static final Image ICON_DELETE = ExtDocPlugin.getIcon("eview16/delete.png");

    private final Composite composite;

    public FeaturesComposite(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 4, 3, 0, 0, 0);
        composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
    }

    public static FeaturesComposite create(final Composite parent, final Object object, final String objectName,
            final IProvider provider, final IStarsRatingsServer server, final Dialog editDialog) {
        final FeaturesComposite features = new FeaturesComposite(parent);
        if (server instanceof ICommentsServer) {
            features.addCommentsIcon(object, objectName, (ICommentsServer) server, provider.getShell());
        }
        features.addEditIcon(editDialog);
        if (provider instanceof IDeletionProvider) {
            features.addDeleteIcon(object, objectName, (IDeletionProvider) provider);
        }
        features.addStarsRating(object, server);
        return features;
    }

    private void addCommentsIcon(final Object object, final String objectName, final ICommentsServer server,
            final Shell shell) {
        final CommentsDialog commentsDialog = new CommentsDialog(shell, server, object, objectName);
        addIcon(ICON_COMMENTS, commentsDialog);
    }

    public void addEditIcon(final Dialog editDialog) {
        addIcon(ICON_EDIT, editDialog);
    }

    private void addDeleteIcon(final Object object, final String objectName, final IDeletionProvider provider) {
        addIcon(ICON_DELETE, new DeleteDialog(provider, object, objectName));
    }

    private void addStarsRating(final Object object, final IStarsRatingsServer server) {
        new StarsRatingComposite(composite, object, server);
    }

    private void addIcon(final Image icon, final Dialog dialog) {
        final Label label = new Label(composite, SWT.NONE);
        label.setImage(icon);
        final DialogListener listener = new DialogListener(dialog);
        label.addMouseListener(listener);
        label.addKeyListener(listener);
    }

    private static final class DialogListener implements MouseListener, KeyListener {

        private final Dialog dialog;

        private DialogListener(final Dialog dialog) {
            this.dialog = Preconditions.checkNotNull(dialog);
        }

        @Override
        public void keyPressed(final KeyEvent e) {
        }

        @Override
        public void keyReleased(final KeyEvent e) {
            dialog.open();
        }

        @Override
        public void mouseDoubleClick(final MouseEvent e) {
        }

        @Override
        public void mouseDown(final MouseEvent e) {
        }

        @Override
        public void mouseUp(final MouseEvent e) {
            dialog.open();
        }

    }

}
