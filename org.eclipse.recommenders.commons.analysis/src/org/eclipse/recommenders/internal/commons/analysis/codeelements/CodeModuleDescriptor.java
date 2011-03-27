/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.commons.analysis.codeelements;

import java.util.Set;

import org.eclipse.recommenders.commons.utils.Version;

public class CodeModuleDescriptor {

    public final CodeElementKind kind = CodeElementKind.MODULE;
    public Version version;
    public String name;
    public Set<String> aliases;
    public Set<TypeReference> classes;
}
