/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.coordinates.rcp;

import java.io.IOException;

import org.eclipse.recommenders.coordinates.ProjectCoordinate;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * {@link TypeAdapter} implementation for {@link ProjectCoordinate}. To serialize a ProjectCoordinate the method
 * {@code ProjectCoordinate.toString()} is used. For the deserialization {@code ProjectCoordinate.valueof(String)} is
 * used.
 * <p>
 * The json representation looks like: jre:jre:1.6.0
 */
public class ProjectCoordinateJsonTypeAdapter extends TypeAdapter<ProjectCoordinate> {

    @Override
    public void write(JsonWriter out, ProjectCoordinate value) throws IOException {
        out.value(value.toString());
    }

    @Override
    public ProjectCoordinate read(JsonReader in) throws IOException {
        String projectCoordinateString = in.nextString();
        return ProjectCoordinate.valueOf(projectCoordinateString);
    }
}
