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
package org.eclipse.recommenders.overrides;

import org.eclipse.recommenders.models.IUniqueName;
import org.eclipse.recommenders.models.IModelProvider;
import org.eclipse.recommenders.utils.names.ITypeName;

public interface IOverrideModelProvider extends IModelProvider<IUniqueName<ITypeName>, IOverrideModel> {
}
