/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.server.extdoc.types;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

import org.eclipse.recommenders.commons.utils.Checks;

public final class ClassSelfcallDirective implements IServerType {

    @SerializedName("_id")
    private String id;
    @SerializedName("_rev")
    private String rev;

    private String providerId;
    private String type;

    private int numberOfSubclasses;
    private Map<String, Integer> calls;

    public Map<String, Integer> getCalls() {
        return calls;
    }

    @Override
    public void validate() {
        Checks.ensureIsTrue("ClassSelfcallDirectives".equals(providerId));
        Checks.ensureIsTrue(type.length() > 6);
        Checks.ensureIsGreaterOrEqualTo(numberOfSubclasses, 1, null);
        Checks.ensureIsTrue(!calls.isEmpty());
    }

    @Override
    public String toString() {
        return id + " / " + rev + " / " + providerId + " / " + type + " / " + numberOfSubclasses;
    }

}
