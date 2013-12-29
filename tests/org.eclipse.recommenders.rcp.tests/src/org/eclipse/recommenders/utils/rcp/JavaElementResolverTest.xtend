package org.eclipse.recommenders.utils.rcp

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IMethod
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.eclipse.recommenders.utils.Checks
import org.eclipse.recommenders.utils.names.VmMethodName
import org.eclipse.recommenders.rcp.JavaElementResolver
import org.junit.Test

import static junit.framework.Assert.*
import static org.eclipse.recommenders.tests.CodeBuilder.*
import org.eclipse.recommenders.utils.names.VmTypeName

class JavaElementResolverTest {

    JavaElementResolver sut = new JavaElementResolver()
    JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(), "test")

    @Test
    def void testBoundReturn() {
        val code = classbody('''public Iterable<? extends Executor> $m(){return null;}''')
        val method = getMethod(code)
        val actual = sut.toRecMethod(method).get
        assertEquals("m()Ljava/lang/Iterable;", actual.signature)
    }

    @Test
    def void testArrays() {
        val code = classbody('''public Iterable[][] $m(String[][] s){return null;}''')
        val method = getMethod(code)
        val actual = sut.toRecMethod(method).get
        assertEquals("m([[Ljava/lang/String;)[[Ljava/lang/Iterable;", actual.signature)
    }

    @Test
    def void testBoundArg() {
        val code = classbody('''public void $m(Iterable<? extends Executor> e){}''')
        val method = getMethod(code)
        val actual = sut.toRecMethod(method)
        assertTrue(actual.present)
    }

    @Test
    def void testUnboundArg() {
        val code = classbody('''public <T> void $m(T s){}''')
        val method = getMethod(code)
        val actual = sut.toRecMethod(method)
        assertTrue(actual.present)
    }

    @Test
    def void testJdtMethods() {
        assertTrue("no hashCode?", sut.toJdtMethod(VmMethodName::get("Ljava/lang/Object.hashCode()I")).present);
        assertTrue("no Arrays.sort?", sut.toJdtMethod(VmMethodName::get("Ljava/util/Arrays.sort([J)V")).present);
        assertTrue("no Arrays.equals?",
            sut.toJdtMethod(VmMethodName::get("Ljava/util/Arrays.equals([Ljava/lang/Object;[Ljava/lang/Object;)Z")).
                present);
    }

    @Test
    def void testJdtClass() {
        assertFalse("Lnull found???", sut.toJdtType(VmTypeName::NULL).present)
        assertFalse("primitive found???", sut.toJdtType(VmTypeName::BOOLEAN).present)
        assertTrue("Object not found???", sut.toJdtType(VmTypeName::OBJECT).present)
        assertTrue("NPE not found???", sut.toJdtType(VmTypeName::JAVA_LANG_NULL_POINTER_EXCEPTION).present)
        assertTrue("NPE not found???", sut.toJdtType(VmTypeName::get("Ljava/util/Map$Entry")).present)
    }

    def IMethod getMethod(CharSequence code) {
        val struct = fixture.createFileAndParseWithMarkers(code)
        val cu = struct.first;
        val pos = struct.second.head;
        val selected = cu.codeSelect(pos, 0)
        val method = selected.get(0) as IMethod
        Checks::ensureIsNotNull(method);
    }
}
