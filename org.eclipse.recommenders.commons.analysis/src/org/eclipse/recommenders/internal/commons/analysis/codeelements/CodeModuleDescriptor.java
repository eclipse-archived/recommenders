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
