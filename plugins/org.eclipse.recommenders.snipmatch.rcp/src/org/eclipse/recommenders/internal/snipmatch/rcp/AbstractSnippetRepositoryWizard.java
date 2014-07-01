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

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnippetRepositoryConfiguration;

public abstract class AbstractSnippetRepositoryWizard extends Wizard {

    public abstract SnippetRepositoryConfiguration getConfiguration();

    public abstract boolean isApplicable(SnippetRepositoryConfiguration configuration);

    public abstract void setConfiguration(SnippetRepositoryConfiguration configuration);

}
