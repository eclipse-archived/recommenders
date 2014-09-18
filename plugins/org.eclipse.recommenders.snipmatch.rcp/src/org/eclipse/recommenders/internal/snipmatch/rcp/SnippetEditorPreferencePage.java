/**
 * Copyright (c) 2013 Stefan Prisca.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Prisca - initial API and implementation
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class SnippetEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    @Override
    public void init(IWorkbench workbench) {
        ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_ID);
        setPreferenceStore(store);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite contents = new Composite(parent, SWT.None);
        contents.setLayout(new GridLayout());

        Link discoverLink = new Link(contents, SWT.NONE);
        discoverLink.setText(Messages.PREFPAGE_EDITOR_EXTENSIONS_LINK);
        discoverLink.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SnippetEditorDiscoveryUtils.openDiscoveryDialog();
            }
        });
        return contents;
    }
}
