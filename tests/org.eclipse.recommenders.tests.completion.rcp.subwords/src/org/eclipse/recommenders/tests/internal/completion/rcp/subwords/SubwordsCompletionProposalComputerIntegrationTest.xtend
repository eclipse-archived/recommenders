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
import org.eclipse.jdt.core.CompletionRequestorAdapter
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector
import org.eclipse.jface.text.Document
 
class SubwordsCompletionProposalComputerIntegrationTest { 
  
  	  
	static JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
	

  	Stopwatch stopwatch =  new Stopwatch()
  	long MAX_COMPUTATION_LIMIT_MILLIS =2000
  	 
  	@Test
	def void test000_smoke(){
		for(scenario : SmokeTestScenarios::scenarios){
			smokeTest(scenario)
		}
	}
  
  	@Test 
	def void test001(){
		val code = method('''Object o=""; o.hc$''')
		exerciseAndVerify(code, asList("hashCode"))
	}
 
 	@Test
	def void test002(){
		val code = method('''Object o=""; o.c$''')
		exerciseAndVerify(code, asList("clone","hashCode", "getClass"))
	}
 
 	@Test
	def void test003(){
		val code = method('''Object o=""; o.C$''')
		exerciseAndVerify(code, asList("hashCode", "getClass"))
	}
	
 	@Test
	def void test004(){
		val code = method('''Object o=""; o.cod$''')
		exerciseAndVerify(code, asList("hashCode"))
	}

 	@Test
	def void test005(){
		val code = method('''Object o=""; o.coe$''')
		exerciseAndVerify(code, asList("hashCode", "clone"))
	}
	
	@Test
	def void test006(){
		val code = method('''Object o=""; o.Ce$''')
		exerciseAndVerify(code, asList("hashCode"))
	}
	
	
	@Test
	def void test007_subtype(){
		val code = method('''String s=""; s.lone$''')
		exerciseAndVerify(code, asList("clone"))
	}
	
	@Test 
	def void test008_overloaded(){
		val code = method('''Object o=""; o.w$''')
		exerciseAndVerify(code, asList("wait", "wait","wait"))
	}
	
	
	@Test 
	def void test009_ProposedGettersAndSetters(){
		val code = classbody('''String id;$''')
		exerciseAndVerifyLenient(code, asList("getId", "setId"))
	}
	
	@Test 
	def void test010_ConstuctorCalls(){
		val code = classbody('''ConcurrentHashMap b = new ConcurrentHashMap$''')
		exerciseAndVerifyLenient(code, asList("ConcurrentHashMap(int"))
	}
	
		
	@Test 
	def void test008_ranking(){
		val code = method('''String s=""; s.has$''')
		val actual = exercise(code) 
		val pHashCode = actual.findFirst(p | p.toString.startsWith("hashCode")) as IJavaCompletionProposal
		val pGetChars= actual.findFirst(p | p.toString.startsWith("getChars")) as IJavaCompletionProposal
		assertTrue(pGetChars.relevance< pHashCode.relevance)
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
		exerciseAndVerify(code, expected); 
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
		exerciseAndVerify(code, expected); 
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
			// warm up jdt
			cu.codeComplete(completionIndex, new CompletionProposalCollector(cu,false))
			
			sut.computeCompletionProposals(ctx, null)
			stopwatch.stop
//			failIfComputerTookTooLong(code)
		}
	}
	
	def exerciseAndVerifyLenient(CharSequence code, List<String> expected){
		val actual = exercise(code)
		for(e : expected) {
			val match = actual.findFirst(p|p.toString.startsWith(e)) 
  			assertNotNull(match)
  			applyProposal(match, code)
  			actual.remove(match)
  			if(actual.empty){
  				return;
  			} 
		}  
	}
	
	def applyProposal(IJavaCompletionProposal proposal, CharSequence code){
		val doc = new Document(code.toString)
		proposal.apply(doc)
		assertTrue('''applying template «proposal» on code «code» failed.'''.toString, doc.get.length> code.length)
	}
	
	def exerciseAndVerify(CharSequence code, List<String> expected){
		val actual = exercise(code)
		
		assertEquals(''' some expected values were not found.\nExpected: «expected»,\nFound: «actual» '''.toString, expected.size, actual.size)
		for(e : expected) {
			val match = actual.findFirst(p|p.toString.startsWith(e)) 
  			assertNotNull(match)
  			applyProposal(match, code)
  			actual.remove(match)
		} 
	}
	
	
	def List<IJavaCompletionProposal> exercise(CharSequence code){
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
//		failIfComputerTookTooLong(code)
		return actual;
	}
	
	
	def failIfComputerTookTooLong(CharSequence code){
		if(stopwatch.elapsedMillis > MAX_COMPUTATION_LIMIT_MILLIS)
			fail('''completion took FAR too long: «stopwatch.elapsedMillis»\n in:\n«code»'''.toString)
	}
}