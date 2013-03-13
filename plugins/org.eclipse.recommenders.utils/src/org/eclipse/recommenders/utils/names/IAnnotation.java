/**
 * Copyright (c) 2011 Sven Amann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.utils.names;

import org.eclipse.recommenders.utils.annotations.Provisional;

@Provisional
public interface IAnnotation {

    ITypeName getAnnotationType();

    // TODO information about the runtime entity's field values should be added here
}
