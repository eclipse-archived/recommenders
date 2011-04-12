package org.eclipse.recommenders.internal.commons.analysis.analyzers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnitVisitor;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;

import com.google.inject.Singleton;
import com.ibm.wala.classLoader.IClass;

@Singleton
public class EmptySetsClearerCompilationUnitFinalizer implements ICompilationUnitFinalizer {

    @Override
    public void finalizeClass(CompilationUnit compilationUnit, IClass exampleClass, IProgressMonitor monitor) {
        compilationUnit.accept(new CompilationUnitVisitor() {

            @Override
            public boolean visit(TypeDeclaration type) {
                type.clearEmptySets();
                return true;
            }

            @Override
            public boolean visit(MethodDeclaration method) {
                method.clearEmptySets();
                return true;
            }

            @Override
            public boolean visit(ObjectInstanceKey objectInstanceKey) {
                objectInstanceKey.clearEmptySets();
                return false;
            }

        });

    }

}
