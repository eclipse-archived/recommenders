/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.models;

import java.io.IOException;
import java.io.InputStream;

public interface IInputStreamTransformer {

    InputStream transform(InputStream stream) throws IOException;
}
