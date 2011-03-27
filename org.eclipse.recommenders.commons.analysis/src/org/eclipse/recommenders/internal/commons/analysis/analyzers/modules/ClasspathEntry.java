package org.eclipse.recommenders.internal.commons.analysis.analyzers.modules;

import java.io.File;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeReference;

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
