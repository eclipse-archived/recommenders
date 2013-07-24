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
package org.eclipse.recommenders.internal.models.rcp;

import java.io.IOException;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * {@link TypeAdapter} implementation for {@link IPackageFragmentRoot}. To serialize a IPackageFragementRoot the method
 * {@code root.getHandleIdentifier()} is used. For the deserialization {@code JavaCore.create(String)} is used.
 * 
 * <p>
 * The json representation looks like: \u003dTest/C:\\/Program Files \\(x86)\\/Java\\/jdk1.6.0_43\\/jre\\/lib\\/rt.jar
 */
public class PackageFragmentRootJsonTypeAdapter extends TypeAdapter<IPackageFragmentRoot> {

    @Override
    public void write(JsonWriter out, IPackageFragmentRoot value) throws IOException {
        out.value(value.getHandleIdentifier());
    }

    @Override
    public IPackageFragmentRoot read(JsonReader in) throws IOException {
        String packageFragmentRootString = in.nextString();
        return (IPackageFragmentRoot) JavaCore.create(packageFragmentRootString);
    }
}
