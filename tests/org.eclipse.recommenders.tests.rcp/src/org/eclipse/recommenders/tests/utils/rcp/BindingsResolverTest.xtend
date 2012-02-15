package org.eclipse.recommenders.tests.utils.rcp

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ITypeBinding
import org.eclipse.jdt.core.dom.SimpleName
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.eclipse.recommenders.utils.rcp.JavaElementResolver
import org.eclipse.recommenders.utils.rcp.ast.BindingUtils
import org.junit.Test

import static org.eclipse.jdt.core.dom.NodeFinder.*
import static org.eclipse.recommenders.tests.CodeBuilder.*
import static org.junit.Assert.*
import org.eclipse.jdt.core.dom.TypeParameter
import org.eclipse.jdt.core.dom.ParameterizedType
class BindingsResolverTest {

 	JavaElementResolver jdtResolver  = new JavaElementResolver()
 
	@Test
	def void test01() {
		val names = newArrayList("String", "Integer", "List", "List<Integer>", "List<? extends Number>")
		for(name : names){
			val code =  method('''«name»$ var;''')
			val binding = findTypeBinding(code)
			val recTypeName = BindingUtils::toTypeName(binding).get
			assertTrue(jdtResolver.toJdtType(recTypeName).present);
		}
	}

	

	def ITypeBinding findTypeBinding(CharSequence code){
		val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
		val struct = fixture.parseWithMarkers(code.toString)
		val cu = struct.first;
		val pos = struct.second.head;
		val selection = perform(cu,pos,0);
		switch (selection) {
  				SimpleName:	{
	     			return selection.resolveBinding as ITypeBinding
     			}
  				TypeParameter:	{
	     			return selection.resolveBinding
	     			
     			}
     			ParameterizedType:{
     				return selection.resolveBinding
     			}
     				
			}
			throw new IllegalArgumentException()	
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