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
package org.eclipse.recommenders.snipmatch.rcp;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;

public interface ISnippetRepositoryWizard extends IWizard {

    SnippetRepositoryConfiguration getConfiguration();

    boolean isApplicable(SnippetRepositoryConfiguration configuration);

    void setConfiguration(SnippetRepositoryConfiguration configuration);
}
