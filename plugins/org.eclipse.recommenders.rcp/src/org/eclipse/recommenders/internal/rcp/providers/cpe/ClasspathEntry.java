/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marcel Bruch - initial API and implementation. 
 */
package org.eclipse.recommenders.internal.rcp.providers.cpe;

import java.io.File;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.recommenders.utils.Version;

public class ClasspathEntry {

    public enum Kind {

        LIBRARY, PROJECT, UNKNONW
    }

    public Kind kind;
    public File location;
    public String name;
    public String fingerprint;
    public Version version;
    public Set<TypeReference> types;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
