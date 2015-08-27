/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - externalize Strings.
 *    Yasser Aziza - contribution link
 */
package org.eclipse.recommenders.internal.rcp;

import static org.eclipse.recommenders.internal.rcp.Constants.PREF_PAGE_ID;

import org.eclipse.recommenders.internal.rcp.l10n.Messages;
import org.eclipse.recommenders.utils.rcp.preferences.AbstractLinkContributionPage;
import org.eclipse.ui.IWorkbench;

public class RootPreferencePage extends AbstractLinkContributionPage {

    public RootPreferencePage() {
        super(PREF_PAGE_ID);
    }

    @Override
    public void init(IWorkbench workbench) {
        setDescription(Messages.PREFPAGE_DESCRIPTION_EMPTY);
    }
}
