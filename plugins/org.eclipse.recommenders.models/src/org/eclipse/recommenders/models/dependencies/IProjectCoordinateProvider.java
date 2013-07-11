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
package org.eclipse.recommenders.models.dependencies;

import org.eclipse.recommenders.models.ProjectCoordinate;

/**
 * Maps an IDE specific code element to a {@link ProjectCoordinate}.
 * <p>
 * Note that this interface is only a marker interface which will be implemented by each IDE-implementation
 * independently. An Eclipse-based provider may contain methods like:
 * 
 * <pre>
 * Optional&lt;ProjectCoordinate&gt; map(IMethod m);
 * 
 * Optional&lt;ProjectCoordinate&gt; map(IType t);
 * 
 * Optional&lt;ProjectCoordinate&gt; map(IPackgeFragmentRoot p);
 * 
 * Optional&lt;ProjectCoordinate&gt; map(IJavaProject p);
 * </pre>
 * 
 * The API for other IDEs or evaluation frameworks may use other methods names or abstractions.
 */
public interface IProjectCoordinateProvider {

}
