/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn, Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.text.MessageFormat.format;

import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.Messages;
import org.eclipse.recommenders.snipmatch.ISnippet;

public class SnippetProposals {

    private SnippetProposals() {
        // Not meant to be instantiated
    }

    public static String createDisplayString(ISnippet snippet) {
        if (isNullOrEmpty(snippet.getDescription())) {
            return snippet.getName();
        } else {
            return format(Messages.SEARCH_DISPLAY_STRING, snippet.getName(), snippet.getDescription());
        }
    }
}
