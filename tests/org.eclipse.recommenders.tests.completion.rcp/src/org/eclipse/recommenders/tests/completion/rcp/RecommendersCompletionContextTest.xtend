package org.eclipse.recommenders.tests.completion.rcp

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.junit.Test

import static junit.framework.Assert.*
import org.eclipse.jdt.internal.compiler.ast.MessageSend
import org.eclipse.jdt.internal.compiler.ast.ASTNode
import com.google.common.base.Optional
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess
 
class RecommendersCompletionContextTest { 
  
	@Test
	def void test01(){
		val code = methodbody('s1.$;')
		val sut = exercise(code)
		
		assertCompletionNode(sut, typeof(CompletionOnQualifiedNameReference));
		assertCompletionNodeParentIsNull(sut);
	}
	
	
	@Test
	def void test02(){
		val code = methodbody('s1.equals(s1.$);')
		val sut = exercise(code)
		
		assertCompletionNode(sut, typeof(CompletionOnQualifiedNameReference));
		assertCompletionNodeParent(sut, typeof(MessageSend));
	}
	
	
	@Test
	def void test03(){
		val code = methodbody('String s1 = new String();
			s1.
			String s2 = new String();
			s2.$')
		val sut = exercise(code)
		// check is absent but no exception is thrown
		assertEquals(Optional::absent(),sut.receiverType);		
	}
	
	@Test
	def void test04(){
		val code = methodbody('s1.concat("").$;')
		val sut = exercise(code)
		assertCompletionNode(sut, typeof(CompletionOnMemberAccess));
		assertTrue(sut.methodDef.present)
	}
	
	
	@Test
	def void testTypeParameters01() {
		val code = classbody('''
		public <T> void m(T t){t.$}''')
		val sut = exercise(code)
		assertTrue(sut.receiverType.present);
		assertEquals("Object",sut.receiverType.get.elementName);
	}
	
	@Test
	def void testTypeParameters02() {
		val code = classbody('''
		public <T extends Collection> void m(T t){t.$}''')
		val sut = exercise(code)
		assertTrue(sut.receiverType.present);
		assertEquals("Collection",sut.receiverType.get.elementName);
	}
	
	@Test
	def void testTypeParameters021() {
		// doesn't make too much sense. This results in a missing type --> absent()
		val code = classbody('''
		public <T super List> void m(T t){t.$}''')
		val sut = exercise(code)
		assertFalse(sut.receiverType.present);
	}

	@Test
	def void testTypeParameters03() {
		val code = methodbody('''
		Class<?> clazz = null;
		clazz.$''')
		val sut = exercise(code)
		assertTrue(sut.receiverType.present);
		assertEquals("Class",sut.receiverType.get.elementName);
	}
	
	@Test
	def void testTypeParameters04() {
		val code = methodbody('''
		Class<? super String> clazz = null;
		clazz.$''')
		val sut = exercise(code)
		assertTrue(sut.receiverType.present);
		assertEquals("Class",sut.receiverType.get.elementName);
	}
	
	
	def private assertCompletionNode(IRecommendersCompletionContext sut, Class<?> type){
		val node = sut.completionNode;
		assertInstanceof(node,type)
	}
	
	def private assertInstanceof(ASTNode node, Class<?> type){
		assertNotNull("completion node is null!", node)
		assertEquals('''unexpected completion node type. Expected «type» but got «node.getClass»'''.toString,
			type, node.getClass
		)
	}
	
	def private assertCompletionNodeParent(IRecommendersCompletionContext sut, Class<?> type){
		val node = sut.completionNodeParent;
		assertInstanceof(node,type)
	}
	
	def private assertCompletionNodeParentIsNull(IRecommendersCompletionContext sut){
		assertNull("parent node is not null!", sut.completionNodeParent)
	}

	def exercise(CharSequence code){
		val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
		val struct = fixture.createFileAndParseWithMarkers(code.toString)
		val cu = struct.first;
		val completionIndex = struct.second.head
		val ctx = new org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock(cu, completionIndex)
		
		new RecommendersCompletionContextFactoryMock().create(ctx);
	}
	
	
	def private classbody(CharSequence classbody)
		'''
		import java.util.*;
		import java.util.concurrent.*;
		public class MyClass {
			String s1;
			String s2;
			«classbody»
		}
  		'''

	def private methodbody(CharSequence methodbody){
		classbody('''
			void test() {
				«methodbody»
			''')
	  }
}