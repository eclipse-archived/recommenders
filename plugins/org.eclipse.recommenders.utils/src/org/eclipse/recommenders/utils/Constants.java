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
package org.eclipse.recommenders.utils;

import org.eclipse.recommenders.utils.names.IFieldName;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmFieldName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.collect.ImmutableList;

public abstract class Constants {

    /*
     * Repository constants. Used for identifying archives, indexes, fields in indexes in model repository related
     * areas.
     */
    /** {@value} */
    public static final String F_FINGERPRINTS = "fingerprints";
    /** {@value} */
    public static final String F_COORDINATE = "coordinate";
    /** {@value} */
    public static final String F_CLASSIFIER = "classifier";
    /** {@value} */
    public static final String F_ARTIFACT_ID = "artifactId";
    /** {@value} */
    public static final String F_SYMBOLIC_NAMES = "symbolic-names";

    /** {@value} */
    public static final String GID_CR = "org.eclipse.recommenders";
    /** {@value} */
    public static final String AID_OVRS = "overrides";
    /** {@value} */
    public static final String AID_INDEX = "index";

    /** {@value} */
    public static final String R_COORD_INDEX = "org.eclipse.recommenders:index:zip:0.0.0";
    /** {@value} */
    public static final String R_COORD_CALL = "org.eclipse.recommenders:call:zip:0.0.0";
    /** {@value} */
    public static final String VERSION = "0.0.1";

    /** {@value} */
    public static final String EXT_POM = "pom";
    /** {@value} */
    public static final String EXT_ZIP = "zip";
    /** {@value} */
    public static final String EXT_JAR = "jar";
    /** {@value} */
    public static final String EXT_JSON = "json";

    /** {@value} */
    public static final String CLASS_CALL_MODELS = "call";
    /** {@value} */
    public static final String CLASS_DECLARES = "declares";
    /** {@value} */
    public static final String CLASS_OVERRIDES_DIRECTIVES = "ovrd";
    /** {@value} */
    public static final String CLASS_OVERRIDES_PATTERNS = "ovrp";
    /** {@value} */
    public static final String CLASS_OVR_DATA = "ovr";
    /** {@value} */
    public static final String CLASS_OUS_DATA = "ous";
    /** {@value} */
    public static final String CLASS_OUS_ALL_DATA = "allous";
    /** {@value} */
    public static final String CLASS_OVRM_MODEL = "ovrm";
    /** {@value} */
    public static final String CLASS_OVRP_MODEL = "ovrp";
    /** {@value} */
    public static final String CLASS_OVRD_MODEL = "ovrd";
    /** {@value} */
    public static final String CLASS_SELFC_MODEL = "selfc";
    /** {@value} */
    public static final String CLASS_SELFM_MODEL = "selfm";
    /** MethodUsageExamples: {@value} */
    public static final String CLASS_MUE = "mue";
    /** MethodUsageExamples: {@value} */
    public static final String CLASS_CHAIN_MODEL = "chain";
    /** {@value} */
    public static final String CLASS_OUP_DATA = "oups";
    /** {@value} */
    public static final ImmutableList<String> MODEL_CLASSIFIER = ImmutableList.of(CLASS_CALL_MODELS, CLASS_OVRD_MODEL,
            CLASS_OVRP_MODEL, CLASS_OVRM_MODEL, CLASS_SELFC_MODEL, CLASS_SELFM_MODEL);

    /*
     * Type and method name constants. Used in many different locations: analysis, networks, etc.
     */
    public static final ITypeName UNKNOWN_TYPE = VmTypeName.get("LUnkown");
    public static final ITypeName NULL_TYPE = VmTypeName.NULL;

    public static final IMethodName UNKNOWN_METHOD = VmMethodName.get("LECR.unknown()V");
    public static final IMethodName NULL_METHOD = VmMethodName.get("LNull.null()V");
    public static final IMethodName NO_METHOD = VmMethodName.get("LNo.nothing()V");
    public static final IMethodName ANY_METHOD = VmMethodName.get("LAny.any()V");
    public static final IMethodName DUMMY_METHOD = VmMethodName.get("LDummy.dummy()V");
    public static final IFieldName UNKNOWN_FIELD = VmFieldName.get("LNo.field;LNoType");

    /*
     * Network constants. Node names, state names, etc.
     */
    /** {@value} */
    public static final String N_NODEID_CALL_GROUPS = "patterns";
    /** {@value} */
    public static final String N_NODEID_CONTEXT = "contexts";
    /** {@value} */
    public static final String N_NODEID_DEF = "definitions";
    /** {@value} */
    public static final String N_NODEID_DEF_KIND = "kinds";
    /** {@value} */

    public static final String N_STATE_TRUE = "true";
    /** {@value} */
    public static final String N_STATE_FALSE = "false";
    /** {@value} */
    public static final String N_STATE_ANY_CTX = Constants.ANY_METHOD.getIdentifier();
    /** {@value} */
    public static final String N_STATE_DUMMY_CTX = Constants.DUMMY_METHOD.getIdentifier();
    /** {@value} */
    public static final String N_STATE_ANY_DEF = Constants.ANY_METHOD.getIdentifier();
    /** {@value} */
    public static final String N_STATE_DUMMY_DEF = Constants.DUMMY_METHOD.getIdentifier();
    /** {@value} */
    public static final String N_STATE_DUMMY_GRP = "pattern dummy";

}
