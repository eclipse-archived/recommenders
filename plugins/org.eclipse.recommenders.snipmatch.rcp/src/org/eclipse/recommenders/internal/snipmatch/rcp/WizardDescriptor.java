/**
 * Copyright (c) 2014 Olav Lenz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import org.eclipse.recommenders.snipmatch.rcp.ISnippetRepositoryWizard;

public class WizardDescriptor {

    private String name;
    private ISnippetRepositoryWizard wizard;

    public WizardDescriptor(String name, ISnippetRepositoryWizard wizard) {
        this.name = name;
        this.wizard = wizard;
    }

    public String getName() {
        return name;
    }

    public ISnippetRepositoryWizard getWizard() {
        return wizard;
    }
}
