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
package org.eclipse.recommenders.internal.completion.rcp.calls.preferences;

import static org.eclipse.recommenders.internal.completion.rcp.calls.wiring.CallsCompletionPlugin.PLUGIN_ID;

import java.io.File;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.commons.udc.DependencyInformation;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.CallModelStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ManifestResolutionRequested;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath.DependencyInfoStore;
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

    private final CallModelStore modelStore;
    private Button reresolveButton;
    private final DependencyInfoStore depStore;

    public CommandSection(final Composite parent, final CallModelStore modelStore) {
        this.modelStore = modelStore;
        this.depStore = modelStore.getDependencyInfoStore();

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

    protected Image loadImage(final String name) {
        final ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, name);
        return desc.createImage();
    }

    private SelectionListener createSelectionListener() {
        return new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                reresolveButton.setEnabled(false);
                for (final File f : modelStore.getDependencyInfoStore().getFiles()) {
                    final DependencyInformation info = depStore.getDependencyInfo(f).get();
                    modelStore.getManifestResolverService().onEvent(new ManifestResolutionRequested(info));
                }
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
