package org.eclipse.recommenders.tests.rcp.internal.providers

import java.util.Collections
import java.util.List
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.dom.NodeFinder
import org.eclipse.jdt.core.dom.SimpleName
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.junit.Ignore
import org.junit.Test

import static junit.framework.Assert.*
import static org.eclipse.recommenders.tests.XtendUtils.*

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
			void test(String s1){
				Str$ing s = new St$ring("");
			}
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
	def void testTypeSelectionInFieldDeclaration () {
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
	def void testEmptySelectionInClassBody () {
		val code = '''
		class Myclass {
			$
		}'''

		exerciseAndVerify(code, Collections::emptyList);
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
	def void exerciseAndVerify(CharSequence code, List<String> expected){
		val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
		val struct = fixture.parseWithMarkers(code.toString)
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