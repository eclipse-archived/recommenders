/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.models;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;

import com.google.common.base.Objects;

public abstract class AbstractUniqueName<T> implements IUniqueName<T> {

    private final T name;
    private final ProjectCoordinate pc;

    public AbstractUniqueName(ProjectCoordinate pc, T name) {
        this.name = name;
        this.pc = pc;
    }

    @Override
    public T getName() {
        return name;
    }

    @Override
    public ProjectCoordinate getProjectCoordinate() {
        return pc;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", getName()).add("qualifier", getProjectCoordinate()).toString();
    }
}
