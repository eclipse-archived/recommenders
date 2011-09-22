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
package com.mycompany;

import org.eclipse.swt.widgets.Text;

class MyPage extends Page {
    Text t;

    @Override
    void createContents() {
        t = new Text(null, 0);
        t.setText("");
    }
}