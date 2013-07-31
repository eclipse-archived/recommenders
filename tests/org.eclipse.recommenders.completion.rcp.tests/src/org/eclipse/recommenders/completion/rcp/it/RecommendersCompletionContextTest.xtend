package org.eclipse.recommenders.completion.rcp.it

import com.google.common.base.Optional
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference
import org.eclipse.jdt.internal.compiler.ast.ASTNode
import org.eclipse.jdt.internal.compiler.ast.MessageSend
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext
import org.eclipse.recommenders.internal.rcp.CachingAstProvider
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.eclipse.recommenders.utils.names.VmTypeName
import org.junit.Test

import static com.google.common.base.Optional.*
import static junit.framework.Assert.*
import static org.eclipse.recommenders.tests.CodeBuilder.*

class RecommendersCompletionContextTest {

    @Test
    def void test01() {
        val code = method('s1.$;')
        val sut = exercise(code)

        assertCompletionNode(sut, typeof(CompletionOnQualifiedNameReference));
        assertCompletionNodeParentIsNull(sut);
    }

    @Test
    def void test02() {
        val code = method('s1.equals(s1.$);')
        val sut = exercise(code)

        assertCompletionNode(sut, typeof(CompletionOnQualifiedNameReference));
        assertCompletionNodeParent(sut, typeof(MessageSend));
    }

    @Test
    def void testArraysAsList() {
        val code = method('java.util.Arrays.asList(get$)')
        val sut = exercise(code)

        // this should be Object[]"
        // TODO testing for existence only ATM
        assertFalse(sut.expectedTypeNames.empty)
    }

    @Test
    def void test03() {
        val code = method(
            'String s1 = new String();
			s1.
			String s2 = new String();
			s2.$')
        val sut = exercise(code)

        // check is absent but no exception is thrown
        assertEquals(Optional::absent(), sut.receiverType);
    }

    @Test
    def void test04() {
        val code = method('String s1; s1.concat("").$;')
        val sut = exercise(code)
        assertCompletionNode(sut, typeof(CompletionOnMemberAccess));
        assertTrue(sut.methodDef.present)
    }

    @Test
    def void testTypeParameters01() {
        val code = classbody(
            '''
            public <T> void m(T t){t.$}''')
        val sut = exercise(code)
        assertTrue(sut.receiverType.present);
        assertEquals("Object", sut.receiverType.get.elementName);
    }

    @Test
    def void testTypeParameters02() {
        val code = classbody(
            '''
            public <T extends Collection> void m(T t){t.$}''')
        val sut = exercise(code)
        assertTrue(sut.receiverType.present);
        assertEquals("Collection", sut.receiverType.get.elementName);
    }

    @Test
    def void testTypeParameters021() {

        // doesn't make too much sense. This results in a missing type --> absent()
        val code = classbody(
            '''
            public <T super List> void m(T t){t.$}''')
        val sut = exercise(code)
        assertFalse(sut.receiverType.present);
    }

    @Test
    def void testTypeParameters03() {
        val code = method(
            '''
            Class<?> clazz = null;
            clazz.$''')
        val sut = exercise(code)
        assertTrue(sut.receiverType.present);
        assertEquals("Class", sut.receiverType.get.elementName);
    }

    @Test
    def void testTypeParameters04() {
        val code = method(
            '''
            Class<? super String> clazz = null;
            clazz.$''')
        val sut = exercise(code)
        assertTrue(sut.receiverType.present);
        assertEquals("Class", sut.receiverType.get.elementName);
    }

    def void testSignatureParseException() {
        val code = '''
            public class Part {
            
            public Part(String string,$ S) {
            }
            }
        '''
        val sut = exercise(code)
        assertEquals(absent(), sut.enclosingElement)
    }

    @Test
    def void testExpectedTypesInIf() {
        val code = method('''if($)''')
        val expected = exercise(code).expectedTypeNames
        assertTrue(expected.contains(VmTypeName::BOOLEAN))
    }

    @Test
    def void testExpectedTypesInNewFile() {
        val code = method('''new File($)''')
        val expected = exercise(code).expectedTypeNames
        assertTrue(expected.contains(VmTypeName::STRING))
        assertEquals(3, expected.size)
    }

    @Test
    def void testExpectedTypesInNewArrayListString() {
        val code = method('''List<String> l = new ArrayList<String>($);''')
        val expected = exercise(code).expectedTypeNames
        assertTrue(expected.contains(VmTypeName::get("Ljava/util/Collection")))
    }

    @Test
    def void testExpectedTypesInListStringAdd() {
        val code = method('''List<String> l = new ArrayList<String>();l.add($)''')
        val expected = exercise(code).expectedTypeNames
        assertTrue(expected.contains(VmTypeName::STRING))
    }

    def private assertCompletionNode(IRecommendersCompletionContext sut, Class<?> type) {
        val node = sut.completionNode.orNull;
        assertInstanceof(node, type)
    }

    def private assertInstanceof(ASTNode node, Class<?> type) {
        assertNotNull("completion node is null!", node)
        assertEquals(
            '''unexpected completion node type. Expected «type» but got «node.getClass»'''.toString,
            type,
            node.getClass
        )
    }

    def private assertCompletionNodeParent(IRecommendersCompletionContext sut, Class<?> type) {
        val node = sut.completionNodeParent.orNull;
        assertInstanceof(node, type)
    }

    def private assertCompletionNodeParentIsNull(IRecommendersCompletionContext sut) {
        assertNull("parent node is not null!", sut.completionNodeParent.orNull)
    }

    def exercise(CharSequence code) {
        val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(), "test")
        val struct = fixture.createFileAndParseWithMarkers(code.toString)
        val cu = struct.first;
        val completionIndex = struct.second.head
        val ctx = new JavaContentAssistContextMock(cu, completionIndex)

        new RecommendersCompletionContext(ctx, new CachingAstProvider())
    }
}
