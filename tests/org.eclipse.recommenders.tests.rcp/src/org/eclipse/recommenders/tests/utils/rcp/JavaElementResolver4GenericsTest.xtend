package org.eclipse.recommenders.tests.utils.rcp

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IMethod
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.eclipse.recommenders.utils.Checks
import org.eclipse.recommenders.utils.rcp.JavaElementResolver
import org.junit.Test

import static junit.framework.Assert.*

class JavaElementResolver4GenericsTest {

 	JavaElementResolver sut  = new JavaElementResolver()
 
	@Test
	def void testBoundReturn() {
		val code = classbody('''public Iterable<? extends Executor> $m(){return null;}''')
		val method = getMethod(code)
		val actual =  sut.toRecMethod(method).get
		assertEquals("LMyClass.m()Ljava/lang/Iterable;",actual.identifier)
	}
	
	
	@Test
	def void testArrays() {
		val code = classbody('''public Iterable[][] $m(String[][] s){return null;}''')
		val method = getMethod(code)
		val actual =  sut.toRecMethod(method).get
		assertEquals("LMyClass.m([[Ljava/lang/String;)[[Ljava/lang/Iterable;",actual.identifier)
	}
	@Test
	def void testBoundArg() {
		val code = classbody('''public void $m(Iterable<? extends Executor> e){}''')
		val method = getMethod(code)
		val actual =  sut.toRecMethod(method)
		assertNotNull(actual)
	}
	
	@Test
	def void testUnboundArg() {
		val code = classbody('''public <T> void $m(T s){}''')
		val method = getMethod(code)
		val actual =  sut.toRecMethod(method)
		assertNotNull(actual)
	}
	

	def IMethod getMethod(CharSequence code){
		val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
		val struct = fixture.createFileAndParseWithMarkers(code)
		val cu = struct.first;
		val pos = struct.second.head;
		val selected = cu.codeSelect(pos,0)
		val method = selected.get(0) as IMethod
		Checks::ensureIsNotNull(method);
	}
	
	def private classbody(CharSequence classbody)
		'''
		import java.util.*;
		import java.util.concurrent.*;
		public class MyClass {
			«classbody»
		}
  		'''
}