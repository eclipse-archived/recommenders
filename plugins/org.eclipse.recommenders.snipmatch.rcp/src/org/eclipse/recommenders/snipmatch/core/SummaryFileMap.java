/**
 * Copyright (c) 2012 Cheng Chen
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Cheng Chen - initial API and implementation and/or initial documentation
*/

package org.eclipse.recommenders.snipmatch.core;

public class SummaryFileMap {
    public String summary;
    public String filePath;
    public boolean enable;

    public SummaryFileMap(String s, String file) {
        this.summary = s;
        this.filePath = file;
        this.enable = true;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}