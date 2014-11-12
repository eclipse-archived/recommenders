package org.eclipse.recommenders.snipmatch.rcp.util

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.ITypeRoot
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility
import org.eclipse.jdt.ui.SharedASTProvider
import org.eclipse.jface.text.TextSelection
import org.eclipse.recommenders.testing.CodeBuilder
import org.eclipse.recommenders.testing.jdt.JavaProjectFixture
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import java.util.Map;

import static org.junit.Assert.*
import org.eclipse.jdt.core.ICompilationUnit

class SnippetCodeBuilderTest {

    private static val FIXTURE = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "SnippetCodeBuilderTest")

    @Rule
    public val TestName testName = new TestName();

    @Test
    def void testNullSelection() {
        val code = CodeBuilder::method(
            '''
                int i = 0;
            ''')
        val actual = exercise(code, -1, -1)

        assertEquals("", actual)
    }

    @Test
    def void testLocalVariableDeclaredInSelection() {
        val code = CodeBuilder::method(
            '''
                $int i = 0;
                i = 1;$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
            int ${i:newName(int)} = 0;
            ${i} = 1;
            ${cursor}
            '''.toString, actual
        )
    }

    @Test
    def void testLocalVariableReference() {
        val code = CodeBuilder::method(
            '''
                String s = "";
                $s = "hello";$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                ${s:var(java.lang.String)} = "hello";
                ${cursor}
             '''.toString, actual)
    }

    @Test
    def void testDeclaredLocalArrayJavaLangVariable() {
        val code = CodeBuilder::method(
            '''
                $String[][] s = new String[0][];$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                String[][] ${s:newName('java.lang.String[][]')} = new String[0][];
                ${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testDeclaredLocalVariable() {
        val code = CodeBuilder::method(
            '''
                $String s;$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                String ${s:newName(java.lang.String)};
                ${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testFieldReference() {
        val code = CodeBuilder::classbody(
            '''
                List l = null;
                void method() {
                    $l = null;$
                }
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                ${l:field(java.util.List)} = null;
                ${cursor}
             '''.toString, actual)
    }

    @Test
    def void testFieldArrayReference() {
        val code = CodeBuilder::classbody(
            '''
                List[] l = null;
                void method() {
                    $l = null;$
                }
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                ${l:field('java.util.List[]')} = null;
                ${cursor}
             '''.toString, actual)
    }

    @Test
    def void testDeclaredLocalNonJavaLangArrayVariable() {
        val code = CodeBuilder::method(
            '''
                $List[] l = null;$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                List[] ${l:newName('java.util.List[]')} = null;
                ${import:import(java.util.List)}${cursor}
             '''.toString, actual)
    }

    @Test
    def void testStaticFieldReferenceInPackage() {
        val code = "package org.example;" + CodeBuilder::classbody(testName.methodName,
            '''
                static List l = null;
                void method() {
                    $l = null;$
                }
            ''')

        val actual = exercise(code)

        assertEquals(
            '''
                l = null;
                ${importStatic:importStatic(org.example.«testName.methodName».l)}${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testStaticFieldReference() {
        val code = CodeBuilder::classbody(
            '''
                static List l = null;
                void myMethod() {
                    $l = null;$
                }
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                l = null;
                ${cursor}
            '''.toString, actual)
    }

    @Test
    def void testLocalArrayVariableReferenceInPackage() {
        val code = "package org.example;" + CodeBuilder::classbody(testName.methodName,
            '''
                void method() {
                    List[] l = null;
                    $l = null;$
                }
            ''')

        val actual = exercise(code)
        assertEquals(
            '''
                ${l:var('java.util.List[]')} = null;
                ${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testLocalNonJavaLangVariableReferenceInPackage() {
        val code = "package org.example;" + CodeBuilder::classbody(testName.methodName,
            '''
                void method() {
                    List l = null;
                    $l = null;$
                }
            ''')

        val actual = exercise(code)
        assertEquals(
            '''
                ${l:var(java.util.List)} = null;
                ${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testLocalArrayVariableReferenceQualifiedMethodCall() {
        val code = CodeBuilder::method(
            '''
                $List[][] l = new List[0][];
                l.hashCode();$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                List[][] ${l:newName('java.util.List[][]')} = new List[0][];
                ${l}.hashCode();
                ${import:import(java.util.List)}${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testStaticMethodCall() {
        val code = CodeBuilder::classbody(
            '''
                static void myStaticMethod() { }
                void myMethod() {
                    $myStaticMethod();$
                }
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                myStaticMethod();
                ${cursor}
            '''.toString, actual)
    }

    @Test
    def void testQualifiedLocalVariableDeclaration() {
        val code = CodeBuilder::method('''
                $Map.Entry e = null;$
            ''')

        val actual = exercise(code)
        assertEquals(
            '''
                Map.Entry ${e:newName(java.util.Map.Entry)} = null;
                ${import:import(java.util.Map)}${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testGenerateUniqueNamesForLocaVariableDelcarations() {
        val code = CodeBuilder::classbody(
            '''
                $void method1() {
                    String e;
                }
                void method2() {
                    String e2;
                }
                void method3() {
                    String e;
                }$
            ''')

        val actual = exercise(code)

        assertEquals(
            '''
                void method1() {
                    String ${e:newName(java.lang.String)};
                }
                void method2() {
                    String ${e2:newName(java.lang.String)};
                }
                void method3() {
                    String ${e3:newName(java.lang.String)};
                }
                ${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testQualifiedMethodCallFromLocalVariable() {
        val code = CodeBuilder::method(
            '''
                System s = null;
                $s.out.println("");$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                ${s:var(java.lang.System)}.out.println("");
                ${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testParameterDeclarationAndReference() {
        val code = CodeBuilder::classbody(
            '''
                $void method(String s) {
                    s = "";
                }$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                void method(String ${s:newName(java.lang.String)}) {
                    ${s} = "";
                }
                ${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testEmptyInterface() {
        val code = CodeBuilder::method(
            '''
                $public interface Test { }$
            '''
        )

        val actual = exercise(code)

        assertEquals(
            '''
                public interface Test { }
                ${cursor}
            '''.toString, actual)
    }

    @Test
    def void testLocalClassInstantiation() {
        val code = CodeBuilder::method('''MyClass''',
            '''
                $MyClass mc = new MyClass();$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                MyClass ${mc:newName(MyClass)} = new MyClass();
                ${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testReferenceToFieldBeforeSelectionAgainstReferenceToFieldAfterSelection() {
        val codeFieldBeforeSelection = CodeBuilder::classbody(
            '''
                List l = null;
                void method() {
                    $this.l = null;$
                }
            ''')
        val codeFieldAfterSelection = CodeBuilder::classbody(
            '''
                void method() {
                    $this.l = null;$
                }
                List l = null;
            ''')
        val actualBefore = exercise(codeFieldBeforeSelection)
        val actualAfter = exercise(codeFieldAfterSelection)

        assertEquals(actualBefore, actualAfter)
    }

    @Test
    def void testGenerics() {
        val code = CodeBuilder::method(
            '''
                $HashMap<Set, List> map = null$;
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                HashMap<Set, List> ${map:newName(java.util.HashMap)} = null
                ${import:import(java.util.HashMap, java.util.List, java.util.Set)}${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testDollarString() {
        val code = CodeBuilder::method(
            '''
                %"Cost: $20"%
            '''
        )

        val actual = exercise(code, "%")

        assertEquals(
            '''
                "Cost: $$20"
                ${cursor}
            '''.toString, actual)
    }

    @Test
    def void testTwoDollarString() {
        val code = CodeBuilder::method(
            '''
                %"Cost: $$20"%
            '''
        )

        val actual = exercise(code, "%")

        assertEquals(
            '''
                "Cost: $$$$20"
                ${cursor}
            '''.toString, actual)
    }

    @Test
    def void testDollarVariableDeclaration() {
        val code = CodeBuilder::method(
            '''
                %String text$str = "";%
            '''
        )

        val actual = exercise(code, "%")

        assertEquals(
            '''
                String ${textstr:newName(java.lang.String)} = "";
                ${cursor}
            '''.toString, actual)
    }

    @Test
    def void testGenerateUniqueNamesWithDollarVariable() {
        val code = CodeBuilder::method(
            '''
                String textstr = "";
                String text$str = "";
                %textstr = text$str;%
            '''
        )
        val actual = exercise(code, "%")

        assertEquals(
            '''
                ${textstr:var(java.lang.String)} = ${textstr2:var(java.lang.String)};
                ${cursor}
            '''.toString, actual)
    }

    def exercise(CharSequence code) {
        val struct = FIXTURE.createFileAndPackageAndParseWithMarkers(code);
        return exercise(struct.first, struct.second.head, struct.second.last);
    }

    def exercise(CharSequence code, String marker) {
        val struct = FIXTURE.createFileAndParseWithMarkers(code, marker);
        return exercise(struct.first, struct.second.head, struct.second.last);
    }

    def exercise(CharSequence code, int start, int end) {
        val struct = FIXTURE.createFileAndPackageAndParseWithMarkers(code);
        return exercise(struct.first, start, end);
    }

    def exercise(ICompilationUnit cu, int start, int end) {
        val editor = EditorUtility.openInEditor(cu) as CompilationUnitEditor;
        val root = editor.getViewPartInput() as ITypeRoot;
        val ast = SharedASTProvider.getAST(root, SharedASTProvider.WAIT_YES, null);
        val doc = editor.viewer.getDocument();
        val selection = new TextSelection(doc, start, end - start);
        return new SnippetCodeBuilder(ast, doc, selection).build();
    }
}
