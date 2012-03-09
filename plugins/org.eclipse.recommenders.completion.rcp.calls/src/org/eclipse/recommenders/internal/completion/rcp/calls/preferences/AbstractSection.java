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
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.google.common.collect.Lists;

public abstract class AbstractSection {

    private final List<Text> texts = Lists.newLinkedList();
    private final List<Button> buttons = Lists.newLinkedList();
    private final PreferencePage preferencePage;
    protected File file;

    public AbstractSection(final PreferencePage preferencePage, final Composite parent, final String title) {
        this.preferencePage = preferencePage;
        final Composite group = createGroup(parent, title);
        createDetailsContainer(group);
        createButtonContainer(group);
    }

    private Composite createGroup(final Composite parent, final String title) {
        final Group section = new Group(parent, SWT.NONE);
        section.setText(title);
        section.setLayout(new GridLayout(2, false));
        section.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        return section;
    }

    protected abstract void createDetailsContainer(final Composite group);

    private void createButtonContainer(final Composite parent) {
        final Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).align(GridData.END, GridData.BEGINNING)
                .create());
        container.setLayout(new RowLayout());
        createButtons(container);
    }

    protected abstract void createButtons(final Composite parent);

    protected Label createLabel(final Composite parent, final String text) {
        final Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        return label;
    }

    protected Text createText(final Composite parent, final int style) {
        final Text text = new Text(parent, style);
        text.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(150, SWT.DEFAULT)
                .align(GridData.FILL, GridData.BEGINNING).create());
        texts.add(text);
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                validate(preferencePage);
            }
        });
        return text;
    }

    protected void validate(final PreferencePage preferencePage) {
        preferencePage.setErrorMessage(null);
    }

    protected Button createButton(final Composite container, final Image image,
            final SelectionListener selectionListener) {
        final Button button = createButton(container, image);
        button.addSelectionListener(selectionListener);
        return button;
    }

    protected Button createButton(final Composite container, final Image image) {
        final Button button = new Button(container, SWT.PUSH);
        button.setEnabled(false);
        button.setImage(image);
        buttons.add(button);
        return button;
    }

    protected Image loadImage(final String name) {
        final ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, name);
        return desc.createImage();
    }

    protected Image loadSharedImage(final String imageName) {
        return PlatformUI.getWorkbench().getSharedImages().getImage(imageName);
    }

    protected void resetTexts() {
        for (final Text text : texts) {
            text.setText("");
        }
    }

    protected void setButtonsEnabled(final boolean enabled) {
        for (final Button button : buttons) {
            button.setEnabled(enabled);
        }
    }
}
