package org.eclipse.recommenders.rcp

import org.junit.Test
import static junit.framework.Assert.*
import static org.eclipse.recommenders.tests.jdt.AstUtils.*

class FindMarkersInAstTest {

    @Test
    def void test001() {
        val code = '''$public class X extends Y {}'''
        val markers = createAstWithMarkers(code.toString)
        assertTrue(markers.second.contains(0))
    }

    @Test
    def void test002() {
        val code = '''class $X$ {}'''
        val markers = createAstWithMarkers(code.toString)

        assertFalse(markers.first.toString.contains(MARKER))

        assertTrue(markers.second.contains(6))
        assertTrue(markers.second.contains(7))
    }
}
