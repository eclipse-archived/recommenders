package org.eclipse.recommenders.tests.internal.completion.rcp.subwords

import java.util.List
import org.apache.commons.lang3.StringUtils
import org.eclipse.core.resources.ResourcesPlugin
import org.junit.Ignore
import org.junit.Test

import static junit.framework.Assert.*
import org.eclipse.recommenders.tests.*
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsCompletionProposalComputer
import static org.eclipse.recommenders.tests.CodeBuilder.*
import static java.util.Arrays.*
import org.eclipse.jdt.core.CompletionProposal
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import com.google.common.base.Stopwatch
import org.junit.Before
 
class SubwordsCompletionProposalComputerIntegrationTest { 
  
  	  
	static JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
	

  	Stopwatch stopwatch =  new Stopwatch()
  	long MAX_COMPUTATION_LIMIT_MILLIS =1000
  	 
  	@Test
	def void test000_smoke(){
		val code = OLD_TEST_CLASS()
		smokeTest(code)
		
		for(scenario : SmokeTestScenarios::scenarios){
			smokeTest(scenario)
		}
	}
  
  	@Test 
	def void test001(){
		val code = method('''Object o=""; o.hc$''')
		exercise(code, asList("hashCode"))
	}
 
 	@Test
	def void test002(){
		val code = method('''Object o=""; o.c$''')
		exercise(code, asList("clone","hashCode", "getClass"))
	}
 
 	@Test
	def void test003(){
		val code = method('''Object o=""; o.C$''')
		exercise(code, asList("hashCode", "getClass"))
	}
	
 	@Test
	def void test004(){
		val code = method('''Object o=""; o.cod$''')
		exercise(code, asList("hashCode"))
	}

 	@Test
	def void test005(){
		val code = method('''Object o=""; o.coe$''')
		exercise(code, asList("hashCode", "clone"))
	}
	
	@Test
	def void test006(){
		val code = method('''Object o=""; o.Ce$''')
		exercise(code, asList("hashCode"))
	}
	
	
	@Test
	def void test007_subtype(){
		val code = method('''String s=""; s.lone$''')
		exercise(code, asList("clone"))
	}
	
 	/**
  	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=370572
 	 */
	@Test
	def void testBug370572_1(){
		val code = '''
		public class SubwordsBug {
			File aFile = null;
		
			public void m1() {
				afl$
			}
		
			public void m(File f) {}}
		}
		'''
		var expected = newArrayList("aFile")
		exercise(code, expected); 
	}
	 

	@Test
	@Ignore("this fails because JDT does not propose anything at m($afl) (note, *we* trigger code completion before the first token))")
	def void testBug370572_2(){
		val code = '''
		public class SubwordsBug {
			File aFile = null;
		
			public void m1() {
				m(afl$);
			}
		
			public void m(File f) {}
		}
		'''
		var expected = newArrayList("aFile")
		exercise(code, expected); 
	}

	def smokeTest(CharSequence code){
		fixture.clear
		val struct = fixture.createFileAndParseWithMarkers(code.toString)
		val cu = struct.first;
		for(completionIndex : struct.second){
			val ctx = new JavaContentAssistContextMock(cu, completionIndex)
			val sut = new SubwordsCompletionProposalComputer()
			sut.sessionStarted
			stopwatch = new Stopwatch()
			stopwatch.start
			sut.computeCompletionProposals(ctx, null)
			stopwatch.stop
			failIfComputerTookTooLong
		}
	}
	
	def exercise(CharSequence code, List<String> expected){
		fixture.clear
		val struct = fixture.createFileAndParseWithMarkers(code.toString)
		val cu = struct.first; 
		val completionIndex = struct.second.head
		val ctx = new JavaContentAssistContextMock(cu, completionIndex)
		
		val sut = new SubwordsCompletionProposalComputer()
		sut.sessionStarted
		
		stopwatch.start
		val actual = sut.computeCompletionProposals(ctx, null)
		stopwatch.stop
		
		failIfComputerTookTooLong

		assertEquals(''' some expected values were not found.\nExpected: «expected»,\nFound: «actual» '''.toString, expected.size, actual.size)
		
		for(e : expected) { 
  			assertNotNull(actual.findFirst(p|p.toString.startsWith(e)))
		} 
	}
	
	def failIfComputerTookTooLong(){
		if(stopwatch.elapsedMillis > MAX_COMPUTATION_LIMIT_MILLIS)
			fail('''completion took FAR too long: «stopwatch.elapsedMillis»'''.toString)
	}
}