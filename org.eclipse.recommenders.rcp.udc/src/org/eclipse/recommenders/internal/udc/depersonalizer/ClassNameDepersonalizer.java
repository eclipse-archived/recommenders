/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.depersonalizer;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeReference;

public class ClassNameDepersonalizer implements ICompilationUnitDepersonalizer {

    @Override
    public CompilationUnit depersonalize(final CompilationUnit compilationUnit) {
        String stringRepresentation = GsonUtil.serialize(compilationUnit);
        stringRepresentation = stringRepresentation.replaceAll(compilationUnit.name, compilationUnit.id);
        for (final TypeReference ref : compilationUnit.imports) {
            if (ref.fingerprint == null || ref.fingerprint.isEmpty()) {
                continue;
            }
            final String identifier = ref.name.getIdentifier();
            stringRepresentation = stringRepresentation.replaceAll("\\Q" + identifier + "\\E", ref.fingerprint);
        }
        System.out.println(stringRepresentation);
        return GsonUtil.deserialize(stringRepresentation, CompilationUnit.class);
    }

}
