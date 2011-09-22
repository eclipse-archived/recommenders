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
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.internal.udc.Activator;

public abstract class WizardWithPageChangeProvider extends Wizard {

    public WizardWithPageChangeProvider() {
        super();

    }

    protected IPageChangeProvider getPageChangeProvider() {
        final IWizardContainer wizardContainer = this.getContainer();
        Checks.ensureIsNotNull(wizardContainer);
        Checks.ensureIsInstanceOf(wizardContainer, IPageChangeProvider.class);
        final IPageChangeProvider pageChangeProvider = (IPageChangeProvider) wizardContainer;
        return pageChangeProvider;
    }

    @Override
    public void setContainer(final IWizardContainer wizardContainer) {
        super.setContainer(wizardContainer);
        if (wizardContainer != null) {
            setWindowTitle(Activator.wizardTitle);
            initPageChangedListener(getPageChangeProvider());
        }
    }

    abstract protected void initPageChangedListener(IPageChangeProvider iPageChangeProvider);

    @Override
    public boolean canFinish() {
        if (!super.canFinish()) {
            return false;
        }
        final IWizardPage page = getContainer().getCurrentPage();
        return canFinishWithPage(page);
    }

    protected abstract boolean canFinishWithPage(IWizardPage page);

}