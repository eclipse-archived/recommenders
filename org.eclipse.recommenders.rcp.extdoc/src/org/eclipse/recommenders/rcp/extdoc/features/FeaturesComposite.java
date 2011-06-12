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

import com.google.common.base.Preconditions;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public final class FeaturesComposite {

    private static Image commentsIcon = ExtDocPlugin.getIcon("eview16/comments.png");
    private static Image editIcon = ExtDocPlugin.getIcon("eview16/edit.png");
    private static Image deleteIcon = ExtDocPlugin.getIcon("eview16/delete.png");

    private final Composite composite;

    public FeaturesComposite(final Composite parent) {
        composite = SwtFactory.createRowComposite(parent, 3, 0, 0);
    }

    public static FeaturesComposite create(final Composite parent, final Object object, final String objectName,
            final IProvider provider, final IStarsRatingsServer server, final Dialog editDialog) {
        final FeaturesComposite features = new FeaturesComposite(parent);
        features.addCommentsIcon(object, objectName, provider);
        features.addEditIcon(editDialog);
        if (provider instanceof IDeletionProvider) {
            features.addDeleteIcon(object, objectName, (IDeletionProvider) provider);
        }
        features.addStarsRating(object, server);
        return features;
    }

    public void addCommentsIcon(final Object object, final String objectName, final IProvider provider) {
        final CommentsDialog commentsDialog = new CommentsDialog(provider.getShell(), null, provider, object,
                objectName);
        createIcon(commentsIcon, commentsDialog);
    }

    public void addEditIcon(final Dialog editDialog) {
        createIcon(editIcon, editDialog);
    }

    public void addDeleteIcon(final Object object, final String objectName, final IDeletionProvider provider) {
        createIcon(deleteIcon, new DeleteDialog(provider, object, objectName));
    }

    public void addStarsRating(final Object object, final IStarsRatingsServer server) {
        new StarsRatingComposite(composite, object, server);
    }

    private void createIcon(final Image icon, final Dialog dialog) {
        final Label label = new Label(composite, SWT.NONE);
        label.setImage(icon);
        final DialogListener listener = new DialogListener(dialog);
        label.addMouseListener(listener);
        label.addKeyListener(listener);
    }

    public void dispose() {
        composite.dispose();
    }

    private static final class DialogListener implements MouseListener, KeyListener {

        private final Dialog dialog;

        public DialogListener(final Dialog dialog) {
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
