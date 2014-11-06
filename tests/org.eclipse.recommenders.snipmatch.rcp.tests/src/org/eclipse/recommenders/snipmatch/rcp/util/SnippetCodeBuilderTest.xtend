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

import static org.junit.Assert.*
import org.eclipse.jdt.core.ICompilationUnit

class SnippetCodeBuilderTest {

    private static val FIXTURE = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "SnippetCodeBuilderTest")

    @Rule
    public val TestName testName = new TestName();

    @Test
    def void testInvalidSelection() {
        val code = CodeBuilder::method(
            '''
                int i = 0;
            ''')
        val actual = exercise(code, -1, -1)

        assertEquals("", actual)
    }

    @Test
    def void testNewArrayAndCalls() {
        val code = CodeBuilder::method(
            '''
                $List[][] ls = new List[0][];
                ls.hashCode();$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                List[][] ${ls:newName('java.util.List[][]')} = new List[0][];
                ${ls}.hashCode();
                ${import:import(java.util.List)}${cursor}
            '''.toString,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=442519
     */
    @Test
    def void testNecessaryStaticImports() {
        val code = CodeBuilder::classDeclaration('''
            import static java.util.Collections.*;
            public class «CodeBuilder::classname»''',
            '''
                $List l = EMPTY_LIST;$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                List ${l:newName(java.util.List)} = EMPTY_LIST;
                ${import:import(java.util.List)}${importStatic:importStatic(java.util.Collections.EMPTY_LIST)}${cursor}
            '''.toString,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=439984
     */
    @Test
    def void testNoJavaLangImport() {
        val code = CodeBuilder::method(
            '''
                $String s = null;$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                String ${s:newName(java.lang.String)} = null;
                ${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testNoJavaLangImportButOtherImports() {
        val code = CodeBuilder::method(
            '''
                $String s = null;
                List l = null;$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                String ${s:newName(java.lang.String)} = null;
                List ${l:newName(java.util.List)} = null;
                ${import:import(java.util.List)}${cursor}
            '''.toString,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=439330
     */
    @Test
    def void testNoEmptyImport() {
        val code = CodeBuilder::method(
            '''
                $int two = 1 + 1;$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                int ${two:newName(int)} = 1 + 1;
                ${cursor}
            '''.toString,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=447186
     */
    @Test
    def void testNoImportForTypeVariable() {
        val code = CodeBuilder::method(CodeBuilder::classname + "<T>",
            '''
                $T t = null;$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                T ${t:newName(java.lang.Object)} = null;
                ${cursor}
            '''.toString,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=440726
     */
    @Test
    def void testReferenceToLocalVariable() {
        val code = CodeBuilder::method(
            '''
                $int i = 0;
                int j = i;$;
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                int ${i:newName(int)} = 0;
                int ${j:newName(int)} = ${i};
                ${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testReferenceToLocalVariableInMultiDeclaration() {
        val code = CodeBuilder::method(
            '''
                $int i = 0, j = i;$;
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                int ${i:newName(int)} = 0, ${j:newName(int)} = ${i};
                ${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testReferenceToLocalOutsideSelection() {
        val code = CodeBuilder::method(
            '''
                List l = null;
                $l.hashCode();$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                ${l:var(java.util.List)}.hashCode();
                ${import:import(java.util.List)}${cursor}
            '''.toString,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=439331
     */
    @Test
    def void testReferenceToFieldBeforeSelection() {
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
                ${import:import(java.util.List)}${cursor}
            '''.toString,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=439331
     */
    @Test
    def void testReferenceToStaticFieldBeforeSelection() {
        val code = CodeBuilder::classbody(testName.methodName,
            '''
                static List L = null;
                void method() {
                    $L = null;$
                }
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                L = null;
                ${importStatic:importStatic(«testName.methodName».L)}${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testReferenceToThisQualifiedFieldBeforeSelection() {
        val code = CodeBuilder::classbody(
            '''
                List l = null;
                void method() {
                    $this.l = null;$
                }
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                this.${l:field(java.util.List)} = null;
                ${import:import(java.util.List)}${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testReferenceToQualifiedFieldBeforeSelection() {
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

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=441205
     */
    @Test
    def void testStaticReferenceToQualifiedField() {
        val code = CodeBuilder::method(
            '''
                $System.out.println("");$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                System.out.println("");
                ${cursor}
            '''.toString,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=439331
     */
    @Test
    def void testReferenceToFieldAfterSelection() {
        val code = CodeBuilder::classbody(
            '''
                void method() {
                    $l = null;$
                }
                List l = null;
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                ${l:field(java.util.List)} = null;
                ${import:import(java.util.List)}${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testReferenceToStaticFieldAfterSelection() {
        val code = CodeBuilder::classbody(testName.methodName,
            '''
                void method() {
                    $L = null;$
                }
                static List L = null;
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                L = null;
                ${importStatic:importStatic(«testName.methodName».L)}${cursor}
            '''.toString,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=439331
     */
    @Test
    def void testReferenceToFieldInSelection() {
        val code = CodeBuilder::classbody(
            '''
                $void method() {
                    l = null;
                }
                List l = null;$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                void method() {
                    ${l} = null;
                }
                List ${l:newName(java.util.List)} = null;
                ${import:import(java.util.List)}${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testReferenceToStaticFieldInSelection() {
        val code = CodeBuilder::classbody(
            '''
                $void method() {
                    L = null;
                }
                static List L = null;$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                void method() {
                    ${L} = null;
                }
                static List ${L:newName(java.util.List)} = null;
                ${import:import(java.util.List)}${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testReferenceToParameterInSelection() {
        val code = CodeBuilder::classbody(
            '''
                $void method(List l) {
                    l = null;
                }$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                void method(List ${l:newName(java.util.List)}) {
                    ${l} = null;
                }
                ${import:import(java.util.List)}${cursor}
            '''.toString,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=437687
     */
    @Test
    def void testVariableDeclarationsInDifferentMethods() {
        val code = CodeBuilder::classbody(
            '''
                $void method1() {
                    String e;
                }
                void method2() {
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
                ${cursor}
            '''.toString,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=437687
     */
    @Test
    def void testVariableDeclarationsInDifferentLoops() {
        val code = CodeBuilder::method(
            '''
                $for (int i = 0; i < 10; i++) {
                    String s;
                }
                for (int i = 0; i < 10; i++) {
                    String s;
                }$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                for (int ${i:newName(int)} = 0; ${i} < 10; ${i}++) {
                    String ${s:newName(java.lang.String)};
                }
                for (int ${i2:newName(int)} = 0; ${i2} < 10; ${i2}++) {
                    String ${s2:newName(java.lang.String)};
                }
                ${cursor}
            '''.toString,
            actual
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=437687
     */
    @Test
    def void testVariableDeclarationsInNestedLoops() {
        val code = CodeBuilder::method(
            '''
                $while (true) {
                    String e;
                    for (int i = 0; i < 10; i++) {
                        String e;
                    }
                }$
                
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                while (true) {
                    String ${e:newName(java.lang.String)};
                    for (int ${i:newName(int)} = 0; ${i} < 10; ${i}++) {
                        String ${e2:newName(java.lang.String)};
                    }
                }
                ${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testParameter() {
        val code = CodeBuilder::classbody(
            '''
                $void method(String s) {
                    String s1;
                    while (true) {
                        s = "";
                    }
                }$
            ''')
        val actual = exercise(code)

        assertEquals(
            '''
                void method(String ${s:newName(java.lang.String)}) {
                    String ${s1:newName(java.lang.String)};
                    while (true) {
                        ${s} = "";
                    }
                }
                ${cursor}
            '''.toString,
            actual
        )
    }

    @Test
    def void testVariableDeclarationsPicksNewName() {
        val code = CodeBuilder::classbody(
            '''
                $void method1() {
                    String e;
                }
                void method2() {
                    String e1;
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
                    String ${e1:newName(java.lang.String)};
                }
                void method3() {
                    String ${e2:newName(java.lang.String)};
                }
                ${cursor}
            '''.toString,
            actual
        )
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
    def void testDollarVariable() {
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
    def void testDollarVariableMethodCall() {
        val code = CodeBuilder::method(
            '''
                String text$str = "";
                %text$str.length();%
            '''
        )

        val actual = exercise(code, "%")

        assertEquals(
            '''
                ${textstr:var(java.lang.String)}.length();
                ${cursor}
            '''.toString, actual)
    }
 
    @Test
    def void testDollarDollarVariableMethodArgument() {
        val code = CodeBuilder::method(
            '''
                %void method(String text$str) { }%
            '''
        )

        val actual = exercise(code, "%")

        assertEquals(
            '''
                void method(String ${textstr:newName(java.lang.String)}) { }
                ${cursor}
            '''.toString, actual)
    }

    @Test
    def void testDollarTwoDollarVariable() {
        val code = CodeBuilder::method(
            '''
                String text$str1 = "";
                String text$str2 = "hello";
                %text$str1 = text$str2;%
            '''
        )

        val actual = exercise(code, "%")

        assertEquals(
            '''
                ${textstr1:var(java.lang.String)} = ${textstr2:var(java.lang.String)};
                ${cursor}
            '''.toString, actual)
    }

    @Test
    def void testDollarVariableClashWithNonDollarVariable() {
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

    @Test
    def void testVariableWithTwoAdjacentDollars() {
        val code = CodeBuilder::method(
            '''
                %String text$$str = "";%
            '''
        )

        val actual = exercise(code, "%")

        assertEquals(
            '''
                String ${textstr:newName(java.lang.String)} = "";
                ${cursor}
            '''.toString, actual)
    }

    def exercise(CharSequence code) {
        val struct = FIXTURE.createFileAndParseWithMarkers(code);
        return exercise(struct.first, struct.second.head, struct.second.last);
    }

    def exercise(CharSequence code, String marker) {
        val struct = FIXTURE.createFileAndParseWithMarkers(code, marker);
        return exercise(struct.first, struct.second.head, struct.second.last);
    }

    def exercise(CharSequence code, int start, int end) {
        val struct = FIXTURE.createFileAndParseWithMarkers(code);
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
