/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon Laffoy - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.tips;

import static java.util.Objects.requireNonNull;
import static org.eclipse.jdt.internal.ui.JavaPlugin.getActiveWorkbenchShell;
import static org.eclipse.ui.dialogs.PreferencesUtil.createPreferenceDialogOn;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessorDescriptor;
import org.eclipse.recommenders.internal.completion.rcp.CompletionRcpPreferences;
import org.eclipse.recommenders.rcp.utils.BrowserUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.ImmutableList;

public class ConfigureContentAssistInformationControl extends AbstractInformationControl {

    private static final List<SessionProcessorDescriptor> NONE = Collections.<SessionProcessorDescriptor>emptyList();

    private static final String HTTP = "http:"; //$NON-NLS-1$
    private static final String HTTPS = "https:"; //$NON-NLS-1$

    public static final String X_PREFERENCES = "X-preferences:"; //$NON-NLS-1$
    public static final String X_SESSION_PROCESSOR = "X-sessionProcessor:"; //$NON-NLS-1$

    public static final char SWITCH_ON = '+';
    public static final char SWITCH_OFF = '-';

    private final String info;
    private final CompletionRcpPreferences preferences;
    private final String statusLineText;

    public ConfigureContentAssistInformationControl(Shell parent, String statusLineText, String info,
            CompletionRcpPreferences preferences) {
        super(parent, statusLineText);
        this.info = requireNonNull(info);
        this.preferences = preferences;
        this.statusLineText = statusLineText;
        create();
    }

    @Override
    public boolean hasContents() {
        return true;
    }

    @Override
    public IInformationControlCreator getInformationPresenterControlCreator() {
        return new IInformationControlCreator() {

            @Override
            public IInformationControl createInformationControl(Shell parent) {
                return new ConfigureContentAssistInformationControl(parent, statusLineText, info, preferences);
            }
        };
    }

    @Override
    protected void createContent(Composite parent) {
        Link link = new Link(parent, SWT.NONE);
        Dialog.applyDialogFont(link);
        link.setForeground(parent.getForeground());
        link.setBackground(parent.getBackground());
        link.setText(info);
        link.addSelectionListener(new SelectionAdapter() {

            @SuppressWarnings("restriction")
            @Override
            public void widgetSelected(SelectionEvent e) {
                dispose();
                String url = e.text;
                if (StringUtils.startsWith(url, HTTP) || StringUtils.startsWith(url, HTTPS)) {
                    BrowserUtils.openInExternalBrowser(url);
                } else if (StringUtils.startsWith(url, X_PREFERENCES)) {
                    createPreferenceDialogOn(getActiveWorkbenchShell(), StringUtils.substringAfter(url, X_PREFERENCES),
                            null, null).open();
                } else if (preferences != null && StringUtils.startsWith(url, X_SESSION_PROCESSOR)) {
                    String sessionProcessorIdWithSwitch = StringUtils.substringAfter(url, X_SESSION_PROCESSOR);
                    char processorSwitch = sessionProcessorIdWithSwitch.charAt(0);
                    String sessionProcessorId = StringUtils.substring(sessionProcessorIdWithSwitch, 1);
                    SessionProcessorDescriptor descriptor = preferences
                            .getSessionProcessorDescriptor(sessionProcessorId);
                    if (descriptor != null) {
                        if (processorSwitch == SWITCH_ON) {
                            preferences.setSessionProcessorEnabled(ImmutableList.of(descriptor), NONE);
                        } else if (processorSwitch == SWITCH_OFF) {
                            preferences.setSessionProcessorEnabled(NONE, ImmutableList.of(descriptor));
                        }
                    }
                }
            }
        });
    }
}
