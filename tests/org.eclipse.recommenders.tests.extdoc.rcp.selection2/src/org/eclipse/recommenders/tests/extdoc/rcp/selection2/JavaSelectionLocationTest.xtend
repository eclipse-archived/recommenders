package org.eclipse.recommenders.tests.extdoc.rcp.selection2

import java.util.List

import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation
import org.eclipse.xtext.xtend2.lib.StringConcatenation
import org.junit.Test

import static junit.framework.Assert.*
import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation.*
import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionUtils.*
import static org.eclipse.recommenders.tests.jdt.AstUtils.*
import static org.eclipse.recommenders.tests.extdoc.rcp.selection2.XtendUtils.*

class JavaSelectionLocationTest {


	@Test
	def void testBeforePackageDeclaration () {
		val code = ''' 
		$pack$age org.$eclipse.recommenders.extdoc.rcp.selection2;$
		imp$ort List;
		class X{}
		'''
		val expected = newListWithFrequency(
			TYPE_DECLARATION -> 5
		) 
		exerciseAndVerify(code, expected);
	}
 
	
	@Test
	def void testPrimaryTypeDeclaration () {
		val code = '''$pu$blic$ $cl$ass$ $My$class$ $ex$tends$ $Supe$rclass$ $imp$lements$ $Interfac$e1$ {}'''
		val expected = newListWithFrequency(
			TYPE_DECLARATION -> 9,				// $pu$blic$ $cl$ass$ $My$class$
			TYPE_DECLARATION -> 3, 				// $ex$tends$ 
			TYPE_DECLARATION_EXTENDS -> 3,		// $Supe$rclass$
			TYPE_DECLARATION -> 3, 				// $imp$lements$ 
			TYPE_DECLARATION_IMPLEMENTS -> 3	// $Interfac$e1$
		)
		exerciseAndVerify(code, expected);
	}




	@Test
	def void testNestedTypeDeclaration () {
		val code = '''
		class Myclass {
			class MyClas$s2 impl$ements L$istener {
				public void run(){}
			};
		}'''
		val expected = newListWithFrequency(
			TYPE_DECLARATION-> 2,
			TYPE_DECLARATION_IMPLEMENTS -> 1
		)
		exerciseAndVerify(code, expected);
	}

	
	// REVIEW: Is this OK/the expected behavior?
	@Test
	def void testAnonymousTypeDeclarationInFieldInitializer () {
		val code = '''
		class Myclass {
			Class2 c = new L$istener(){
				public void run(){}
			};
			
		}'''
		val expected = newListWithFrequency(
			FIELD_DECLARATION_INITIALIZER -> 1 // new Listener
		)
		exerciseAndVerify(code, expected);
	}


	// REVIEW: Is this OK/the expected behavior?
	@Test
	def void testAnonymousInnerTypeDeclarationInMethodBody () {
		val code = '''
		class Myclass {
			void m(){
				Listener l = new L$istener(){
					public void run(){}
				};
			}
		}'''
		val expected = newListWithFrequency(
			METHOD_BODY -> 1
		)
		exerciseAndVerify(code, expected);
	}

	@Test
	def void testFieldDeclaration() {
	
	
		val code = '''
		class X {
			p$ublic stat$ic St$ring $  f$ield$ =$ $new$ St$ring("$")$.$toStri$ng($)$; $
		}'''
	
		val expected = newListWithFrequency(
			FIELD_DECLARATION -> 3, // p$ublic stat$ic St$ring
			FIELD_DECLARATION -> 1, // ..String $  field...
			FIELD_DECLARATION -> 2, // f$ield$
			FIELD_DECLARATION_INITIALIZER -> 1, // =$
			FIELD_DECLARATION_INITIALIZER -> 4, // $new$ St$ring("$")
			FIELD_DECLARATION_INITIALIZER -> 5, // $.$toStri$ng($)$
			TYPE_DECLARATION -> 1 // ; $
		)
		exerciseAndVerify(code, expected);
	}

	@Test
	def void testMethodDeclaration () {
		val code = '''
		class X {
			$pu$blic$ $Stri$ng$ $metho$d$($St$ring$ a$rg0$, S$tring $arg1) th$rows $IllegalA$rgumentEception$ {
			}
		}'''
		
		val expected = newListWithFrequency(
			METHOD_DECLARATION -> 3,			// $pu$blic$
			METHOD_DECLARATION_RETURN -> 3,		// $Stri$ng$
			METHOD_DECLARATION -> 3,			// $metho$d$
			METHOD_DECLARATION_PARAMETER -> 7,	// ($St$ring$ a$rg0$, S$tring $arg1)
			METHOD_DECLARATION -> 1,			//  th$rows
			METHOD_DECLARATION_THROWS -> 3		// $IllegalA$rgumentEception$
		)
		exerciseAndVerify(code, expected);
	}
	
	@Test
	def void testMethodBody() {
		val code = '''
		class X {
			
			 String method(String arg0) throws Exception {$
				S$tring $s$2 = arg0.to$String();
				if(s2.is$Empty()){
					// c$omment
					s2 = s2$.append("s"$)
				}$
			}
		}'''

		val expected = newListWithFrequency(
			METHOD_BODY -> 10
		)
		exerciseAndVerify(code, expected);
	}




	def private exerciseAndVerify(CharSequence code, List<JavaSelectionLocation> expected){
		
		val markers = createAstWithMarkers(code.toString)
		val cu = markers.first
		val pos = markers.second

		
		val actual = newArrayList
		for(position : pos){
			val selection = resolveSelectionLocationFromAst(cu, position)
			actual.add(selection)
		}
		assertEquals(expected, actual)
	}
}