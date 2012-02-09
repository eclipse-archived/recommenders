package org.eclipse.recommenders.tests

import java.util.Arrays
import static org.eclipse.recommenders.tests.CodeBuilder.*

class SmokeTestScenarios {
	
	def static scenarios (){
		Arrays::asList(
			IMPORT_01, IMPORT_02, 
			PACKAGE_01, PACKAGE_02, PACKAGE_03,
			METHOD_STMT_01, METHOD_STMT_02, METHOD_STMT_03, METHOD_STMT_04, METHOD_STMT_05,METHOD_STMT_06,
			OLD_TEST_CLASS
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