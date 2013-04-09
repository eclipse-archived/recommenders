package org.eclipse.recommenders.tests.rcp.utils;

import static org.eclipse.recommenders.utils.names.VmTypeName.INT;
import static org.eclipse.recommenders.utils.names.VmTypeName.OBJECT;
import static org.eclipse.recommenders.utils.rcp.ast.ASTNodeUtils.sameSimpleName;
import static org.eclipse.recommenders.utils.rcp.ast.ASTNodeUtils.stripQualifier;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Test;

public class AstNodeUtilsTest {

    final String OBJECT_LITERAL = "Object";
    final AST ast = AST.newAST(AST.JLS4);

    @Test
    public void testStripQualifier() {
        assertEquals(OBJECT_LITERAL, stripQualifier(newSimpleName()).getIdentifier());
        assertEquals(OBJECT_LITERAL, stripQualifier(newQualifiedName()).getIdentifier());
    }

    /**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=405235
     */
    @Test
    public void testSameSimpleNameOnSimpleTypes() {
        // test Object simple type with simple name
        assertTrue(sameSimpleName(newSimpleType(newSimpleName()), OBJECT));
        // test Object simple type with qualified name
        assertTrue(sameSimpleName(newSimpleType(newQualifiedName()), OBJECT));
        // test Object qualified type
        assertTrue(sameSimpleName(newQualifiedType(), OBJECT));
        // test Object parameterized type
        assertTrue(sameSimpleName(newParameterizedType(), OBJECT));
        // test Object primitive type
        assertTrue(sameSimpleName(newPrimitiveIntType(), INT));
        // test Object array type
        assertTrue(sameSimpleName(newObjectArrayType(), VmTypeName.get("[[" + OBJECT)));
    }

    private ArrayType newObjectArrayType() {
        return ast.newArrayType(newQualifiedType(), 2);
    }

    private PrimitiveType newPrimitiveIntType() {
        return ast.newPrimitiveType(PrimitiveType.INT);
    }

    private ParameterizedType newParameterizedType() {
        return ast.newParameterizedType(newSimpleType(newQualifiedName()));
    }

    private SimpleType newSimpleType(Name name) {
        return ast.newSimpleType(name);
    }

    private QualifiedType newQualifiedType() {
        QualifiedType type = ast.newQualifiedType(newSimpleType(newSimpleName()), newSimpleName());
        return ast.newQualifiedType(type, newSimpleName());
    }

    private SimpleName newSimpleName() {
        return ast.newSimpleName(OBJECT_LITERAL);
    }

    private QualifiedName newQualifiedName() {
        return newQualifiedName(OBJECT_LITERAL);
    }

    private QualifiedName newQualifiedName(String last) {
        return ast.newQualifiedName(ast.newQualifiedName(ast.newSimpleName("java"), ast.newSimpleName("lang")),
                ast.newSimpleName(last));
    }

}
