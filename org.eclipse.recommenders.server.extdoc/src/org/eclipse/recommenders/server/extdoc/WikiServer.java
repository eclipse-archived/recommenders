/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.server.extdoc;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.internal.server.extdoc.AbstractFeedbackServer;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.server.extdoc.types.WikiEntry;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.sun.jersey.api.client.GenericType;

public final class WikiServer extends AbstractFeedbackServer {

    private static final String PROVIDER_ID = WikiEntry.class.getSimpleName();

    @Inject
    public WikiServer(final ICouchDbServer server, final UsernamePreferenceListener usernameListener,
            final JavaElementResolver resolver) {
        super(server, usernameListener, resolver);
    }

    public String getText(final IJavaElement javaElement) {
        final WikiEntry entry = getEntry(javaElement);
        return entry == null ? null : entry.getText();
    }

    public void setText(final IJavaElement javaElement, final String text) {
        WikiEntry entry = getEntry(javaElement);
        if (entry == null) {
            entry = WikiEntry.create(getIdentifier(javaElement), text);
        } else {
            entry.setText(text);
        }
        getServer().post(entry);
    }

    private WikiEntry getEntry(final IJavaElement javaElement) {
        final String key = getIdentifier(javaElement);
        final List<WikiEntry> entries = getServer().getRows("providers",
                ImmutableMap.of("providerId", PROVIDER_ID, "type", key),
                new GenericType<GenericResultObjectView<WikiEntry>>() {
                });
        return entries == null || entries.isEmpty() ? null : entries.get(0);
    }

    private static String getIdentifier(final IJavaElement javaElement) {
        return javaElement.getHandleIdentifier().replaceAll(".*<", "").replaceAll("[{\\[]", ".");
    }

}
