/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package data;

//call chain 1 ok
public class LocalNameClashWithField {

    String var = "findMe";

    public LocalNameClashWithField() {
        //@start
        final String var = <^Space|.*var.*(1 element).*>
        //@end
        //final String var = this.var
    }
}
