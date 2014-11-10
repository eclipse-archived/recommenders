/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.recommenders.utils.Urls;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.Sets;

public final class Dialogs {

    private Dialogs() {
    }

    public static InputDialog newModelRepositoryUrlDialog(Shell parent, final String[] remoteUrls) {
        return new InputDialog(parent, Messages.DIALOG_TITLE_ADD_MODEL_REPOSITORY, Messages.FIELD_LABEL_REPOSITORY_URI,
                "http://download.eclipse.org/recommenders/models/<version>", //$NON-NLS-1$
                new IInputValidator() {

                    @Override
                    public String isValid(String newText) {
                        if (isURIAlreadyAdded(newText)) {
                            return Messages.DIALOG_MESSAGE_URI_ALREADY_ADDED;
                        }
                        URI uri = Urls.parseURI(newText).orNull();
                        if (uri == null) {
                            return Messages.DIALOG_MESSAGE_INVALID_URI;
                        }
                        if (!uri.isAbsolute()) {
                            return Messages.DIALOG_MESSAGE_NOT_ABSOLUTE_URI;
                        }
                        if (!Urls.isUriProtocolSupported(uri, "file", "http", "https")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            return MessageFormat.format(Messages.DIALOG_MESSAGE_UNSUPPORTED_PROTOCOL, uri.getScheme(),
                                    "file, http, https"); //$NON-NLS-1
                        }
                        return null;
                    }

                    private boolean isURIAlreadyAdded(String newText) {
                        Set<String> items = Sets.newHashSet(remoteUrls);
                        if (items.contains(newText)) {
                            return true;
                        }
                        return false;
                    }
                });
    }
}
