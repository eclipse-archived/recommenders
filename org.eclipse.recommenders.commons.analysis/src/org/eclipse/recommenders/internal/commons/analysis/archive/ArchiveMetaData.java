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
package org.eclipse.recommenders.internal.commons.analysis.archive;

import java.util.Collection;

import org.eclipse.recommenders.commons.utils.Version;

import com.google.gson.annotations.SerializedName;

public class ArchiveMetaData {

    @SerializedName("_id")
    public String id;

    public String fingerprint;
    public String name;
    public Version version;
    public Collection<ClassId> types;
}
