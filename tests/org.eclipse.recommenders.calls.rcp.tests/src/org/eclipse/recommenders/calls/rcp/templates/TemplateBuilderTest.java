package org.eclipse.recommenders.calls.rcp.templates;

import static org.eclipse.recommenders.utils.names.VmTypeName.*;
import static org.junit.Assert.assertEquals;

import org.eclipse.recommenders.internal.calls.rcp.templates.TemplateBuilder;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Test;

public class TemplateBuilderTest {

    TemplateBuilder sut = new TemplateBuilder();

    @Test
    public void testNewVarnameMthd() {
        IMethodName mthd = VmMethodName.get("Ljava/lang/String.append()[[D");
        assertEquals("append", sut.suggestId(mthd));
        assertEquals("append1", sut.suggestId(mthd));
    }

    @Test
    public void testAppendCtor() {
        IMethodName ctor = VmMethodName.get("Ljava/lang/String.<init>()V");
        sut.appendCtor(ctor);
        String expected = "${stringType:newType(java.lang.String)} ${string:newName(java.lang.String)} = new ${stringType}();";
        assertEquals(expected, sut.toString());
    }

    @Test
    public void testAppendDoubleArrayRet() {
        VmMethodName mthd = VmMethodName.get("LC.m()[[D");
        sut.appendCall(mthd, "some");
        String expected = "${mType:newType('double[][]')} ${m:newName('double[][]')} = some.m();";
        assertEquals(expected, sut.toString());
    }

    @Test
    public void testAppendRefParam() {
        VmMethodName mthd = VmMethodName.get("LC.m(LObject;)V");
        sut.appendParameters(mthd, "obj");
        String expected = "${obj:var(Object)}";
        assertEquals(expected, sut.toString());
    }

    @Test
    public void testAppendRefArrayParam() {
        VmMethodName mthd = VmMethodName.get("LC.m([[LObject;)V");
        sut.appendParameters(mthd, "obj");
        String expected = "${obj:var('Object[][]')}";
        assertEquals(expected, sut.toString());
    }

    @Test
    public void testAppendIntParam() {
        VmMethodName mthd = VmMethodName.get("LC.m(I)V");
        sut.appendParameters(mthd, "i");
        String expected = "${i:link(0)}";
        assertEquals(expected, sut.toString());
    }

    @Test
    public void testAppendIntArrayParam() {
        VmMethodName mthd = VmMethodName.get("LC.m([I)V");
        sut.appendParameters(mthd, "i");
        String expected = "${i:var('int[]')}";
        assertEquals(expected, sut.toString());
    }

    @Test
    public void testAppendTwoArrayParams() {
        VmMethodName mthd = VmMethodName.get("LC.m([I[[Ll/Object;)V");
        sut.appendParameters(mthd, "i", "o");
        String expected = "${i:var('int[]')}, ${o:var('l.Object[][]')}";
        assertEquals(expected, sut.toString());
    }

    @Test
    public void testNewVarnameCtor() {
        IMethodName ctor = VmMethodName.get("Ljava/lang/String.<init>()V");
        assertEquals("string", sut.suggestId(ctor));
        assertEquals("string1", sut.suggestId(ctor));
    }

    @Test
    public void testNewVarnameAccessMthd() {
        IMethodName mthd = VmMethodName.get("Ljava/lang/String.access$1()D");

        assertEquals("access1", sut.suggestId(mthd));
        assertEquals("access11", sut.suggestId(mthd));
    }

    @Test
    public void testNewVarnameGetterMthd() {
        IMethodName mthd = VmMethodName.get("Ljava/lang/String.getString()[[D");
        assertEquals("string", sut.suggestId(mthd));
    }

    @Test
    public void testNewVarnameGetMthd() {
        IMethodName mthd = VmMethodName.get("Ljava/lang/String.get()[[D");
        assertEquals("get", sut.suggestId(mthd));
    }

    @Test
    public void testToLiteralDouble() {
        assertEquals("double", sut.toLiteral(DOUBLE));
    }

    @Test
    public void testToLiteralDoubleArray() {
        assertEquals("'double[][]'", sut.toLiteral(VmTypeName.get("[[D")));
    }

    @Test
    public void testToLiteralReferenceType() {
        assertEquals("java.lang.ExceptionInInitializerError", sut.toLiteral(JavaLangExceptionInInitializerError));
    }

    @Test
    public void testToLiteralReferenceTypeArray() {
        assertEquals("'java.lang.String[][]'", sut.toLiteral(VmTypeName.get("[[Ljava/lang/String")));
    }

    @Test
    public void testAppendStatement() {
        sut.appendCommand("myId", "cmdId", "string1", "string2", "string3");
        assertEquals("${myId:cmdId(string1,string2,string3)}", sut.toString());
    }

}
