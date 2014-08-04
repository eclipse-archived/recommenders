package org.eclipse.recommenders.internal.snipmatch.rcp

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility
import org.eclipse.jface.text.TextSelection
import org.eclipse.recommenders.snipmatch.Snippet
import org.eclipse.recommenders.testing.CodeBuilder
import org.eclipse.recommenders.testing.jdt.JavaProjectFixture
import org.junit.Test

import static org.junit.Assert.*

class CreateSnippetHandlerTest {

    static val fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "test")
    CharSequence code

    Snippet actual

    @Test
    def void testNewArrayAndCalls() {
        code = CodeBuilder::method(
            '''
                $List[][] ls = new List[0][];
                ls.hashCode();$
            ''')
        exercise()

        assertEquals(
            '''
                List[][] ${ls:newName('java.util.List[][]')} = new List[0][];
                ${ls}.hashCode();
                ${:import(java.util.List)}${cursor}
            '''.toString,
            actual.code
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=439984
     */
    @Test
    def void testNoJavaLangImport() {
        code = CodeBuilder::method(
            '''
                $String s = null;$
            ''')
        exercise()

        assertEquals(
            '''
                String ${s:newName(java.lang.String)} = null;
                ${cursor}
            '''.toString,
            actual.code
        )
    }

    @Test
    def void testNoJavaLangImportButOtherImports() {
        code = CodeBuilder::method(
            '''
                $String s = null;
                List l = null;$
            ''')
        exercise()

        assertEquals(
            '''
                String ${s:newName(java.lang.String)} = null;
                List ${l:newName(java.util.List)} = null;
                ${:import(java.util.List)}${cursor}
            '''.toString,
            actual.code
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=439330
     */
    @Test
    def void testNoEmptyImport() {
        code = CodeBuilder::method(
            '''
                $int two = 1 + 1;$
            ''')
        exercise()

        assertEquals(
            '''
                int ${two:newName(int)} = 1 + 1;
                ${cursor}
            '''.toString,
            actual.code
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=440726
     */
    @Test
    def void testReferenceToLocalVariable() {
        code = CodeBuilder::method(
            '''
                $int i = 0;
                int j = i;$;
            ''')
        exercise()

        assertEquals(
            '''
                int ${i:newName(int)} = 0;
                int ${j:newName(int)} = ${i};
                ${cursor}
            '''.toString,
            actual.code
        )
    }

    @Test
    def void testReferenceToLocalVariableInMultiDeclaration() {
        code = CodeBuilder::method(
            '''
                $int i = 0, j = i;$;
            ''')
        exercise()

        assertEquals(
            '''
                int ${i:newName(int)} = 0, ${j:newName(int)} = ${i};
                ${cursor}
            '''.toString,
            actual.code
        )
    }

    @Test
    def void testReferenceToLocalOutsideSelection() {
        code = CodeBuilder::method(
            '''
                List l = null;
                $l.hashCode();$
            ''')
        exercise()

        assertEquals(
            '''
                ${l:var(java.util.List)}.hashCode();
                ${:import(java.util.List)}${cursor}
            '''.toString,
            actual.code
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=439331
     */
    @Test
    def void testReferenceToFieldBeforeSelection() {
        code = CodeBuilder::classbody(
            '''
                List l = null;
                void method() {
                    $l = null;$
                }
            ''')
        exercise()

        assertEquals(
            '''
                ${l:field(java.util.List)} = null;
                ${:import(java.util.List)}${cursor}
            '''.toString,
            actual.code
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=439331
     */
    @Test
    def void testReferenceToFieldAfterSelection() {
        code = CodeBuilder::classbody(
            '''
                void method() {
                    $l = null;$
                }
                List l = null;
            ''')
        exercise()

        assertEquals(
            '''
                ${l:field(java.util.List)} = null;
                ${:import(java.util.List)}${cursor}
            '''.toString,
            actual.code
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=439331
     */
    @Test
    def void testReferenceToFieldInSelection() {
        code = CodeBuilder::classbody(
            '''
                $void method() {
                    l = null;
                }
                List l = null;$
            ''')
        exercise()

        assertEquals(
            '''
                void method() {
                    ${l} = null;
                }
                List ${l:newName(java.util.List)} = null;
                ${:import(java.util.List)}${cursor}
            '''.toString,
            actual.code
        )
    }

    @Test
    def void testReferenceToParameterInSelection() {
        code = CodeBuilder::classbody(
            '''
                $void method(List l) {
                    l = null;
                }$
            ''')
        exercise()

        assertEquals(
            '''
                void method(List ${l:newName(java.util.List)}) {
                    ${l} = null;
                }
                ${:import(java.util.List)}${cursor}
            '''.toString,
            actual.code
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=437687
     */
    @Test
    def void testVariableDeclarationsInDifferentMethods() {
        code = CodeBuilder::classbody(
            '''
                $void method1() {
                    String e;
                }
                void method2() {
                    String e;
                }$
            ''')
        exercise()

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
            actual.code
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=437687
     */
    @Test
    def void testVariableDeclarationsInDifferentLoops() {
        code = CodeBuilder::method(
            '''
                $for (int i = 0; i < 10; i++) {
                    String s;
                }
                for (int i = 0; i < 10; i++) {
                    String s;
                }$
            ''')
        exercise()

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
            actual.code
        )
    }

    /*
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=437687
     */
    @Test
    def void testVariableDeclarationsInNestedLoops() {
        code = CodeBuilder::method(
            '''
                $while (true) {
                    String e;
                    for (int i = 0; i < 10; i++) {
                        String e;
                    }
                }$
                
            ''')
        exercise()

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
            actual.code
        )
    }

    @Test
    def void testParameter() {
        code = CodeBuilder::classbody(
            '''
                $void method(String s) {
                    String s1;
                    while (true) {
                        s = "";
                    }
                }$
            ''')
        exercise()

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
            actual.code
        )
    }

    @Test
    def void testVariableDeclarationsPicksNewName() {
        code = CodeBuilder::classbody(
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
        exercise()

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
            actual.code
        )
    }

    @Test
    def void testGenerics() {
        code = CodeBuilder::method(
            '''
                $HashMap<Set, List> map = null$;
            ''')
        exercise()

        assertEquals(
            '''
                HashMap<Set, List> ${map:newName(java.util.HashMap)} = null
                ${:import(java.util.HashMap, java.util.List, java.util.Set)}${cursor}
            '''.toString,
            actual.code
        )
    }

    def void exercise() {
        val struct = fixture.createFileAndParseWithMarkers(code)
        val cu = struct.first;
        val start = struct.second.head;
        val end = struct.second.last;
        val editor = EditorUtility.openInEditor(cu) as CompilationUnitEditor;
        editor.selectionProvider.selection = new TextSelection(start, end - start)
        val sut = new CreateSnippetHandler(newHashSet())
        actual = sut.createSnippet(editor)
    }
}
