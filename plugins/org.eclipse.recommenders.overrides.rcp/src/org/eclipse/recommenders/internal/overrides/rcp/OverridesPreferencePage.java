/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.overrides.rcp;

import org.eclipse.recommenders.completion.rcp.AbstractCompletionPreferencePage;
import org.eclipse.recommenders.internal.overrides.rcp.l10n.Messages;

public class OverridesPreferencePage extends AbstractCompletionPreferencePage {

    public OverridesPreferencePage() {
        super(Constants.BUNDLE_NAME, Messages.PREFPAGE_DESCRIPTION_OVERRIDES);
    }
}
