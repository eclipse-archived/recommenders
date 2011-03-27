package org.eclipse.recommenders.internal.commons.analysis.analyzers.modules;

import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.Sets;

public class ProjectClasspath {

    public Set<ClasspathEntry> path;

    public static ProjectClasspath create() {
        final ProjectClasspath res = new ProjectClasspath();
        res.path = Sets.newHashSet();
        return res;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
