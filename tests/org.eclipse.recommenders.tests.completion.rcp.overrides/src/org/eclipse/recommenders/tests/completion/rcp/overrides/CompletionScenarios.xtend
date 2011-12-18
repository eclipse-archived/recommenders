package org.eclipse.recommenders.tests.completion.rcp.overrides

import java.util.List
import org.apache.commons.lang3.StringUtils
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.recommenders.completion.rcp.IntelligentCompletionContextResolver
import org.eclipse.recommenders.utils.rcp.JavaElementResolver
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.eclipse.recommenders.tests.jdt.TestJavaContentAssistContext
import org.junit.Test

import static junit.framework.Assert.*
import org.eclipse.recommenders.internal.completion.rcp.overrides.OverridesCompletionProposalComputer
 
class CompletionScenarios { 
  
 
	@Test
	def void testFindLocalAnchor(){
		val code = '''
		import java.util.concurrent.*;
		public class MyClass extends Object{
			public int hashCode(){return 0;}
			$
		}
		'''
		var expected = w(newArrayList(
			"equals"
			))
			
		exercise(code, expected); 
	} 
	
	def exercise(CharSequence code, List<? extends List<String>> expected){
		val fixture = new JavaProjectFixture(ResourcesPlugin::workspace,"test")
		val struct = fixture.createFileAndParseWithMarkers(code.toString, "MyClass.java")
		val cu = struct.first;
		val completionIndex = struct.second.head
		val ctx = new TestJavaContentAssistContext(cu, completionIndex)
		val resolver = new JavaElementResolver()
		val recommender = MockRecommender::get
		val sut = new OverridesCompletionProposalComputer(recommender, new IntelligentCompletionContextResolver(resolver),resolver)
		sut.sessionStarted
		val proposals = sut.computeCompletionProposals(ctx, null) 
		 for(proposal : proposals){
		} 
		assertTrue(''' some expected values were not found «expected» '''.toString, expected.empty)
	}
	
	def l(String spaceSeparatedElementNames){ 
		val elementNames = StringUtils::split(spaceSeparatedElementNames);
		return newArrayList(elementNames) as List<String>
	}
	def w (String [] chains){
		val List<List<String>> res = newArrayList();
		for(chain :chains){
			res.add(l(chain))
		}
		return res as List<List<String>>
	}
	
}