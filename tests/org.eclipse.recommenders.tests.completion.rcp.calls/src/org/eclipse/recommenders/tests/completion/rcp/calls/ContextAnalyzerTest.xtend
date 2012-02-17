package org.eclipse.recommenders.tests.completion.rcp.calls

import java.util.List
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.NodeFinder
import org.eclipse.recommenders.commons.udc.ObjectUsage
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.AstBasedObjectUsageResolver
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.junit.Test

import static junit.framework.Assert.*
import org.junit.Before
 
class ContextAnalyzerTest { 

	static JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
	
	@Before
	def before(){
		fixture.clear
	}
	  
	@Test
	def void testCalls(){
		val code = methodbody('
			ExecutorService pool;
			pool.shutdown();
			pool.hashCode();
			')
		val res = exercise(code,"pool")
		res.assertCalls(newArrayList("shutdown", "hashCode"));
	}
	
	/**
	 * documentation purpose: we simply match on variable names.
	 * We do no control flow or variable scope analysis!
	 */
	@Test
	def void testCallsOnReusedVar(){
		val code = methodbody('
			Object o = new Object();
			o.hashCode();
			o = new Object();
			o.equals(null);
			')
		val res = exercise(code,"o")
		res.assertCalls(newArrayList("hashCode", "equals"));
		res.assertDef("<init>")
	}
	
	@Test
	def void testCallsOnParam01(){
		// this test ensures that not accicentially calls on this are collected for parameter (as happend)
		val code = classbody('
		public void m1(String s$){
			hashCode();
		}
		')
		val res = exercise(code,"s")
		res.assertCalls(newArrayList());
		res.assertDef("m1")
	}
	
	
	@Test
	def void testCallsOnThisAndSuper(){
		val code = methodbody('
			hashCode();
			super.wait();
			this.equals(null);
			')
		val res = exercise(code,"this")
		res.assertCalls(newArrayList("hashCode", "wait","equals"));
	}
	
	@Test
	def void testCallsSuperConstructor(){
		val code =  classbody('
			MyClass() {
				super();
				$
			}
			')
		val res = exercise(code,"this")
		res.assertCalls(newArrayList("<init>"));
	}
	
	@Test
	def void testCallThisConstructor(){
		val code = classbody('
			MyClass() {
			}

			MyClass(String s) {
				this();
				$
			}
			')
		val res = exercise(code,"this")
		res.assertCalls(newArrayList("<init>"));
	}
	
	@Test
	def void testDefConstructor(){
		val code = methodbody('
			Object o = new Object();
			')
		val res = exercise(code,"o")
		res.assertDef("<init>");
		res.assertType("Object")
	}

	@Test
	def void testDefMethodReturn(){
		val code = methodbody('
			ExecutorService pool = Executors.newCachedThreadPool();
			')
		val res = exercise(code,"pool")
		res.assertDef("newCachedThreadPool")
		res.assertType("ExecutorService")
	}
	
	@Test
	def void testDefSuperMethodReturn(){
		val code = methodbody('
			int hash = super.hashCode();
			')
		val res = exercise(code,"hash")
		res.assertDef("hashCode")
		res.assertType("I")
	}

	@Test
	def void testDefOnCallChain(){
		val code = methodbody('
			int i = Executors.newCachedThreadPool().hashCode();
			')
		val res = exercise(code,"i")
		res.assertDef("hashCode")
		res.assertType("I")
	}
	
	@Test
	def void testDefOnAlias(){
		val code = methodbody('
			int i = 23;
			int j = i;
			')
		val res = exercise(code,"j")
		assertNull(res.definition);
		res.assertType("I")
	}
	
	@Test
	def void testDefAssignment(){
		val code = methodbody('
			int i,j = 23;
			j = i;
			')
		val res = exercise(code,"j")
		assertNull(res.definition);
		res.assertType("I")
	}
	
	
	def private classbody(CharSequence classbody)
		'''
		import java.util.*;
		import java.util.concurrent.*;
		public class MyClass {
			«classbody»
		}
  		'''

	def private methodbody(CharSequence methodbody){
		classbody('''
			void test() {
				«methodbody»
				$
			''')
	  }
	  
	def private exercise (CharSequence code,String varname){
		val struct = fixture.parseWithMarkers(code.toString)
		val cu = struct.first;
		val pos = struct.second.head;
		val enclosingMethod = findEnclosingMethod(cu, pos)
		val sut = new AstBasedObjectUsageResolver();
		return sut.findObjectUsage(varname, enclosingMethod);
	}
		
	def private findEnclosingMethod(CompilationUnit ast, int pos){
		
		val node = NodeFinder::perform(ast,pos,0)
		var parent = node;
		while(!(parent instanceof MethodDeclaration)){
			parent = parent.parent;
		}
		parent as MethodDeclaration;
	}

	def private assertType(ObjectUsage usage, String simpleTypeName){
		assertNotNull(usage.type)
		usage.type.className.equals(simpleTypeName)
	}
	
	def private assertCalls(ObjectUsage usage, List<String> methods){
		for(name : methods){
			val match = usage.calls.findFirst(e | e.name.equals(name))
			if(match==null)
				fail("method " + name +" not found in " + usage.calls)
		}
		assertEquals("expected calls not same size as actual", methods.size, usage.calls.size)
	}
	
	def private assertDef(ObjectUsage usage, String method){
		if(usage.definition==null) fail("no definition found")
		if(!usage.definition.name.equals(method))
			fail('''Def did not match «method»'''.toString)
	}
}