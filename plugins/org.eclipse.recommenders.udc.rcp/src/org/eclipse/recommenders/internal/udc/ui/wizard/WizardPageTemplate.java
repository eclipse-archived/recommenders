/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.wizard;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import org.eclipse.jface.wizard.WizardPage;

public abstract class WizardPageTemplate extends WizardPage {

    protected WizardPageTemplate() {
        super("");
        this.setTitle(getPageTitle());
    }

    public abstract String getPageTitle();

    @Override
    public void setErrorMessage(final String newMessage) {
        super.setErrorMessage(newMessage);
        setPageComplete(newMessage == null);
    }

}
