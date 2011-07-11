package org.eclipse.recommenders.commons.lfm;
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


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.Version;

public class LibraryIdentifier {

    public static LibraryIdentifier UNKNOWN = new LibraryIdentifier("", Version.UNKNOWN);

    public String name;
    public Version version;

    protected LibraryIdentifier() {
        // Only used for deserialization
    }

    public LibraryIdentifier(final String name, final Version version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }
}
