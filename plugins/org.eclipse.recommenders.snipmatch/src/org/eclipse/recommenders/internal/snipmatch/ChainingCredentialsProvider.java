/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch;

import static com.google.common.base.Predicates.isNull;
import static com.google.common.collect.Iterables.any;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

/**
 * A replacement for JGits's {@link org.eclipse.jgit.transport.ChainingCredentialsProvider}, which only exists in JGit
 * 3.5 or newer (not included in Eclipse Luna SR2 or earlier).
 */
public class ChainingCredentialsProvider extends CredentialsProvider {

    private final List<CredentialsProvider> providers;

    public ChainingCredentialsProvider(CredentialsProvider... providers) {
        this.providers = new ArrayList<>(asList(providers));
    }

    @Override
    public boolean isInteractive() {
        for (CredentialsProvider provider : providers) {
            if (provider.isInteractive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean supports(CredentialItem... items) {
        for (CredentialsProvider provider : providers) {
            if (provider.supports(items)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean get(URIish uri, CredentialItem... items) {
        for (CredentialsProvider provider : providers) {
            if (provider.supports(items)) {
                if (!provider.get(uri, items)) {
                    if (provider.isInteractive()) {
                        return false;
                    }
                    continue;
                }
                if (any(asList(items), isNull())) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }
}
