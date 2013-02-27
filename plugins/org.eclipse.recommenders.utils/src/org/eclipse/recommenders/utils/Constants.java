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

import org.eclipse.recommenders.internal.utils.codestructs.DefinitionSite;
import org.eclipse.recommenders.utils.names.IFieldName;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmFieldName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;

public interface Constants {

    /*
     * Repository constants. Used for identifying archives, indexes, fields in indexes in model repository related
     * areas.
     */
    /** {@value} */
    String F_FINGERPRINTS = "fingerprints";
    /** {@value} */
    String F_COORDINATE = "coordinate";
    /** {@value} */
    String F_CLASSIFIER = "classifier";
    /** {@value} */
    String F_ARTIFACT_ID = "artifactId";
    /** {@value} */
    String F_SYMBOLIC_NAMES = "symbolic-names";

    /** {@value} */
    String GID_CR = "org.eclipse.recommenders";
    /** {@value} */
    String AID_OVRS = "overrides";
    /** {@value} */
    String AID_INDEX = "index";

    /** {@value} */
    String R_COORD_INDEX = "org.eclipse.recommenders:index:zip:0.0.0";
    /** {@value} */
    String R_COORD_CALL = "org.eclipse.recommenders:call:zip:0.0.0";
    /** {@value} */
    String VERSION = "0.0.1";

    /** {@value} */
    String EXT_POM = "pom";
    /** {@value} */
    String EXT_ZIP = "zip";
    /** {@value} */
    String EXT_JAR = "jar";
    /** {@value} */
    String EXT_JSON = "json";

    /** {@value} */
    String CLASS_CALL_MODELS = "call";
    /** {@value} */
    String CLASS_DECLARES = "declares";
    /** {@value} */
    String CLASS_OVERRIDES_DIRECTIVES = "ovrd";
    /** {@value} */
    String CLASS_OVERRIDES_PATTERNS = "ovrp";
    /** {@value} */
    String CLASS_OVR_DATA = "ovr";
    /** {@value} */
    String CLASS_OUS_DATA = "ous";
    /** {@value} */
    String CLASS_OUS_ALL_DATA = "allous";
    /** {@value} */
    String CLASS_OVRM_MODEL = "ovrm";
    /** {@value} */
    String CLASS_OVRP_MODEL = "ovrp";
    /** {@value} */
    String CLASS_OVRD_MODEL = "ovrd";
    /** {@value} */
    String CLASS_SELFC_MODEL = "selfc";
    /** {@value} */
    String CLASS_SELFM_MODEL = "selfm";
    /** MethodUsageExamples: {@value} */
    String CLASS_MUE = "mue";
    /** MethodUsageExamples: {@value} */
    String CLASS_CHAIN_MODEL = "chain";
    /** {@value} */
    String CLASS_OUP_DATA = "oups";

    /*
     * Type and method name constants. Used in many different locations: analysis, networks, etc.
     */
    ITypeName UNKNOWN_TYPE = VmTypeName.get("LUnkown");
    ITypeName NULL_TYPE = VmTypeName.NULL;

    IMethodName UNKNOWN_METHOD = VmMethodName.get("LECR.unknown()V");
    IMethodName NULL_METHOD = VmMethodName.get("LNull.null()V");
    IMethodName NO_METHOD = VmMethodName.get("LNo.nothing()V");
    IMethodName ANY_METHOD = VmMethodName.get("LAny.any()V");
    IMethodName DUMMY_METHOD = VmMethodName.get("LDummy.dummy()V");

    DefinitionSite.Kind UNKNOWN_KIND = DefinitionSite.Kind.UNKNOWN;
    IFieldName UNKNOWN_FIELD = VmFieldName.get("LNo.field;LNoType");

    /*
     * Network constants. Node names, state names, etc.
     */
    /** {@value} */
    String N_NODEID_CALL_GROUPS = "patterns";
    /** {@value} */
    String N_NODEID_CONTEXT = "contexts";
    /** {@value} */
    String N_NODEID_DEF = "definitions";
    /** {@value} */
    String N_NODEID_DEF_KIND = "kinds";
    /** {@value} */

    String N_STATE_TRUE = "true";
    /** {@value} */
    String N_STATE_FALSE = "false";
    /** {@value} */
    String N_STATE_ANY_CTX = Constants.ANY_METHOD.getIdentifier();
    /** {@value} */
    String N_STATE_DUMMY_CTX = Constants.DUMMY_METHOD.getIdentifier();
    /** {@value} */
    String N_STATE_ANY_DEF = Constants.ANY_METHOD.getIdentifier();
    /** {@value} */
    String N_STATE_DUMMY_DEF = Constants.DUMMY_METHOD.getIdentifier();
    /** {@value} */
    String N_STATE_DUMMY_GRP = "pattern dummy";
}
