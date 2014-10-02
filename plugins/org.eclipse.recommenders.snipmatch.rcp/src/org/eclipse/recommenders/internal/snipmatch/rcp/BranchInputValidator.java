/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import org.eclipse.jface.dialogs.IInputValidator;

public class BranchInputValidator implements IInputValidator {

    @Override
    public String isValid(String newText) {
        if (newText.matches("[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*(/[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*)*")) { //$NON-NLS-1$
            return null;
        }
        return Messages.WIZARD_GIT_REPOSITORY_ERROR_INVALID_BRANCH_PREFIX_FORMAT;
    }
}
