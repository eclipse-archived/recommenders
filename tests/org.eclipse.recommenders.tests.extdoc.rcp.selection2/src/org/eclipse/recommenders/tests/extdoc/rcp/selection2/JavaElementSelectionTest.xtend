package org.eclipse.recommenders.tests.extdoc.rcp.selection2

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.dom.NodeFinder
import org.eclipse.jdt.core.dom.SimpleName
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.junit.Test
import static org.eclipse.recommenders.tests.jdt.AstUtils.*
import static org.eclipse.recommenders.tests.extdoc.rcp.selection2.XtendUtils.*

class JavaElementSelectionTest {

	@Test
	def void testAnonymousTypeDeclarationInFieldInitializer () {
		val code = '''
		class Myclass {
			Str¥ing s = new St¥ring("");
		}'''

		val expected = newListWithFrequency(
			"Ljava/lang/String;" -> 2 
		)

		val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
		val struct = fixture.parseWithMarkers(code.toString, "MyClass.java")
		val cu = struct.first;
		val pos = struct.second;

		for(position : pos){
			val selection = NodeFinder::perform(cu,position,0);
			switch (selection) {
  			SimpleName:
  				{
    	 		val binding = selection.resolveBinding
     			val javaElement = binding.javaElement
     			//println(binding)
     			println(javaElement)
     			}
			}		
		}
	}
}