/**
 * Copyright (c) 2016 Yasett Acurana.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yasett Acurana - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp.util;

import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportProtocol;
import org.eclipse.jgit.transport.TransportProtocol.URIishField;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.StringUtils;
import org.eclipse.recommenders.internal.snipmatch.rcp.Constants;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.Messages;

public class RepositoryUrlValidator {

    public static IStatus isValidUri(String repositoryUri) {
        URIish urish;
        try {
            urish = new URIish(repositoryUri);
        } catch (URISyntaxException e) {
            return new Status(IStatus.ERROR, Constants.BUNDLE_ID, e.getLocalizedMessage(), e);
        }

        if (urish.getScheme() == null) {
            return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                    MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_ABSOLUTE_URL_REQUIRED, repositoryUri));
        }

        boolean isSupportedScheme = false;

        for (TransportProtocol protocol : Transport.getTransportProtocols()) {
            if (protocol.getSchemes().contains(urish.getScheme())) {

                for (URIishField requieredField : protocol.getRequiredFields()) {
                    switch (requieredField) {
                    case USER:
                        if (StringUtils.isEmptyOrNull(urish.getUser())) {
                            return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                                    Messages.WIZARD_GIT_REPOSITORY_ERROR_MISSING_COMPONENT_USER);
                        }
                        break;
                    case PASS:
                        if (StringUtils.isEmptyOrNull(urish.getPass())) {
                            return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                                    Messages.WIZARD_GIT_REPOSITORY_ERROR_MISSING_COMPONENT_PASSWORD);
                        }
                        break;
                    case HOST:
                        if (StringUtils.isEmptyOrNull(urish.getHost())) {
                            return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                                    Messages.WIZARD_GIT_REPOSITORY_ERROR_MISSING_COMPONENT_HOST);
                        }
                        break;
                    case PATH:
                        if (StringUtils.isEmptyOrNull(urish.getPath())) {
                            return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                                    Messages.WIZARD_GIT_REPOSITORY_ERROR_MISSING_COMPONENT_PATH);
                        }
                        break;
                    case PORT:
                        if (urish.getPort() <= 0) {
                            return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                                    Messages.WIZARD_GIT_REPOSITORY_ERROR_MISSING_COMPONENT_PORT);
                        }
                        break;
                    }
                }

                EnumSet<URIishField> validFields = EnumSet.copyOf(protocol.getRequiredFields());
                validFields.addAll(protocol.getOptionalFields());

                for (URIishField invalidField : EnumSet.complementOf(validFields)) {
                    switch (invalidField) {
                    case HOST:
                        if (!StringUtils.isEmptyOrNull(urish.getHost())) {
                            return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                                    Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_COMPONENT_HOST);
                        }
                        break;
                    case PATH:
                        if (!StringUtils.isEmptyOrNull(urish.getPath())) {
                            return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                                    Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_COMPONENT_PATH);
                        }
                        break;
                    case USER:
                        if (!StringUtils.isEmptyOrNull(urish.getUser())) {
                            return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                                    Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_COMPONENT_USER);
                        }
                        break;
                    case PASS:
                        if (!StringUtils.isEmptyOrNull(urish.getPass())) {
                            return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                                    Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_COMPONENT_PASSWORD);
                        }
                        break;
                    case PORT:
                        if (urish.getPort() >= 0) {
                            return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                                    Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_COMPONENT_PORT);
                        }
                        break;
                    }
                }

                isSupportedScheme = true;
                break;
            }
        }

        if (!isSupportedScheme) {
            return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                    MessageFormat.format(Messages.WIZARD_GIT_REPOSITORY_ERROR_URL_PROTOCOL_UNSUPPORTED,
                            urish.getScheme(), StringUtils.join(getSupportedSchemes(), Messages.LIST_SEPARATOR)));
        } else {
            return Status.OK_STATUS;
        }
    }

    private static SortedSet<String> getSupportedSchemes() {
        SortedSet<String> supportedSchemes = new TreeSet<String>();
        for (TransportProtocol protocol : Transport.getTransportProtocols()) {
            supportedSchemes.addAll(protocol.getSchemes());
        }
        return supportedSchemes;
    }
}
