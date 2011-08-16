/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.preferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ClasspathDependencyStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.RemoteResolverJobFactory;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.UpdateAllModelsJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class CommandSection {

    private final ClasspathDependencyStore dependencyStore;
    private Button reresolveButton;
    private final RemoteResolverJobFactory jobFactory;

    public CommandSection(final Composite parent, final ClasspathDependencyStore dependencyStore,
            final RemoteResolverJobFactory jobFactory) {
        this.dependencyStore = dependencyStore;
        this.jobFactory = jobFactory;

        final Composite group = createGroup(parent);
        createButtons(group);
    }

    private Composite createGroup(final Composite parent) {
        final Composite section = new Composite(parent, SWT.NONE);
        section.setLayout(new GridLayout(2, false));
        section.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.END).create());
        return section;
    }

    private void createButtons(final Composite group) {
        final Composite container = new Composite(group, SWT.NONE);
        container.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).align(GridData.END, GridData.BEGINNING)
                .create());
        container.setLayout(new RowLayout());

        reresolveButton = createButton(container, "Update all models", loadImage("/icons/obj16/refresh.gif"));
    }

    private Image loadImage(final String name) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.recommenders.rcp.codecompletion.calls", name)
                .createImage();
    }

    private SelectionListener createSelectionListener() {
        return new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                reresolveButton.setEnabled(false);
                final UpdateAllModelsJob job = new UpdateAllModelsJob(dependencyStore, jobFactory);
                job.schedule();
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        };
    }

    private Button createButton(final Composite container, final String text, final Image image) {
        final Button button = new Button(container, SWT.PUSH);
        button.setImage(image);
        button.setText(text);
        button.addSelectionListener(createSelectionListener());
        return button;
    }
}
