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
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Objects;

/**
 * Represents an {@link ITypeName} qualified by a {@link ProjectCoordinate} like <i>jre:jre:1.6<i>. Project coordinates
 * are required to find the right recommendation model for the given type. It's in the responsibility of the recommender
 * to qualify the type it wants to make recommendations for.
 */
public class UniqueTypeName implements IUniqueName<ITypeName> {

    private final ITypeName name;
    private final ProjectCoordinate pc;

    public UniqueTypeName(ProjectCoordinate pc, ITypeName name) {
        this.name = name;
        this.pc = pc;
    }

    @Override
    public ITypeName getName() {
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
