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
package completion.overrides;

import completion.overrides.util.SuperClassWithGenericArguments;

public class GenericArgumentsInSuperclassMethods extends SuperClassWithGenericArguments {

    // @start
    // <^Space|test.*%>
    // @end
    // @Override
    // public void test(Collection<String> list) {
    // // TODO Auto-generated method stub
    // super.test(list);
    // }
}
