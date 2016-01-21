/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon Laffoy- initial API and implementation.
 */
package org.eclipse.recommenders.internal.constructors.rcp;

import org.eclipse.recommenders.completion.rcp.AbstractCompletionPreferencePage;
import org.eclipse.recommenders.internal.constructors.rcp.l10n.Messages;

public class ConstructorsPreferencePage extends AbstractCompletionPreferencePage {

    public ConstructorsPreferencePage() {
        super(Constants.BUNDLE_ID, Messages.PREFPAGE_DESCRIPTION_CONSTRUCTORS);
    }
}
