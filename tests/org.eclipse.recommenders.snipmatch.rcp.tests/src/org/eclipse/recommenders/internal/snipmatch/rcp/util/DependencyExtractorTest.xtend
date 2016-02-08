package org.eclipse.recommenders.internal.snipmatch.rcp.util

import com.google.common.base.Optional
import com.google.common.collect.ImmutableSet
import java.util.Collections
import java.util.Set
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.ITypeRoot
import org.eclipse.jdt.core.dom.ITypeBinding
import org.eclipse.jdt.core.dom.NodeFinder
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility
import org.eclipse.jdt.ui.SharedASTProvider
import org.eclipse.jface.text.TextSelection
import org.eclipse.recommenders.coordinates.ProjectCoordinate
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider
import org.eclipse.recommenders.testing.CodeBuilder
import org.eclipse.recommenders.testing.jdt.JavaProjectFixture
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.mockito.Mockito

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import static org.mockito.Matchers.*
import static org.mockito.Mockito.*

class DependencyExtractorTest {

    private static val FIXTURE = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "DependencyExtractorTest")

    private static val JRE_1_7_0 = new ProjectCoordinate("jre", "jre", "1.7.0");
    private static val JRE_0_0_0 = new ProjectCoordinate("jre", "jre", "0.0.0");
    private static val FOO_1_0_0 = new ProjectCoordinate("foo", "foo", "1.0.0")
    private static val FOO_0_0_0 = new ProjectCoordinate("foo", "foo", "0.0.0")

    @Rule
    public val TestName testName = new TestName();

    private var IProjectCoordinateProvider pcProvider

    @Before
    def void setup() {
        pcProvider = Mockito.mock(typeof(IProjectCoordinateProvider));
        when(pcProvider.resolve(argThat(new TypeMatcher("System")))).thenReturn(Optional.of(JRE_1_7_0));
        when(pcProvider.resolve(argThat(new TypeMatcher("PrintStream")))).thenReturn(Optional.of(JRE_1_7_0));
        when(pcProvider.resolve(argThat(new TypeMatcher("Object")))).thenReturn(Optional.of(JRE_1_7_0));
        when(pcProvider.resolve(argThat(new TypeMatcher("Foo")))).thenReturn(Optional.of(FOO_1_0_0));
        when(pcProvider.resolve(argThat(new TypeMatcher("Bar")))).thenReturn(Optional.absent());
    }

    private def Matcher<ITypeBinding> hasBindingWithName(String name) {
        System.out.println("Has Binding with name: " + name);
        return both(is(instanceOf(ITypeBinding))).and(hasProperty("name", equalTo(name)))
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=447654
     */
    @Test
    def void testDependenciesForLocalVariables() {
        val code = CodeBuilder::classbody(
            '''
                public void method() {
                    Foo foo = new Foo();
                    $if (foo.isTrue()) {
                          System.out.println("empty list");
                    }$
                    Foo foo2 = foo.getFoo();
                }

                private class Foo {
                    public boolean isTrue() {
                        return true;
                    }
                    public Foo getFoo() {
                        return this;
                    }
                }
            ''')
        val actual = exercise(code)

        val Set<ProjectCoordinate> expected = ImmutableSet.of(JRE_0_0_0, FOO_0_0_0);

        assertEquals(
            expected,
            actual
        )
    }

    @Test
    def void testDependenciesForMethodsDeclaredInSuperclass() {
        val code = CodeBuilder::classbody(
            '''
                public void method() {
                    Foo foo = new Foo();
                    $foo.toString();$
                }

                private class Foo {}
            ''')
        val actual = exercise(code)

        val Set<ProjectCoordinate> expected = ImmutableSet.of(FOO_0_0_0);

        assertEquals(
            expected,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=447654
     */
    @Test
    def void testDependenciesOfField() {

        val code = CodeBuilder::classbody(
            '''
                Foo foo;
                public void method() {
                   foo = new Foo();
                    $if (foo.isTrue()) {
                        System.out.println("empty list");
                    }$
                   Foo foo2 = foo.getFoo();
                }

                private class Foo {
                    public boolean isTrue() {
                        return true;
                    }
                    public Foo getFoo() {
                        return this;
                    }
                }
            ''')
        val actual = exercise(code)

        val Set<ProjectCoordinate> expected = ImmutableSet.of(JRE_0_0_0, FOO_0_0_0);

        assertEquals(
            expected,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=447654
     */
    @Test
    def void testUnresolveableVariableName() {
        val code = CodeBuilder::method('''foo.toString();$''')
        val actual = exercise(code)

        val Set<ProjectCoordinate> expected = Collections.emptySet();

        assertEquals(
            expected,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=447654
     */
    @Test
    def void testUnresolveableDeclaredType() {
        val code = CodeBuilder::method('''$Bar bar = null;$''')
        val actual = exercise(code)

        val Set<ProjectCoordinate> expected = Collections.emptySet();

        assertEquals(
            expected,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=447654
     */
    @Test
    def void testDependenciesOfSelectionBeforeNewLine() {

        val code = CodeBuilder::classbody(
            '''
                Foo foo;
                public void method() {
                   foo = new Foo();$
                    if (foo.isTrue()) {
                        System.out.println("empty list");
                    }$
                    Foo foo2 = foo.getFoo();
                }

                private class Foo {
                    public boolean isTrue() {
                        return true;
                    }
                    public Foo getFoo() {
                        return this;
                    }
                }
            ''')
        val actual = exercise(code)

        val Set<ProjectCoordinate> expected = ImmutableSet.of(JRE_0_0_0, FOO_0_0_0);

        assertEquals(
            expected,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=447654
     */
    @Test
    def void testDependenciesOfSelectionAfterNewLine() {

        val code = CodeBuilder::classbody(
            '''
                Foo foo;
                public void method() {
                   foo = new Foo();
                    $if (foo.isTrue()) {
                        System.out.println("empty list");
                    }
                    $Foo foo2 = foo.getFoo();
                }

                private class Foo {
                    public boolean isTrue() {
                        return true;
                    }
                    public Foo getFoo() {
                        return this;
                    }
                }
            ''')
        val actual = exercise(code)

        val Set<ProjectCoordinate> expected = ImmutableSet.of(JRE_0_0_0, FOO_0_0_0);

        assertEquals(
            expected,
            actual
        )
    }

    @Test
    def void testDependencies() {

        val code = CodeBuilder::classbody(
            '''
                Foo foo;
                public void method() {
                    foo = new Foo();
                    if (foo.isTrue()) {
                        $System.out.println("empty list");
                    }
                    $Foo foo2 = foo.getFoo();
                }

                private class Foo {
                    public boolean isTrue() {
                        return true;
                    }
                    public Foo getFoo() {
                        return this;
                    }
                }
            ''')
        val actual = exercise(code)

        val Set<ProjectCoordinate> expected = ImmutableSet.of(JRE_0_0_0);

        assertEquals(
            expected,
            actual
        )
    }

    @Test
    def void testDependenciesSimpleNamePartiallySelectedFromStart() {
        val code = CodeBuilder::method('''Object obj$ect$ = new Object();''')
        val actual = exercise(code)

        val Set<ProjectCoordinate> expected = Collections.emptySet();

        assertEquals(
            expected,
            actual
        )
    }

    @Test
    def void testDependenciesSimpleNamePartiallySelectedToEnd() {
        val code = CodeBuilder::method('''Object $obj$ect = new Object();''')
        val actual = exercise(code)

        val Set<ProjectCoordinate> expected = Collections.emptySet();

        assertEquals(
            expected,
            actual
        )
    }

    @Test
    def void testDependenciesASTConstructor() {
        val code = CodeBuilder::method('''$Object object = new Object();$''')

        val struct = FIXTURE.createFileAndParseWithMarkers(code)
        val cu = struct.first;
        val start = struct.second.head;
        val end = struct.second.last;
        val editor = EditorUtility.openInEditor(cu) as CompilationUnitEditor;
        val root = editor.getViewPartInput() as ITypeRoot;
        val ast = SharedASTProvider.getAST(root, SharedASTProvider.WAIT_YES, null);
        val node = NodeFinder.perform(ast, start, end - start);

        assertEquals(node.startPosition, start);
        assertEquals(node.length, end - start);

        val actual = new DependencyExtractor(node, pcProvider).extractDependencies;

        val Set<ProjectCoordinate> expected = ImmutableSet.of(JRE_0_0_0);

        assertEquals(
            expected,
            actual
        )
    }

    def exercise(CharSequence code) {
        val struct = FIXTURE.createFileAndParseWithMarkers(code)
        val cu = struct.first;
        val start = struct.second.head;
        val end = struct.second.last;
        val editor = EditorUtility.openInEditor(cu) as CompilationUnitEditor;
        val root = editor.getViewPartInput() as ITypeRoot;
        val ast = SharedASTProvider.getAST(root, SharedASTProvider.WAIT_YES, null);
        val doc = editor.viewer.getDocument();
        val selection = new TextSelection(doc, start, end - start);
        return new DependencyExtractor(ast, selection, pcProvider).extractDependencies;
    }
}
