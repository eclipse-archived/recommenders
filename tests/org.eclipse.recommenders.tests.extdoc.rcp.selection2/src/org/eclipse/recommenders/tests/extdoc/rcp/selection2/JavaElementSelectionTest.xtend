package org.eclipse.recommenders.tests.extdoc.rcp.selection2

import java.util.List
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.dom.NodeFinder
import org.eclipse.jdt.core.dom.SimpleName
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.eclipse.xtext.xtend2.lib.StringConcatenation
import org.junit.Ignore
import org.junit.Test

import static junit.framework.Assert.*
import static org.eclipse.recommenders.tests.extdoc.rcp.selection2.XtendUtils.*

class JavaElementSelectionTest {


	@Test
	def void testTypeSelectionInTypeDeclaration() {
		// note: this does not work since classpath cannot resolve this!
		val code = '''class Myc$lass {}'''

		val expected = newListWithFrequency(
			null as String -> 1 
		)
		exerciseAndVerify(code, expected);
	}
	
	@Test
	def void testTypeSelectionsInMethodBody() {
		val code = '''
		class Myclass {
			Str$ing s = new St$ring("");
		}''' 

		val expected = newListWithFrequency(
			"Ljava/lang/String;" -> 2 
		)
		exerciseAndVerify(code, expected);
	}
	
	@Test
	def void testTypeSelectionInExtends() {

		val code = '''
		import java.util.*;
		class Myclass extends L$ist {}
		'''

		val expected = newListWithFrequency(
			"Ljava/util/List<>;" -> 1 
		)
		exerciseAndVerify(code, expected);
	}

	
	@Test
	def void testTypeSelectionInMethodBody () {
		val code = '''
		class Myclass {
			Str$ing s = new St$ring("");
		}'''

		val expected = newListWithFrequency(
			"Ljava/lang/String;" -> 2 
		)

		exerciseAndVerify(code, expected);
	}


	@Test
	def void testMethodSelectionInMethodBody () {
		val code = '''
		class Myclass {
			void test(String s1){
				String s2 = s1.co$ncat("hello");
				s2.hashCode$();
				s1.$equals(s2);
			}
			
		}'''

		val expected = newListWithFrequency(
			"Ljava/lang/String;.concat(Ljava/lang/String;)Ljava/lang/String;" -> 1,
			"Ljava/lang/String;.hashCode()I" -> 1,
			"Ljava/lang/String;.equals(Ljava/lang/Object;)Z" -> 1
		)

		exerciseAndVerify(code, expected);
	}

	@Test
	@Ignore("Only for debugging the ui")
	def void waitAlongTime(){
		Thread::sleep(120*1000)
	}
	def void exerciseAndVerify(StringConcatenation code, List<String> expected){
		val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
		val struct = fixture.parseWithMarkers(code.toString, "MyClass.java")
		val cu = struct.first;
		val pos = struct.second;

		val List<String> actual = newArrayList();
		for(position : pos){
			val selection = NodeFinder::perform(cu,position,0);
			switch (selection) {
  				SimpleName:	{
    	 			val binding = selection.resolveBinding
	     			val javaElement = binding.javaElement
    	 			if(javaElement==null) actual.add(null) 
     				else actual.add(binding.key)
     			}
			}		
		}
		assertEquals(expected, actual)
	}
}