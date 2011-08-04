/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.tests.fixtures.rcp.utils;

import org.eclipse.swt.events.ControlAdapter;

public class AnonymousListenerDeclaredInMethod {

    // class initializer
    static {
        class AdapterClassInit extends ControlAdapter {
        }
    }

    // static method
    static void __testStatic() {
        class AdapterStatic extends ControlAdapter {
            class AdapterStatic2 extends ControlAdapter {
            }
        }
    }

    // constructor
    public AnonymousListenerDeclaredInMethod() {
        class AdapterConstructor extends ControlAdapter {
        }
    }

    // member method
    public void __test() {
        class AdapterMember extends ControlAdapter {
        }
    }

}
