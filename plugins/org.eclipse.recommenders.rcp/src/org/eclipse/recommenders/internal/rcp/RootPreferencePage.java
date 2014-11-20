/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.rcp;

import java.text.MessageFormat;

import javax.inject.Inject;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.SharedImages.Images;
import org.eclipse.recommenders.rcp.utils.BrowserUtils;
import org.eclipse.recommenders.rcp.utils.Dialogs;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class RootPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private final SharedImages images;

    @Inject
    public RootPreferencePage(SharedImages images) {
        this.images = images;
    }

    @Override
    public void init(final IWorkbench workbench) {
        setDescription(Messages.PREFPAGE_DESCRIPTION_EMPTY);
    }

    @Override
    protected Control createContents(final Composite parent) {
        noDefaultAndApplyButton();
        Composite composite = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().applyTo(composite);
        GridLayoutFactory.swtDefaults().margins(0, 5).applyTo(composite);

        Group group = new Group(composite, SWT.NONE);
        group.setText("Useful links");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);

        BrowserUtils.addOpenBrowserAction(createLink(group, Images.OBJ_HOMEPAGE, Messages.PREFPAGE_LINK_HOMEPAGE,
                "http://www.eclipse.org/recommenders/")); //$NON-NLS-1$

        BrowserUtils.addOpenBrowserAction(createLink(group, Images.OBJ_CONTAINER, Messages.PREFPAGE_LINK_MANUAL,
                "http://www.eclipse.org/recommenders/manual/")); //$NON-NLS-1$

        BrowserUtils.addOpenBrowserAction(createLink(group, Images.OBJ_FAVORITE_STAR, Messages.PREFPAGE_LINK_FAVORITE,
                "http://marketplace.eclipse.org/content/eclipse-code-recommenders")); //$NON-NLS-1$

        BrowserUtils.addOpenBrowserAction(createLink(group, Images.OBJ_BIRD_BLUE, Messages.PREFPAGE_LINK_TWITTER,
                "http://twitter.com/recommenders")); //$NON-NLS-1$

        addOpenExtensionDiscoveryDialogAction(createLink(group, Images.OBJ_LIGHTBULB,
                Messages.PREFPAGE_LINK_EXTENSIONS, "")); //$NON-NLS-1$

        return parent;
    }

    private Link createLink(Composite content, Images icon, String urlLabel, String url) {
        Label label = new Label(content, SWT.BEGINNING);
        label.setImage(images.getImage(icon));

        Link link = new Link(content, SWT.BEGINNING);
        link.setText(MessageFormat.format(urlLabel, url));
        return link;
    }

    private void addOpenExtensionDiscoveryDialogAction(Link link) {
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Dialogs.newExtensionsDiscoveryDialog().open();
            }
        });
    }
}
