/*******************************************************************************
 * Copyright (c) 2012 Michael Kutschke. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Michael Kutschke - initial API and implementation
 ******************************************************************************/
package org.eclipse.recommenders.jayes.transformation.util;

public class DecompositionFailedException extends Exception {

    private static final long serialVersionUID = 7414539974242778583L;

    public DecompositionFailedException() {
        super();
    }

    public DecompositionFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecompositionFailedException(String message) {
        super(message);
    }

    public DecompositionFailedException(Throwable cause) {
        super(cause);
    }
}
