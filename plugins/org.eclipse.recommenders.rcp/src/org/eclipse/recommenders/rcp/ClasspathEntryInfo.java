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
package org.eclipse.recommenders.rcp;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.utils.Version;

public class ClasspathEntryInfo {

    public static final ClasspathEntryInfo NULL = new ClasspathEntryInfo();
    public static final String P_LAST_MOD = "modificationDate"; //$NON-NLS-1$
    public static final String P_LOCATION = "location"; //$NON-NLS-1$
    public static final String P_FINGERPRINT = "fingerprint"; //$NON-NLS-1$
    public static final String P_VERSION = "version"; //$NON-NLS-1$
    public static final String P_SYMBOLIC_NAME = "symbolicName"; //$NON-NLS-1$

    private String symbolicName;
    private Version version = Version.UNKNOWN;
    private String jarFileFingerprint;
    private Date jarFileModificationDate;

    private File location;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

    public static ClasspathEntryInfo create(final String symbolicName, final Version version,
            final String jarFingerprint, File location) {
        final ClasspathEntryInfo res = new ClasspathEntryInfo();
        res.setSymbolicName(symbolicName);
        res.setVersion(version);
        res.setFingerprint(jarFingerprint);
        res.setLocation(location);
        return res;
    }

    private transient PropertyChangeSupport chg = new PropertyChangeSupport(this);
    private boolean javaRuntime;

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        chg.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        chg.removePropertyChangeListener(listener);
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public void setSymbolicName(String newSymbolicName) {
        chg.firePropertyChange(P_SYMBOLIC_NAME, symbolicName, symbolicName = newSymbolicName);
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version newVersion) {
        chg.firePropertyChange(P_VERSION, version, version = newVersion);
    }

    public String getFingerprint() {
        return jarFileFingerprint;
    }

    public void setFingerprint(String newFingerprint) {
        chg.firePropertyChange(P_FINGERPRINT, jarFileFingerprint, jarFileFingerprint = newFingerprint);
    }

    public File getLocation() {
        return location;
    }

    public void setLocation(File newLocation) {
        chg.firePropertyChange(P_LOCATION, location, location = newLocation);
    }

    public Date getModificationDate() {
        return jarFileModificationDate;
    }

    public void setModificationDate(Date newModificationDate) {
        chg.firePropertyChange(P_LAST_MOD, jarFileModificationDate, jarFileModificationDate = newModificationDate);
    }

    public void setJavaRuntime(boolean partOfJavaRuntime) {
        this.javaRuntime = partOfJavaRuntime;
    }

    public boolean isJavaRuntime() {
        return javaRuntime;
    }
}
