package org.eclipse.recommenders.completion.rcp.it;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.ImportedPackagesFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.testing.rcp.completion.rules.TemporaryWorkspace;
import org.eclipse.recommenders.utils.names.IPackageName;
import org.eclipse.recommenders.utils.names.VmPackageName;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class ImportedPackagesContextFunctionTest {

    private static final IPackageName JAVA_UTIL = VmPackageName.get("java/util");

    private final String importDeclaration;
    private final IPackageName expectedPackage;

    public ImportedPackagesContextFunctionTest(String importDeclaration, IPackageName expectedPackage) {
        this.importDeclaration = importDeclaration;
        this.expectedPackage = expectedPackage;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario("import java.util.List;", JAVA_UTIL));
        scenarios.add(scenario("import java.util.*;", JAVA_UTIL));
        scenarios.add(scenario("import static java.util.Collections.singleton;", JAVA_UTIL));
        scenarios.add(scenario("import static java.util.Collections.*;", JAVA_UTIL));

        return scenarios;
    }

    private static Object[] scenario(String importDeclaration, IPackageName expectedPackage) {
        return new Object[] { importDeclaration, expectedPackage };
    }

    @ClassRule
    public static TemporaryWorkspace WORKSPACE = new TemporaryWorkspace();

    @Test
    public void test() throws Exception {
        CompilationUnit ast = WORKSPACE.createProject().createFile(importDeclaration  + "public class Importer {}").getAst();

        IRecommendersCompletionContext context = mock(IRecommendersCompletionContext.class);
        when(context.getAST()).thenReturn(Optional.of(ast));

        ImportedPackagesFunction sut = new ImportedPackagesFunction();
        Set<IPackageName> packages = sut.compute(context, CompletionContextKey.IMPORTED_PACKAGES);

        assertThat(getOnlyElement(packages), is(equalTo(expectedPackage)));
    }
}
