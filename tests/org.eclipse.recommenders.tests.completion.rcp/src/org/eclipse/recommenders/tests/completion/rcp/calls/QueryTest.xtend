package org.eclipse.recommenders.tests.completion.rcp.calls

import java.util.List
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.CallsCompletionProposalComputer
import org.eclipse.recommenders.internal.utils.codestructs.DefinitionSite$Kind
import org.eclipse.recommenders.tests.CodeBuilder
import org.junit.Test

import static junit.framework.Assert.* 
class QueryTest { 
  


	CallsCompletionProposalComputer sut;
	List<IJavaCompletionProposal> proposals;
	CharSequence code
	
	@Test
	def testDefMethodReturn01(){
		code = CodeBuilder::method('''
		List l = Collections.emptyList();
		l.get(0).$''')
		exercise()
		verifyDefinition(DefinitionSite$Kind::METHOD_RETURN)		
	}

	
	@Test
	def testDefMethodReturn012(){
		code = CodeBuilder::method('''
		List l;
		Object o = l.get(0);
		o.$''')
		exercise()
		verifyDefinition(DefinitionSite$Kind::METHOD_RETURN)		
	}
	
	@Test
	def testDefField(){
		code = CodeBuilder::classbody('''
		List l;
		void __test(){
			l.$;
		}''')
		exercise()
		verifyDefinition(DefinitionSite$Kind::FIELD)		
	}

	
	@Test
	def testDefThis01(){
		code = CodeBuilder::method('''$''')
		exercise()
		verifyDefinition(DefinitionSite$Kind::THIS)		
	}
	
	@Test
	def testDefThis02(){
		code = CodeBuilder::method('''$''')
		exercise()
		verifyDefinition(DefinitionSite$Kind::THIS)		
	}
	
	@Test
	def testDefThis03(){
		code = CodeBuilder::method('''this.$''')
		exercise()
		verifyDefinition(DefinitionSite$Kind::THIS)		
	}
	
	@Test
	def testDefThis04(){
		code = CodeBuilder::method('''super.$''')
		exercise()
		verifyDefinition(DefinitionSite$Kind::THIS)		
	}
	
	
	
	def exercise(){
		val actual = CallCompletionProposalComputerSmokeTest::exercise(code)
		sut = actual.second
		proposals = actual.first
	}
	
	def verifyDefinition(DefinitionSite$Kind kind){
		assertEquals(kind, sut.query.kind)
	}
}