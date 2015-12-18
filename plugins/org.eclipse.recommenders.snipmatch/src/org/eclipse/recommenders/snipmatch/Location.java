/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.snipmatch;

import com.google.gson.annotations.SerializedName;

public enum Location {

    /**
     * Nowhere. Used as a placeholder.
     */
    @SerializedName("NONE") NONE,

    /**
     * Everywhere in a file.
     */
    @SerializedName("GENERIC_FILE") FILE,

    /**
     * Everywhere in a Java source file.
     */
    @SerializedName("FILE") JAVA_FILE,

    /**
     * Everywhere a Java statement or Java type member (field, method, nested type, etc.) is allowed.
     */
    @SerializedName("JAVA") JAVA,

    /**
     * Everywhere a Java statement is allowed.
     */
    @SerializedName("JAVA_STATEMENTS") JAVA_STATEMENTS,

    /**
     * Everywhere a Java type member (field, method, nested type, etc.) is allowed.
     */
    @SerializedName("JAVA_TYPE_MEMBERS") JAVA_TYPE_MEMBERS,

    /**
     * Everywhere in a Javadoc comment.
     */
    @SerializedName("JAVADOC") JAVADOC;
}
