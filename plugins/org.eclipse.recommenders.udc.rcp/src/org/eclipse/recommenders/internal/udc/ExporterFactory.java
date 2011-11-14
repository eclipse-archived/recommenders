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
package org.eclipse.recommenders.internal.udc;

import org.eclipse.recommenders.injection.InjectionService;

public class ExporterFactory {

    public static CompilationUnitServerExporter createCompilationUnitServerExporter() {
        final CompilationUnitServerExporter exporter = InjectionService.getInstance().requestInstance(
                CompilationUnitServerExporter.class);

        return exporter;

    }
}
