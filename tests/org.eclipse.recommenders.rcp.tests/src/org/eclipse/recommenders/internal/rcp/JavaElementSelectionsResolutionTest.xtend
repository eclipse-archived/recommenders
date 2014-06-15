/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp

import java.util.Collections
import java.util.List
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.IType
import org.eclipse.recommenders.testing.jdt.JavaProjectFixture
import org.junit.Ignore
import org.junit.Test

import static junit.framework.Assert.*
import static org.eclipse.recommenders.testing.XtendUtils.*

class JavaElementSelectionsResolutionTest {

    @Test
    def void testTypeSelectionInTypeDeclaration() {

        // note: this does not work since classpath cannot resolve this!
        val code = '''class Myc$lass {}'''

        val expected = newListWithFrequency(
            "LMyclass;" -> 1
        )
        exerciseAndVerify(code, expected);
    }

    @Test
    def void testTypeSelectionsInMethodBody() {
        val code = ''' 
        class Myclass {
        	void test(String s1){
        		Str$ing s = new St$ring("");
        	}
        }'''

        val expected = newListWithFrequency(
            "Ljava/lang/String;" -> 1,
            "Ljava/lang/String;.(Ljava/lang/String;)V" -> 1
        )
        exerciseAndVerify(code, expected);
    }

    @Test
    def void testTypeSelectionInExtends() {

        val code = '''
            import java.util.*;
            class Myclass123 extends L$ist {}
        '''

        val expected = newListWithFrequency(
            "Ljava/util/List<>;" -> 1
        )
        exerciseAndVerify(code, expected);
    }

    @Test
    def void testTypeSelectionInFieldDeclaration() {
        val code = '''
        class Myclass {
        	Str$ing s = new St$ring("");
        }'''

        val expected = newListWithFrequency(
            "Ljava/lang/String;" -> 1,
            "Ljava/lang/String;.(Ljava/lang/String;)V" -> 1
        )

        exerciseAndVerify(code, expected);
    }

    @Test
    def void testEmptySelectionInClassBody() {
        val code = '''
        class Myclass {
        	$
        }'''

        exerciseAndVerify(code, Collections::emptyList);
    }

    @Test
    def void testMethodSelectionInMethodBody() {
        val code = '''
        class Myclass {
        	void test(String s1){
        		String s2 = s1.co$ncat("hello");
        		s2.hashCode$();
        		s1.$equals(s2);
        	}
        	
        }'''

        val expected = newListWithFrequency(
            "Ljava/lang/String;.concat(Ljava/lang/String;)Ljava/lang/String;" -> 1,
            "Ljava/lang/String;.hashCode()I" -> 1,
            "Ljava/lang/String;.equals(Ljava/lang/Object;)Z" -> 1
        )

        exerciseAndVerify(code, expected);
    }

    @Test
    @Ignore("Only for debugging the ui")
    def void waitAlongTime() {
        Thread::sleep(120 * 1000)
    }

    def void exerciseAndVerify(CharSequence code, List<String> expected) {
        val fixture = new JavaProjectFixture(ResourcesPlugin::getWorkspace(), "test")
        val struct = fixture.createFileAndParseWithMarkers(code)
        val cu = struct.first;
        if (cu == null) {
            fail("cu is not allowed to be null!")
        }
        val pos = struct.second;
        val List<String> actual = newArrayList();
        for (position : pos) {
            val selection = JavaElementSelections.resolveJavaElementFromTypeRootInEditor(cu, position)
            if (selection.present) {
                val t = selection.get;
                switch t {
                    IType: actual.add(t.key)
                    IMethod: actual.add(t.key)
                }
            }
        }
        assertEquals(expected, actual)
    }

}
