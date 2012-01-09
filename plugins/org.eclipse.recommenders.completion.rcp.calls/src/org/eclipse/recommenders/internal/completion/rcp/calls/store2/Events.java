/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.store2;

import java.io.File;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.recommenders.commons.udc.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath.ManifestResolverInfo;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.IModelArchive;

public class Events {

    public static class DependencyResolutionRequested extends InternalEvent {
        public IPackageFragmentRoot fragmentRoot;
    }

    public static class DependencyResolutionFinished extends InternalEvent {
        public IPackageFragmentRoot fragmentRoot;
        public ClasspathDependencyInformation dependency;
        public File fragmentLocation;
    }

    public static class ManifestResolutionRequested extends InternalEvent {
        public ClasspathDependencyInformation dependency;
        public boolean manuallyTriggered;
    }

    public static class ManifestResolutionFinished extends InternalEvent {
        public ClasspathDependencyInformation dependency;
        public ManifestResolverInfo manifestResolverInfo;
    }

    public static class ModelArchiveDownloadRequested extends InternalEvent {
        public Manifest manifest;
    }

    public static class ModelArchiveDownloadFinished extends InternalEvent {
        public File archive;
    }

    public static class ModelArchiveRegistered extends InternalEvent {
        public IModelArchive<?> archive;
    }

    private static class InternalEvent {
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(final Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

}
