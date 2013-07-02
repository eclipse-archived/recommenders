package org.eclipse.recommenders.tests.completion.rcp;

import static org.eclipse.recommenders.tests.CodeBuilder.classbody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

/**
 * Test that the enclosing type is computed correctly when completion is triggered for the generic argument of a field.
 * 
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=399800">Bug 399800</a>
 */
@RunWith(Parameterized.class)
public class Bug399800Test {

    private final String fieldDeclaration;

    public Bug399800Test(String fieldDeclaration) {
        this.fieldDeclaration = fieldDeclaration;
    }

    @Parameters
    public static Collection<Object[]> fieldDeclarations() {
        LinkedList<Object[]> fieldDeclarations = Lists.newLinkedList();

        fieldDeclarations.add(fieldDeclaration("List<S$>"));
        fieldDeclarations.add(fieldDeclaration("List<S$>;"));
        fieldDeclarations.add(fieldDeclaration("List<S$> field"));
        fieldDeclarations.add(fieldDeclaration("List<S$> field;"));

        fieldDeclarations.add(fieldDeclaration("Map<String, S$>"));
        fieldDeclarations.add(fieldDeclaration("Map<String, S$>;"));
        fieldDeclarations.add(fieldDeclaration("Map<String, S$> field"));
        fieldDeclarations.add(fieldDeclaration("Map<String, S$> field;"));

        fieldDeclarations.add(fieldDeclaration("Map<String, List<S$>>"));
        fieldDeclarations.add(fieldDeclaration("Map<String, List<S$>>;"));
        fieldDeclarations.add(fieldDeclaration("Map<String, List<S$>> field"));
        fieldDeclarations.add(fieldDeclaration("Map<String, List<S$>> field;"));

        return fieldDeclarations;
    }

    private static Object[] fieldDeclaration(String... fieldDeclaration) {
        return fieldDeclaration;
    }

    @Test
    public void testEnclosingType() throws CoreException {
        CharSequence code = classbody("TestClass", fieldDeclaration);

        IRecommendersCompletionContext sut = exercise(code);
        IType enclosingType = sut.getEnclosingType().get();

        assertThat(enclosingType.getElementName(), is(equalTo("TestClass")));
    }

    private IRecommendersCompletionContext exercise(CharSequence code) throws CoreException {
        JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "test");
        Pair<ICompilationUnit, Set<Integer>> struct = fixture.createFileAndParseWithMarkers(code.toString());
        ICompilationUnit cu = struct.getFirst();
        int completionIndex = struct.getSecond().iterator().next();
        JavaContentAssistInvocationContext ctx = new JavaContentAssistContextMock(cu, completionIndex);

        return new RecommendersCompletionContextFactoryMock().create(ctx);
    }
}
