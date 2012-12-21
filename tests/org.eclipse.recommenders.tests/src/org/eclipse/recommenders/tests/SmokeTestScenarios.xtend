package org.eclipse.recommenders.tests

import java.util.Arrays

import static org.eclipse.recommenders.tests.CodeBuilder.*

class SmokeTestScenarios {
	
	def static scenarios (){
		Arrays::asList(
			IMPORT_01, IMPORT_02, 
			PACKAGE_01, PACKAGE_02, PACKAGE_03,
			CLASSBODY_01, CLASSBODY_02, CLASSBODY_03, CLASSBODY_04, CLASSBODY_05,
			METHOD_STMT_01, METHOD_STMT_02, METHOD_STMT_03, METHOD_STMT_04, METHOD_STMT_05,METHOD_STMT_06,
			METHOD_STMT_07, METHOD_STMT_08, METHOD_STMT_09, METHOD_STMT_10, METHOD_STMT_11, METHOD_STMT_12,
			COMMENTS_01, COMMENTS_02
		)
	}
	
	
	public static CharSequence someClass = '''public class C {}'''
	
	public static CharSequence IMPORT_01='''
		$i$mport$ $java$.$uti$l.$
		«someClass»
		'''
	
	public static CharSequence IMPORT_02='''
		import $stat$ic$ $java$.$uti$l.Collection.$
		«someClass»
		'''

	public static CharSequence IMPORT_03='''
		$
		«someClass»
		'''
		
	public static CharSequence PACKAGE_01='''
		$
		«someClass»
		'''

	public static CharSequence PACKAGE_02='''
		pack$age $
		«someClass»
		'''

	public static CharSequence PACKAGE_03='''
		package org.$
		«someClass»
		'''
		
	public static CharSequence CLASSBODY_01 = classDeclaration(
		'''class ExtendingClass1 extends UnknownType''',
		'''$'''
	)
	
	public static CharSequence CLASSBODY_02 = classDeclaration(
		'''class ExtendingClass2 extends UnknownType''',
		'''siz$'''
	)
	
	public static CharSequence CLASSBODY_03 = classbody('''private UnknownType field = $''')
	
	public static CharSequence CLASSBODY_04 = classbody('''modifier Object o = $''')
	
	public static CharSequence CLASSBODY_05 = classbody('''public List = $''')
	
	public static CharSequence METHOD_STMT_01 = method('''Ob$;''')

	public static CharSequence METHOD_STMT_02 = method('''Object $''')
		
	public static CharSequence METHOD_STMT_03 = method('''Object $o$ = $''')

	public static CharSequence METHOD_STMT_04 = method('''Object o = new $''')

	public static CharSequence METHOD_STMT_05 = method('''
		Object o = "";
		o.$
		''')
	
	public static CharSequence METHOD_STMT_06 = classbody('''void <T> m(T t){
		t.$
		}''')
	
	public static CharSequence METHOD_STMT_07 = method('''UnknownType.$exit$($)''')
	
	public static CharSequence METHOD_STMT_08 = method('''UnknownType o = $new $File($to$String())''')
	
	public static CharSequence METHOD_STMT_09 = method('''
		UnknownType o = "";
		o.$''')
	
	public static CharSequence METHOD_STMT_10 = method('''undef$inedMethod($).$call($)''')
	
	public static CharSequence METHOD_STMT_11 = method('''java.util.Arrays.asList(get$)''')
	
	public static CharSequence METHOD_STMT_12 = method('''List<?> l = new java.util.ArrayList();
		l.$subList(0, 1).$''')
	
	public static CharSequence COMMENTS_01 = 
		'''
		/**
		 *$ Copyright (c) 2010, 2011 Darmstadt University of Technology.
		 * All rights reserved. This$ program and the accompanying materials
		 * are made available under the terms of the Eclipse Public License v1.0
		 * which accompanies this distribution, and is available at
		 * http://www.$eclipse.org/legal/epl-v10.html
		 *
		 * Contributors$:
		 *    Marcel Bruch $- initial API and implementation.
		 */
		package org.ecli$pse.recommenders.tests.comp$letion.rcp.calls$;$
		public class Comments01 {
			
		}
		'''
		
		
	public static CharSequence COMMENTS_02 = classbody(
		'''
		/**
		* $
		*/
		static {
		}''')
		
		
	def static OLD_TEST_CLASS(){
		'''
		/**
		 *$ Copyright (c) 2010, 2011 Darmstadt University of Technology.
		 * All rights reserved. This$ program and the accompanying materials
		 * are made available under the terms of the Eclipse Public License v1.0
		 * which accompanies this distribution, and is available at
		 * http://www.$eclipse.org/legal/epl-v10.html
		 *
		 * Contributors$:
		 *    Marcel Bruch $- initial API and implementation.
		 */
		package org.ecli$pse.recommenders.tests.comp$letion.rcp.calls$;$
		$
		im$port java.$util.*$;
		im$port $stati$c$ java.util.Collections.$;
		$
		/**
		 * Some $class comments {@link$plain $}
		 * 
		 * @see $
		 */
		public class AllJavaFeatures<T extends Collection> {
		
		    /**
		     * $
		     */
		    static {
		        S$et $s = new Has$hSet<St$ring>();
		        s$.$add("$");
		    }
		
		    /**
		     * $
		     * 
		     * @par$am $
		     */
		    pub$lic st$atic voi$d stat$ic1(fi$nal St$ring ar$g) {
		        ch$ar$ c$ = a$rg.$charAt($);
		        Str$ing $s $=$ "$"$;
		    }
		    
		    public void <T$> mT$ypeParameter(T$ s$) {
		        s.$;
		    }
		    
		    
		    priv$ate sta$tic cl$ass MyInne$rClass extend$s Obse$rvable{
		        
		        @Override
		        pub$lic synchro$nized vo$id addObs$erver(Observ$er $o) {
		        	o$
		        	;
		            // TO$DO A$uto-generated method stub
		            sup$er.addOb$server($o);
		            o.$
		        }
		    }
		}
	'''	
		
	}
}