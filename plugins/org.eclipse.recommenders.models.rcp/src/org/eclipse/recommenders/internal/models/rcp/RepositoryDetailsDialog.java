/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import java.net.URI;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.recommenders.internal.models.rcp.l10n.Messages;
import org.eclipse.recommenders.utils.Uris;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.ImmutableList;

public class RepositoryDetailsDialog extends TitleAreaDialog {

    private static final List<String> SUPPORTED_PROTOCOLS = ImmutableList.of("file", "http", "https"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private Text repositoryUrlText;
    private Text repositoryUsernameText;
    private Text repositoryPasswordText;

    private String repositoryUrl;
    private final List<String> remoteUris;

    private final ModelsRcpPreferences preferences;

    public RepositoryDetailsDialog(Shell parentShell, String repositoryUrl, List<String> remoteUris,
            ModelsRcpPreferences preferences) {
        super(parentShell);
        this.repositoryUrl = repositoryUrl;
        this.remoteUris = remoteUris;
        this.preferences = preferences;
        setHelpAvailable(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite fieldArea = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);

        final boolean noRepository = repositoryUrl == null;
        String title = noRepository ? Messages.DIALOG_TITLE_ADD_MODEL_REPOSITORY
                : Messages.DIALOG_TITLE_EDIT_MODEL_REPOSITORY;

        getShell().setText(title);
        setTitle(title);

        setMessage(noRepository ? Messages.DIALOG_MESSAGE_ENTER_REPOSITORY_DETAILS
                : Messages.DIALOG_MESSAGE_EDIT_REPOSITORY_DETAILS);

        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(fieldArea);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(fieldArea);

        repositoryUrlText = createLabelledText(fieldArea, Messages.DIALOG_LABEL_REPOSITORY_URL,
                noRepository ? "http://download.eclipse.org/recommenders/models/<version>" : repositoryUrl, SWT.NONE); //$NON-NLS-1$

        repositoryUsernameText = createLabelledText(fieldArea, Messages.DIALOG_LABEL_REPOSITORY_USERNAME,
                preferences.getServerUsername(repositoryUrl).or(""), SWT.NONE); //$NON-NLS-1$
        repositoryUsernameText.setMessage(Messages.DIALOG_HINT_OPTIONAL);

        repositoryPasswordText = createLabelledText(fieldArea, Messages.DIALOG_LABEL_REPOSITORY_PASSWORD,
                preferences.getServerPassword(repositoryUrl).or(""), SWT.PASSWORD); //$NON-NLS-1$

        repositoryPasswordText.setMessage(Messages.DIALOG_HINT_OPTIONAL);

        repositoryUrlText
                .addModifyListener(new ServerRepositoryUrlListener(repositoryUsernameText, repositoryPasswordText));

        Dialog.applyDialogFont(fieldArea);

        return fieldArea;
    }

    private Text createLabelledText(Composite parent, String labelText, String initialText, int additionalStyle) {
        Label label = new Label(parent, SWT.NONE);

        label.setText(labelText);
        GridDataFactory.swtDefaults().applyTo(label);

        Text text = new Text(parent, SWT.BORDER | additionalStyle);
        text.setText(initialText);
        text.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updatePageComplete();
            }
        });
        GridDataFactory.fillDefaults().grab(true, false).applyTo(text);

        return text;
    }

    private void updatePageComplete() {
        setErrorMessage(null);
        boolean hasError = false;

        try {
            URI uri = Uris.parseURI(repositoryUrlText.getText()).orNull();
            if (uri == null) {
                hasError = true;
                setErrorMessage(Messages.DIALOG_MESSAGE_INVALID_URI);
                return;
            }
            if (!uri.isAbsolute()) {
                hasError = true;
                setErrorMessage(Messages.DIALOG_MESSAGE_NOT_ABSOLUTE_URI);
                return;
            }
            if (isUriAlreadyAdded(uri)) {
                hasError = true;
                setErrorMessage(Messages.DIALOG_MESSAGE_URI_ALREADY_ADDED);
                return;
            }

            if (!Uris.isUriProtocolSupported(uri, SUPPORTED_PROTOCOLS)) {
                hasError = true;
                setErrorMessage(MessageFormat.format(Messages.DIALOG_MESSAGE_UNSUPPORTED_PROTOCOL, uri.getScheme(),
                        StringUtils.join(SUPPORTED_PROTOCOLS, Messages.LIST_SEPARATOR)));
                return;
            }

            if (!isUsernameValid()) {
                hasError = true;
                setErrorMessage(Messages.DIALOG_MESSAGE_PASSWORD_WITHOUT_USERNAME);
                return;
            }
        } finally {
            getButton(IDialogConstants.OK_ID).setEnabled(!hasError);
        }
    }

    private boolean isUriAlreadyAdded(URI uri) {
        if (repositoryUrl != null) {
            URI repositoryUri = Uris.parseURI(repositoryUrl).orNull();
            if (uri.equals(repositoryUri)) {
                return false;
            }
        }

        String mangledUri = Uris.mangle(uri);
        for (String remoteUri : remoteUris) {
            if (Uris.mangle(Uris.toUri(remoteUri)).equals(mangledUri)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUsernameValid() {
        return !repositoryUsernameText.getText().isEmpty() || repositoryPasswordText.getText().isEmpty();
    }

    @Override
    protected void okPressed() {
        repositoryUrl = repositoryUrlText.getText();

        preferences.setServerUsername(repositoryUrl, repositoryUsernameText.getText());
        preferences.setServerPassword(repositoryUrl, repositoryPasswordText.getText());

        super.okPressed();
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    private final class ServerRepositoryUrlListener implements ModifyListener {

        private final Text usernameField;
        private final Text passwordField;

        public ServerRepositoryUrlListener(Text usernameField, Text passwordField) {
            this.usernameField = usernameField;
            this.passwordField = passwordField;
        }

        @Override
        public void modifyText(ModifyEvent e) {
            Text urlField = (Text) e.widget;
            String serverUri = urlField.getText();
            String username = preferences.getServerUsername(serverUri).orNull();
            if (username == null) {
                return;
            }
            usernameField.setText(username);

            String password = preferences.getServerPassword(serverUri).or(""); //$NON-NLS-1$ // Can't set null password
            passwordField.setText(password);
        }
    }
}
