/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.models;

import static org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus.UNINITIALIZED;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

@SuppressWarnings("rawtypes")
public class ModelArchiveMetadata<K, V> {

    public enum ModelArchiveResolutionStatus {
        RESOLVED, MANUAL, PROHIBITED, FAILED, UNRESOLVED, UNINITIALIZED
    }

    public enum ArchiveModelUpdatePolicy {
        MANUAL, DEFAULT, NEVER
    }

    public static final String P_COORDINATE = "coordinate";
    public static final String P_STATUS = "status";
    public static final String P_ERROR = "error";
    public static final String P_MODEL = "model";
    public static final String P_LOCATION = "location";

    @SuppressWarnings("unchecked")
    public static final ModelArchiveMetadata NULL = new ModelArchiveMetadata() {
        {
            // use fields instead of setters to bypass checks
            coordinate = "";
            status = UNINITIALIZED;
        }
    };

    protected String coordinate;
    protected File location;
    protected ModelArchiveResolutionStatus status;
    protected String error;

    protected transient Artifact artifact;
    protected transient IModelArchive<K, V> model;

    public ModelArchiveMetadata() {
    }

    private transient PropertyChangeSupport chg = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        chg.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        chg.removePropertyChangeListener(listener);
    }

    public String getCoordinate() {
        return coordinate;
    }

    public Artifact getArtifact() {
        if (artifact == null && !StringUtils.isEmpty(coordinate)) {
            artifact = new DefaultArtifact(coordinate);
        }
        return artifact;
    }

    public void setCoordinate(String newCoordinates) {
        checkStringFormat(newCoordinates);
        artifact = null;
        chg.firePropertyChange(P_COORDINATE, coordinate, coordinate = newCoordinates);
    }

    private void checkStringFormat(String newCoordinates) {
        if (newCoordinates != null)
            RepositoryUtils.newArtifact(newCoordinates);
    }

    public ModelArchiveResolutionStatus getStatus() {
        return status;
    }

    public void setStatus(ModelArchiveResolutionStatus newStatus) {
        chg.firePropertyChange(P_STATUS, status, status = newStatus);
    }

    public String getError() {
        return error;
    }

    public void setError(String newError) {
        chg.firePropertyChange(P_ERROR, error, error = newError);
    }

    public IModelArchive<K, V> getModel() {
        return model;
    }

    public void setModel(IModelArchive<K, V> newModel) {
        chg.firePropertyChange(P_MODEL, model, model = newModel);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    /**
     * Returns the location of the package fragment root this meta-data object is associated with.
     * 
     * @return
     */
    public File getLocation() {
        return location;
    }

    public void setLocation(File newLocation) {
        chg.firePropertyChange(P_MODEL, location, location = newLocation);
    }
}
