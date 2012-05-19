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
package org.eclipse.recommenders.rdk.index;

import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.utils.annotations.Provisional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

/**
 * Draft of a simple data structure used to generate the search index.
 */
@Provisional
public class ModelDocuments {
    public List<ModelDocument> entries = Lists.newLinkedList();

    public static class ModelDocument {

        @SerializedName("coordinate")
        public String coordinate;
        @SerializedName("bundle-symbolic-names")
        public Set<String> symbolicNames = Sets.newTreeSet();
        public Set<String> fingerprints = Sets.newTreeSet();
        public Set<String> models = Sets.newTreeSet();

    }

    public class AdditionalMetadata {

        public Set<String> models = Sets.newTreeSet();

    }
}
