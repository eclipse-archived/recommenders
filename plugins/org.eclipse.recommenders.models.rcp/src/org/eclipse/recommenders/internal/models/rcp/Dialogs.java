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

import static org.eclipse.recommenders.internal.models.rcp.Messages.PREFPAGE_URI_ALREADY_ADDED;
import static org.eclipse.recommenders.internal.models.rcp.Messages.DIALOG_ENTER_REPOSITORY_URI;
import static org.eclipse.recommenders.internal.models.rcp.Messages.DIALOG_INVALID_REPOSITORY_URI;
import static org.eclipse.recommenders.internal.models.rcp.Messages.DIALOG_ADD_MODEL_REPOSITORY;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.Sets;

public final class Dialogs {

    private Dialogs() {
    }

    public static InputDialog newModelRepositoryUrlDialog(Shell parent, final String[] remoteUrls) {
        return new InputDialog(parent, DIALOG_ADD_MODEL_REPOSITORY, DIALOG_ENTER_REPOSITORY_URI,
                "http://download.eclipse.org/recommenders/models/<version>", //$NON-NLS-1$
                new IInputValidator() {

                    @Override
                    public String isValid(String newText) {
                        if (isURIAlreadyAdded(newText)) {
                            return PREFPAGE_URI_ALREADY_ADDED;
                        }
                        if (isInvalidRepoURI(newText)) {
                            return DIALOG_INVALID_REPOSITORY_URI;
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

                    private boolean isInvalidRepoURI(String uri) {
                        try {
                            new URI(uri);
                        } catch (URISyntaxException e) {
                            return true;
                        }
                        return false;
                    }
                });
    }
}
