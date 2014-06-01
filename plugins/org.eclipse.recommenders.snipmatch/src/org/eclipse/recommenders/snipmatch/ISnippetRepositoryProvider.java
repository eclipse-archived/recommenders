/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.snipmatch;

import java.io.File;
import java.util.Collection;

import com.google.common.base.Optional;

public interface ISnippetRepositoryProvider {

    boolean isApplicable(String identifier);

    ISnippetRepositoryConfiguration fromPreferenceString(String stringRepresentation);

    String toPreferenceString(ISnippetRepositoryConfiguration configuration);

    Optional<ISnippetRepository> create(ISnippetRepositoryConfiguration configuration, File basedir);

    Collection<ISnippetRepositoryConfiguration> getDefaultConfigurations();

}
