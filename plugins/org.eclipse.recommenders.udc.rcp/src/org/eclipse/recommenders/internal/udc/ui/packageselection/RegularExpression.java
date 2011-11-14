/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.packageselection;

class RegularExpression {
    String regExp;

    public String getRegExp() {
        return regExp;
    }

    public void setRegExp(final String regExp) {
        this.regExp = regExp;
    }

    public String validate() {
        for (final char c : getRegExp().toCharArray()) {
            if (c == '*') {
                continue;
            }
            if (c == '.') {
                continue;
            }
            if (Character.isJavaIdentifierPart(c)) {
                continue;
            }
            return "Invalid character \"" + c + "\" in expression " + toString();
        }
        return null;
    }

    @Override
    public String toString() {
        return getRegExp();
    }
}