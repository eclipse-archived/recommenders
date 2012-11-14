package org.eclipse.recommenders.tests.completion.rcp.calls

import java.util.List
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.CallsCompletionProposalComputer
import org.eclipse.recommenders.tests.CodeBuilder
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock
import org.eclipse.recommenders.tests.completion.rcp.RecommendersCompletionContextFactoryMock
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.eclipse.recommenders.utils.Tuple
import org.eclipse.recommenders.utils.rcp.JavaElementResolver
import org.junit.Before
import org.junit.Test

import static junit.framework.Assert.*
import static org.eclipse.recommenders.tests.completion.rcp.calls.CallCompletionProposalComputerSmokeTest.*
import org.eclipse.recommenders.tests.SmokeTestScenarios 
class CallCompletionProposalComputerSmokeTest { 
  
	static JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
	@Before
	def void before(){
		fixture.clear
	}

	def method(CharSequence code){
		CodeBuilder::classbody(
		'''
		public void __test(Object o, List l) {
			«code»
		}
		''')
	}
	
	
		@Test
	def void test000_smoke(){
		for(scenario : SmokeTestScenarios::scenarios){
			exercise(scenario)
		}
	}
	
	@Test
	def void testStdCompletion(){
		val code = method('''
			o.$'''
		)
		test(code)
	}

	@Test
	def void testOnConstructor(){
		val code = method('''new Object().$''')
		test(code)
	}


	@Test
	def void testOnReturn(){
		val code = method('''l.get(0).$''')
		test(code)
	}


	@Test
	def void testInIf(){
		val code = method('''if(o.$)''')
		test(code)
	}

	@Test
	def void testExpectsPrimitive(){
		val code = method('''int i = o.$''')
		test(code)
	}


	@Test
	def void testExpectsNonPrimitive(){
		val code = method('''Object o1 = o.$''')
		test(code)
	}

	@Test
	def void testInMessageSend(){
		val code = method('''l.add(o.$)''')
		test(code)
	}
	
	@Test
	def void testPrefix01(){
		val code = method('''o.has$''')
		test(code)
	}

	@Test
	def void testPrefix02(){
		val code = method('''o.hashc$''')
		test(code)
	}

	@Test
	def void testPrefix03(){
		val code = method('''o.hashC$''')
		test(code)
	}

	@Test
	def void testPrefix04(){
		val code = method('''o.x$''')
		test(code,0)
	}


	

	def private test(CharSequence code){
		test(code,1)
	}

	def private test(CharSequence code, int numberOfExpectedProposals){
		val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
		val struct = fixture.createFileAndParseWithMarkers(code.toString)
		val cu = struct.first;
		cu.becomeWorkingCopy(null)
		// just be sure that this file still compiles...
		val ast = cu.reconcile(AST::JLS4, true,true, null,null);
		assertNotNull(ast)
		val CallsCompletionProposalComputer sut = new CallsCompletionProposalComputer(new ModelStoreMock(), new JavaElementResolver(),
            new RecommendersCompletionContextFactoryMock(), CallsPreferenceStoreMock::create())
		for(pos : struct.second){
			val proposals = sut.computeCompletionProposals(new JavaContentAssistContextMock(cu, pos), null);
			assertEquals("wrong number of proposals", numberOfExpectedProposals, proposals.size())
		}
	}

 	def static Tuple<List<IJavaCompletionProposal>, CallsCompletionProposalComputer> exercise(CharSequence code){
 		val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
 		fixture.clear
		val struct = fixture.createFileAndParseWithMarkers(code.toString)
		val cu = struct.first;
		cu.becomeWorkingCopy(null)
		// just be sure that this file still compiles...
		val ast = cu.reconcile(AST::JLS4, true,true, null,null);
		assertNotNull(ast)
		val CallsCompletionProposalComputer sut = new CallsCompletionProposalComputer(new ModelStoreMock(), new JavaElementResolver(),
            new RecommendersCompletionContextFactoryMock(), CallsPreferenceStoreMock::create())
        val proposals = sut.computeCompletionProposals(new JavaContentAssistContextMock(cu, struct.second.head), new NullProgressMonitor());
        
        Tuple::newTuple(proposals, sut) as Tuple
 }
	
	
	
}