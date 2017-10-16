/**
 * Copyright (c) 2017 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.statics;

import org.eclipse.recommenders.models.IModelProvider;
import org.eclipse.recommenders.models.UniqueTypeName;

import com.google.common.annotations.Beta;

/**
 * The model provider interface for loading {@link IStaticsModel}s. Note that this interface is a marker interface. Use
 * the "Find references" or "Find implementors" functionality of your IDE to find implementations and example usages of
 * this interface.
 */
@Beta
public interface IStaticsModelProvider extends IModelProvider<UniqueTypeName, IStaticsModel> {
}
