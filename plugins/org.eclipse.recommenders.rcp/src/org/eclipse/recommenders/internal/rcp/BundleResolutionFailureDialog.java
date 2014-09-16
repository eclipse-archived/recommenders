/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

import static org.eclipse.recommenders.internal.rcp.LogMessages.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.rcp.utils.BrowserUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * Gives the user the option to restart the workbench with the -clean flag
 *
 * Code partially taken from {@link org.eclipse.ui.internal.ide.actions.OpenWorkspaceAction}
 */
public class BundleResolutionFailureDialog extends MessageDialogWithToggle {

    private static final String RECOMMENDERS_FAQ_URL = "http://www.eclipse.org/recommenders/faq/"; //$NON-NLS-1$
    private static final String BUGZILLA_URL = "https://bugs.eclipse.org/bugs/enter_bug.cgi?product=Recommenders&version={0}&bug_severity=enhancement&short_desc=Your%20short%20description&comment=A%20longer%20description%0D%0DUnresolved bundles:%0D{1}&component=Core&rep_platform=All"; //$NON-NLS-1$

    private static final String PROP_VM = "eclipse.vm"; //$NON-NLS-1$
    private static final String PROP_VMARGS = "eclipse.vmargs"; //$NON-NLS-1$
    private static final String PROP_COMMANDS = "eclipse.commands"; //$NON-NLS-1$
    private static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$
    private static final String PROP_EXIT_DATA = "eclipse.exitdata"; //$NON-NLS-1$
    private static final String CMD_VMARGS = "-vmargs"; //$NON-NLS-1$
    private static final String NEW_LINE = "\n"; //$NON-NLS-1$

    private final Version recommendersVersion;
    private final Collection<Bundle> unresolvedBundles;

    public BundleResolutionFailureDialog(Shell parentShell, Version recommendersVersion,
            Collection<Bundle> unresolvedBundles) {
        super(parentShell, Messages.DIALOG_TITLE_BUNDLE_RESOLUTION_FAILURE, null,
                Messages.DIALOG_MESSAGE_BUNDLE_RESOLUTION_FAILURE, MessageDialog.ERROR, new String[] {
                        IDialogConstants.CANCEL_LABEL, Messages.DIALOG_BUTTON_RESTART }, 1,
                Messages.DIALOG_TOGGLE_IGNORE_BUNDLE_RESOLUTION_FAILURES, false);
        this.recommendersVersion = recommendersVersion;
        this.unresolvedBundles = unresolvedBundles;
        setPrefStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_ID));
        setPrefKey(Constants.PREF_IGNORE_BUNDLE_RESOLUTION_FAILURE);
    }

    @Override
    protected Control createCustomArea(Composite parent) {
        Label bundleListLabel = new Label(parent, SWT.NONE);
        bundleListLabel.setText(Messages.DIALOG_LABEL_BUNDLE_LIST);

        List bundleList = new List(parent, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
        for (Bundle bundle : unresolvedBundles) {
            bundleList.add(bundle.getSymbolicName());
        }
        GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 150).applyTo(bundleList);

        addLink(parent, Messages.DIALOG_MESSAGE_BUNDLE_RESOLUTION_FAQ, RECOMMENDERS_FAQ_URL);

        Collection<String> unresolvedBundleNames = Collections2.transform(unresolvedBundles,
                new Function<Bundle, String>() {

                    @Override
                    public String apply(Bundle input) {
                        return input.getSymbolicName();
                    }
                });
        String version = recommendersVersion.getMajor() + "." + recommendersVersion.getMinor() + "." //$NON-NLS-1$ //$NON-NLS-2$
                + recommendersVersion.getMicro();
        String bugLinkUrl = MessageFormat.format(BUGZILLA_URL, version, StringUtils.join(unresolvedBundleNames, '\n'));
        addLink(parent, Messages.DIALOG_MESSAGE_BUNDLE_RESOLUTION_FILE_A_BUG, bugLinkUrl);

        return parent;
    }

    private void addLink(Composite parent, String text, String url) {
        Link link = new Link(parent, SWT.BEGINNING);
        link.setText(MessageFormat.format(text, url));
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                BrowserUtils.openInExternalBrowser(event.text);
            }
        });
    }

    @Override
    protected void buttonPressed(int buttonId) {
        setReturnCode(buttonId);
        close();
        if (getToggleState() && getPrefStore() != null && getPrefKey() != null) {
            getPrefStore().setValue(getPrefKey(), ALWAYS);
            try {
                ((ScopedPreferenceStore) getPrefStore()).save();
            } catch (IOException e) {
                log(PREFERENCES_NOT_SAVED, e);
            }
        }
        if (buttonId == IDialogConstants.INTERNAL_ID) {
            String commandLine = buildCommandLine();
            if (commandLine == null) {
                return;
            }
            System.setProperty(PROP_EXIT_CODE, Integer.toString(24));
            System.setProperty(PROP_EXIT_DATA, buildCommandLine());
            PlatformUI.getWorkbench().restart();
        }
    }

    public boolean isIgnored() {
        return getPrefStore().getString(getPrefKey()).equals(MessageDialogWithToggle.ALWAYS);
    }

    /**
     * Create and return a string with command line options for eclipse that will launch a new workbench that is the
     * same as the currently running one, but adding the -clean flag.
     *
     * @param workspace
     *            the directory to use as the new workspace
     * @return a string of command line options or null on error
     */
    private String buildCommandLine() {
        String property = System.getProperty(PROP_VM);
        if (property == null) {
            log(RESTART_ECLIPSE_NOT_POSSIBLE);
            return null;
        }

        StringBuffer result = new StringBuffer(512);
        result.append(property);
        result.append(NEW_LINE);

        // append the vmargs and commands. Assume that these already end in \n
        String vmargs = System.getProperty(PROP_VMARGS);
        if (vmargs != null) {
            result.append(vmargs);
        }

        result.append("-clean"); //$NON-NLS-1$
        result.append(NEW_LINE);

        // append the rest of the args, replacing or adding -data as required
        property = System.getProperty(PROP_COMMANDS);
        result.append(property);
        result.append(NEW_LINE);

        // put the vmargs back at the very end (the eclipse.commands property
        // already contains the -vm arg)
        if (vmargs != null) {
            result.append(CMD_VMARGS);
            result.append(NEW_LINE);
            result.append(vmargs);
        }
        return result.toString();
    }
}
