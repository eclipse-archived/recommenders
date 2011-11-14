/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.codesearch;

import java.net.URI;

import org.eclipse.recommenders.internal.server.codesearch.wiring.WebserviceResourceConfig;

/**
 * Maps an internal source URI to an externally accessible URI. For example an
 * internal URI "local:some/class/Name" may be mapped to
 * "http://hostname:port/sources?class=some/class/Name".
 * 
 * Different mappers exist. They are configured in
 * {@link WebserviceResourceConfig#getModule()}.
 * 
 */
public interface ISourceUriMapper {

    URI map(final URI internalSourceUri);
}
