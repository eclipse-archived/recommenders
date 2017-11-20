/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.utils;

import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;

@SuppressWarnings("restriction")
public final class Asts {

    private Asts() {
        // Not meant to be instantiated
    }

    /**
     * @deprecated Use {@linkplain ASTProvider#SHARED_AST_LEVEL} directly.
     */
    @Deprecated
    public static int getSharedAstLevel() {
        return ASTProvider.SHARED_AST_LEVEL;
    }
}
