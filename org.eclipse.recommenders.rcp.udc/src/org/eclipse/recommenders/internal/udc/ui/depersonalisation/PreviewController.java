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
package org.eclipse.recommenders.internal.udc.ui.depersonalisation;

import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey.Kind;
import org.eclipse.recommenders.internal.udc.CompilationUnitBuilder;
import org.eclipse.recommenders.internal.udc.depersonalizer.ICompilationUnitDepersonalizer;
import org.eclipse.recommenders.internal.udc.ui.CompareComposite;

public class PreviewController {
    final CompareComposite compareComposite;
    ICompilationUnitDepersonalizer[] depersonalizers;

    public void setDepersonalizers(final ICompilationUnitDepersonalizer[] depersonalizers) {
        this.depersonalizers = depersonalizers;
        refreshPreview();
    }

    public PreviewController(final CompareComposite compareComposite) {
        super();
        this.compareComposite = compareComposite;
        compareComposite.setLeftText(getOriginalCompilationUnitString());
    }

    private void refreshPreview() {
        CompilationUnit depersonalizedUnit = getDefaultCompilationUnit();

        for (final ICompilationUnitDepersonalizer depersonalizer : depersonalizers) {
            depersonalizedUnit = depersonalizer.depersonalize(depersonalizedUnit);
        }

        final String depersonalizedUnitString = GsonUtil.serialize(depersonalizedUnit);

        compareComposite.setRightText(depersonalizedUnitString);
    }

    private String getOriginalCompilationUnitString() {
        return GsonUtil.serialize(getDefaultCompilationUnit());
    }

    protected CompilationUnit getDefaultCompilationUnit() {
        final CompilationUnitBuilder builder = new CompilationUnitBuilder();
        builder.setPrimaryType("Lorg/eclipse/recommenders/lfm/export/ExampleClass", "Ljava/lang/Object");
        builder.addImport("Ljava/lang/Comparable", "bc544e20e723b8c1caf9dec27868727123449cc3");
        builder.addInterfaces("Ljava/lang/Comparable");
        builder.addFields("Ljava/lang/String");
        final String methodName = "compareTo(Lorg/eclipse/recommenders/lfm/export/ExampleClass;)I";
        builder.addMethod(methodName, 17, null, null, 1);
        builder.addObject(methodName, "Ljava/lang/Object", Kind.PARAMETER, "this");
        builder.addObject(methodName, "Lorg/eclipse/recommenders/lfm/export/ExampleClass", Kind.PARAMETER, "o");

        return builder.getCompilationUnit();
    }

}
