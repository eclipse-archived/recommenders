package org.eclipse.recommenders.tests.completion.rcp.templates

import java.util.List
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.recommenders.internal.completion.rcp.templates.TemplatesCompletionProposalComputer
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock
import org.eclipse.recommenders.tests.completion.rcp.RecommendersCompletionContextFactoryMock
import org.eclipse.recommenders.tests.completion.rcp.calls.ModelStoreMock
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture
import org.eclipse.recommenders.utils.rcp.JavaElementResolver
import org.junit.Test

import static junit.framework.Assert.*
import static org.eclipse.recommenders.internal.completion.rcp.templates.TemplatesCompletionProposalComputer$CompletionMode.*
import static org.eclipse.recommenders.tests.CodeBuilder.*
import org.junit.Ignore


class TemplateCompletionProposalComputerTest { 
  
  TemplatesCompletionProposalComputer sut 
  List<IJavaCompletionProposal> proposals
  CharSequence code
  
  	@Test
  	def testThis01(){
  		code = method('''
  			$
  		''')
  		exercise()
  		assertEquals(THIS, sut.getCompletionMode())
  	}
  
  	@Test
  	def testThis01a(){
  		code = method('''
  			this.$
  		''')
  		exercise()
  		assertEquals(THIS, sut.getCompletionMode())
  	}

	@Test
  	def testThis01b(){
  		code = method('''
  			super.$
  		''')
  		exercise()
  		assertEquals(THIS, sut.getCompletionMode())
  	}
    
	@Test
  	def testThis02(){
  		code = method('''
  			eq$
  		''')
  		exercise()
  		assertEquals(THIS, sut.getCompletionMode())
  	}
  

	@Test
  	def testTypeName01(){
  		code = method('''
  			List$
  		''')
  		exercise()
  		assertEquals(TYPE_NAME, sut.getCompletionMode())
  	}
  
  	@Test
  	def testTypeName02(){
  		code = method('''
  			java.util.List$
  		''')
  		exercise()
  		assertEquals(TYPE_NAME, sut.getCompletionMode())
  	}
	@Test
  	def testThis01c(){
  		code = method('''
  			Event evt;
  			evt$
  		''')
  		exercise()
  		assertEquals(THIS, sut.getCompletionMode())
  	}

	@Test
  	def testVar02(){
  		code = method('''
  			Event evt;
  			evt.$
  		''')
  		exercise()
  		assertEquals(MEMBER_ACCESS, sut.getCompletionMode())
  	}

	@Test
  	def testVar03(){
  		code = method('''
  			Event evt;
  			evt.evt.$
  		''')
  		exercise()
  		assertEquals(MEMBER_ACCESS, sut.getCompletionMode())
  	}

	def private exercise(){
		val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(),"test")
		val struct = fixture.createFileAndParseWithMarkers(code.toString)
		val cu = struct.first;
		cu.becomeWorkingCopy(null)
		// just be sure that this file still compiles...
		val ast = cu.reconcile(AST::JLS4, true,true, null,null);
		assertNotNull(ast)
		sut = new TemplatesCompletionProposalComputer(new RecommendersCompletionContextFactoryMock(),
			new ModelStoreMock(), new JavaElementResolver())
		val pos = struct.second.head;
		proposals = sut.computeCompletionProposals(new JavaContentAssistContextMock(cu, pos), null);
	}
}