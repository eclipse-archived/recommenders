package org.eclipse.recommenders.completion.rcp.it;

import static org.eclipse.recommenders.testing.CodeBuilder.classbody;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.testing.rcp.completion.rules.TemporaryWorkspace;
import org.junit.ClassRule;
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

    @ClassRule
    public static final TemporaryWorkspace WORKSPACE = new TemporaryWorkspace();

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

        IRecommendersCompletionContext sut = WORKSPACE.createProject().createFile(code).triggerContentAssist();
        IType enclosingType = sut.getEnclosingType().get();

        assertThat(enclosingType.getElementName(), is(equalTo("TestClass")));
    }
}
