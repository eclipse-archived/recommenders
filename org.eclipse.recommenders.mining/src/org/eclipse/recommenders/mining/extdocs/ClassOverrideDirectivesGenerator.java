package org.eclipse.recommenders.mining.extdocs;

import static org.eclipse.recommenders.commons.utils.TreeBag.newTreeBag;

import org.eclipse.recommenders.commons.utils.TreeBag;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.server.extdoc.types.ClassOverrideDirectives;

import com.google.common.collect.Sets;

public class ClassOverrideDirectivesGenerator {

    private final double minOverridesProbability;
    private TreeBag<IMethodName> overriddenMethods;
    private ITypeName superclass;
    private int numberOfSubclasses;

    public ClassOverrideDirectivesGenerator(final double minOverridesProbability) {
        this.minOverridesProbability = minOverridesProbability;
    }

    public ClassOverrideDirectives generate(final ITypeName superclass, final Iterable<CompilationUnit> cus) {
        this.superclass = superclass;
        this.overriddenMethods = newTreeBag();
        numberOfSubclasses = 0;

        System.out.println("Superclass: " + superclass);
        System.out.println();
        for (final CompilationUnit cu : cus) {
            numberOfSubclasses++;
            visitOverriddenMethods(cu);
            System.out.println("\t" + cu.primaryType.name);
        }
        filterInfrequentMethods();
        final ClassOverrideDirectives res = toDirective();
        return res;
    }

    private void visitOverriddenMethods(final CompilationUnit cu) {
        final TypeDeclaration type = cu.primaryType;

        for (final MethodDeclaration method : type.methods) {
            if (!method.name.isInit() && method.superDeclaration != null) {
                overriddenMethods.add(method.superDeclaration);
            }
        }
    }

    private ClassOverrideDirectives toDirective() {
        final ClassOverrideDirectives res = ClassOverrideDirectives.create(superclass, numberOfSubclasses,
                overriddenMethods.asMap());
        res.validate();
        return res;
    }

    private void filterInfrequentMethods() {
        for (final IMethodName method : Sets.newHashSet(overriddenMethods.elements())) {
            final int timesObserved = overriddenMethods.count(method);
            final double probability = timesObserved / (double) numberOfSubclasses;
            if (probability < minOverridesProbability) {
                overriddenMethods.removeAll(method);
            }
        }
    }

}
