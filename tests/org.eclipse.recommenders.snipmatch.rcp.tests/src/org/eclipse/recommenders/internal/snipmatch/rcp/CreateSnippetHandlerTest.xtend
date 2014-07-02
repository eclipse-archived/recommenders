package org.eclipse.recommenders.internal.snipmatch.rcp

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility
import org.eclipse.jface.text.TextSelection
import org.eclipse.recommenders.internal.snipmatch.rcp.CreateSnippetHandler
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
                $String[] s[] = new String[0][];
                s.hashCode();$
            ''')
        exercise()

        assertEquals(
            '''
                String[] ${s:newName(array)}[] = new String[0][];
                ${s}.hashCode();
                ${:import(java.lang.String)}${cursor}
            '''.toString,
            actual.code
        )
    }

    @Test
    def void testGenerics() {
        code = CodeBuilder::method(
            '''
                $HashMap<String, List> map = null$;
            ''')
        exercise()

        assertEquals(
            '''
                HashMap<String, List> ${map:newName(java.util.HashMap)} = null
                ${:import(java.lang.String, java.util.HashMap, java.util.List)}${cursor}
            '''.toString,
            actual.code
        )
    }

    def void exercise() {
        val struct = fixture.createFileAndParseWithMarkers(code)
        val cu = struct.first;
        val start = struct.second.head;
        val end = struct.second.last;
        val editor = EditorUtility.openInEditor(cu)as CompilationUnitEditor;
        editor.selectionProvider.selection = new TextSelection(start, end - start)
        val sut = new CreateSnippetHandler(newHashSet())
        actual = sut.createSnippet(editor)
    }
}
