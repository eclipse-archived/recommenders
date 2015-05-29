/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn- initial API and implementation.
 */
package org.eclipse.recommenders.internal.types.rcp;

import java.io.Closeable;

import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.ImmutableSet;

public interface IProjectTypesIndex extends Closeable {

    ImmutableSet<String> subtypes(ITypeName expected, String prefix);

    void suggestRebuild();

}
