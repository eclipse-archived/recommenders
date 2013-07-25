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
package org.eclipse.recommenders.rcp;

/**
 * Marker interface used by recommenders' DI framework. {@link IRcpService}s may provide methods annotated with @PostConstruct
 * and @PreDestroy to register for special life-cycle hooks: The @PostConstruct hook is called by the DI framework after
 * the service was completely initialized, the @PreDestroy hook is called when the Eclipse workspace shuts down.
 */
public interface IRcpService {

}
