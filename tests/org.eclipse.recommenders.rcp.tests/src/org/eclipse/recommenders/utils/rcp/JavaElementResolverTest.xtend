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
import org.junit.Ignore

class JavaElementResolverTest {

    JavaElementResolver sut = new JavaElementResolver()
    JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(), "test")

    @Test
    def void testGenericReturn() {
        val code = classbody('''public List<String> $m(){return null;}''')
        val method = getMethod(code)
        val actual = sut.toRecMethod(method).get
        assertEquals("m()Ljava/util/List;", actual.signature)
    }

    @Test
    def void testBoundReturn() {
        val code = classbody('''public Iterable<? extends Executor> $m(){return null;}''')
        val method = getMethod(code)
        val actual = sut.toRecMethod(method).get
        assertEquals("m()Ljava/lang/Iterable;", actual.signature)
    }

    @Test
    def void testGenericClassWithGenericReturn() {
        val code = classDeclaration('''public class Test<E>''', '''public Iterator<E> $m(){return null;}''')
        val method = getMethod(code);
        val actual = sut.toRecMethod(method).get
        assertEquals("m()Ljava/util/Iterator;", actual.signature)
    }

    @Test
    def void testGenericReturnOnBinaryMethod() {
        val code = classbody('''public void m(){ List<String> l; l.$iterator(); }''')
        val method = getMethod(code);
        val actual = sut.toRecMethod(method).get
        assertEquals("iterator()Ljava/util/Iterator;", actual.signature)
    }

    @Test
    def void testBoundReturnOnBinaryMethod() {
        val code = classbody('''public void m(){ List<? extends Number> l; l.$iterator(); }''')
        val method = getMethod(code);
        val actual = sut.toRecMethod(method).get
        assertEquals("iterator()Ljava/util/Iterator;", actual.signature)
    }

    @Test
    def void testBoundIntersectionReturnOnBinaryMethod() {
        val code = classDeclaration('''public class Test<E extends Number & Closable>''',
            '''public void m(){ List<E> l; l.$iterator(); }''')
        val method = getMethod(code);
        val actual = sut.toRecMethod(method).get
        assertEquals("iterator()Ljava/util/Iterator;", actual.signature)
    }

    @Test
    def void testRawReturnOnBinaryMethod() {
        val code = classbody('''public void m(){ List l; l.$iterator(); }''')
        val method = getMethod(code);
        val actual = sut.toRecMethod(method).get
        assertEquals("iterator()Ljava/util/Iterator;", actual.signature)
    }

    @Test
    def void testPrimitiveParameter() {
        val code = classbody('''public void $m(int i){return null;}''')
        val method = getMethod(code)
        val actual = sut.toRecMethod(method).get
        assertEquals("m(I)V", actual.signature)
    }

    @Test
    def void testPrimitiveArrayParameter() {
        val code = classbody('''public void $m(int[] i){return null;}''')
        val method = getMethod(code)
        val actual = sut.toRecMethod(method).get
        assertEquals("m([I)V", actual.signature)
    }

    @Test
    def void testArrayOfGenericsParameter() {
        val code = classbody('''public void $m(List<Integer>[] lists){return null;}''')
        val method = getMethod(code)
        val actual = sut.toRecMethod(method).get
        assertEquals("m([Ljava/util/List;)V", actual.signature)
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
        val actual = sut.toRecMethod(method).get
        assertEquals("m(Ljava/lang/Iterable;)V", actual.signature)
    }

    @Test
    def void testUnboundArg() {
        val code = classbody('''public <T> void $m(T s){}''')
        val method = getMethod(code)
        val actual = sut.toRecMethod(method).get
        assertEquals("m(Ljava/lang/Object;)V", actual.signature)
    }

    @Test
    @Ignore("See Bug 429979")
    def void testBoundMethod() {
        val code = classbody('''public <T extends List> void $m(T s){}''')
        val method = getMethod(code)
        val actual = sut.toRecMethod(method).get
        assertEquals("m(Ljava/util/List;)V", actual.signature)
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
