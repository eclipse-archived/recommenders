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
package org.eclipse.recommenders.calls;

import org.eclipse.recommenders.models.BasedTypeName;
import org.eclipse.recommenders.models.IModelProvider;

import com.google.common.annotations.Beta;

/**
 * The model provider interface for loading {@link ICallModel}s. Note that this interface is a marker interface. Use the
 * "Find references" or "Find implementors" functionality of your IDE to find implementations and example usages of this
 * interface.
 */
@Beta
public interface ICallModelProvider extends IModelProvider<BasedTypeName, ICallModel> {
}
