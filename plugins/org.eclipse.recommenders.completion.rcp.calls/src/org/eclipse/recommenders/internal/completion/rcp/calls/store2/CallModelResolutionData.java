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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.IModelArchive;
import org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils;

public class CallModelResolutionData {
    public static final CallModelResolutionData NULL = new CallModelResolutionData() {
        {
            coordinate = "";
            status = ModelResolutionStatus.UNINITIALIZED;
        }
    };

    public static final String P_COORDINATE = "coordinate";
    public static final String P_STATUS = "status";
    public static final String P_ERROR = "error";
    public static final String P_MODEL = "model";

    public enum ModelResolutionStatus {
        RESOLVED, MANUAL, PROHIBITED, FAILED, UNRESOLVED, UNINITIALIZED
    }

    public enum ModelResolutionPolicy {
        MANUAL, DEFAULT, NEVER
    }

    protected String coordinate;
    protected ModelResolutionStatus status;
    protected String error;
    protected transient IModelArchive<IObjectMethodCallsNet> model;

    public CallModelResolutionData() {
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

    public void setCoordinate(String newCoordinates) {
        checkStringFormat(newCoordinates);
        chg.firePropertyChange(P_COORDINATE, coordinate, coordinate = newCoordinates);
    }

    private void checkStringFormat(String newCoordinates) {
        RepositoryUtils.newArtifact(newCoordinates);
    }

    public ModelResolutionStatus getStatus() {
        return status;
    }

    public void setStatus(ModelResolutionStatus newStatus) {
        chg.firePropertyChange(P_STATUS, status, status = newStatus);
    }

    public String getError() {
        return error;
    }

    public void setError(String newError) {
        chg.firePropertyChange(P_ERROR, error, error = newError);
    }

    public IModelArchive<IObjectMethodCallsNet> getModel() {
        return model;
    }

    public void setModel(IModelArchive<IObjectMethodCallsNet> newModel) {
        chg.firePropertyChange(P_MODEL, model, model = newModel);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
