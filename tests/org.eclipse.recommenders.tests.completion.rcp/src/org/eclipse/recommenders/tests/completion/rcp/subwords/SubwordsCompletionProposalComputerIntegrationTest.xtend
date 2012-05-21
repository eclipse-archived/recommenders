package org.eclipse.recommenders.tests.completion.rcp.subwords

import com.google.common.base.Stopwatch
import java.util.List
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.jface.text.Document
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsCompletionProposalComputer
import org.eclipse.recommenders.tests.SmokeTestScenarios
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.junit.Ignore
import org.junit.Test

import static java.util.Arrays.*
import static junit.framework.Assert.*
import static org.eclipse.recommenders.tests.CodeBuilder.*
import static org.eclipse.recommenders.tests.completion.rcp.subwords.SubwordsCompletionProposalComputerIntegrationTest.*
import org.eclipse.osgi.internal.loader.buddy.SystemPolicy$ParentClassLoader
 
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
		exerciseAndVerify(code, asList("hashCode", "getClass", 
		// since the latest changes to not filter upper-case typos...
		"clone"))
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
		exerciseAndVerify(code, asList("hashCode","clone"))
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
	def void test011_NewCompletionOnCompleteStatement(){
		val code = method('''new LinkedLis$;''')
		exerciseAndVerifyLenient(code, asList("LinkedList("))
	}
	
	@Test 
	def void test012_OverrideWithNewImports(){
		val code =
		'''import java.util.concurrent.ThreadPoolExecutor;
		import java.util.concurrent.TimeUnit;
		public class MyThreadPool extends ThreadPoolExecutor {
		
		awaitTermination$
		}''' 
		
		
		val proposal = exercise(code).head
		val d = new Document ()
		d.set(code.toString)
		proposal.apply(d);
		var after  = d.get
		assertTrue("doc:\n" + after, after.contains('''public boolean awaitTermination(long'''))
		assertTrue("doc:\n" + after, after.contains(''', TimeUnit'''))
		assertTrue("doc:\n" + after, after.contains('''throws InterruptedException {'''))
		assertTrue("doc:\n" + after, after.contains('''return super.awaitTermination('''))
	}
		
	@Test 
	def void test013_ranking(){
		val code = method('''String s=""; s.at$''')
		val actual = exercise(code) 
		val pHashCode = actual.findFirst(p | p.toString.startsWith("charAt")) as IJavaCompletionProposal
		val plastIndexOf= actual.findFirst(p | p.toString.startsWith("lastIndexOf")) as IJavaCompletionProposal
		assertTrue(plastIndexOf.relevance< pHashCode.relevance)
	}
	
		@Test 
	def void test014_ranking_prefix(){
		val code = method('''String s=""; s.char$''')
		val actual = exercise(code) 
		val p1 = actual.findFirst(p | p.toString.startsWith("charAt")) as IJavaCompletionProposal
		val p2= actual.findFirst(p | p.toString.startsWith("getChars")) as IJavaCompletionProposal
		assertTrue(p1.relevance > p2.relevance)
	}

	@Test 
	def void test015(){
		val code = method('''new File$("")''')
		val actual = exercise(code) 
		val p1 = actual.findFirst(p | p.toString.startsWith("File(")) as IJavaCompletionProposal
		assertNotNull(p1)
	}	
	
	
	@Test 
	def void test016(){
		val code = '''public class MyClass {
			main$
		}'''
		exercise(code).forEach( p| 
			assertTrue(p.displayString.startsWith("Main"))
		)
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
			
			sut.computeCompletionProposals(ctx, new NullProgressMonitor())
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
		val sut = SubwordsMockUtils::createEngine
		sut.sessionStarted
		stopwatch.start
		val actual = sut.computeCompletionProposals(ctx, new NullProgressMonitor())
		stopwatch.stop
//		failIfComputerTookTooLong(code)
		return actual;
	}
	
	
	def failIfComputerTookTooLong(CharSequence code){
		if(stopwatch.elapsedMillis > MAX_COMPUTATION_LIMIT_MILLIS)
			fail('''completion took FAR too long: «stopwatch.elapsedMillis»\n in:\n«code»'''.toString)
	}
}