package org.eclipse.recommenders.tests

class CodeBuilder {
	
	public static CharSequence someClass = '''public class C {}'''
	
	def static classDeclaration(CharSequence declaration, CharSequence body) {
		'''
		import java.util.*;
		import java.util.concurrent.*;
		import java.text.*;
		import java.util.concurrent.*;
		import javax.annotation.*;
		«declaration» {
			«body»
		}
		'''
	}
	
	def static classbody(CharSequence classname, CharSequence classbody){
		'''
		import java.util.*;
		import java.util.concurrent.*;
		import java.text.*;
		import java.util.concurrent.*;
		import javax.annotation.*;
		public class «classname» {
			«classbody»
		}
		'''
	}
	
	def static classbody(CharSequence classbody){
		'''
		import java.util.*;
		import java.util.concurrent.*;
		import java.text.*;
		import java.util.concurrent.*;
		import javax.annotation.*;
		public class MyClass {
			«classbody»
		}
		'''
	}
	
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
	
	def static method(CharSequence methodbody){
		classbody('MyClass', '''
		public void __test() {
			«methodbody»
		}''')
	}

	def static classWithFieldsAndTestMethod(CharSequence fieldDeclarations, CharSequence methodbody){
		classbody('MyClass', '''
		
		«fieldDeclarations»
		
		public void __test() {
			«methodbody»
		}''')
	}
}