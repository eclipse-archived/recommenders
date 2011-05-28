package commons.selection/* PackageFragment | PACKAGE_DECLARATION | QualifiedName */;

import org.eclipse.jface.viewers.ISelection/* ResolvedBinaryType | IMPORT_DECLARATION | QualifiedName */;

public class GeneralTest/* SourceType | TYPE_DECLARATION | TypeDeclaration */ extends Button/* ResolvedBinaryType | TYPE_DECLARATION_EXTENDS | SimpleType */ implements ISelectionListener/* ResolvedBinaryType | TYPE_DECLARATION_IMPLEMENTS | SimpleType */ {

    private static String/* ResolvedBinaryType | FIELD_DECLARATION | SimpleType */ staticString/* SourceField | FIELD_DECLARATION | VariableDeclarationFragment */;

    private String/* ResolvedBinaryType | FIELD_DECLARATION | SimpleType */ objectString/* SourceField | FIELD_DECLARATION | VariableDeclarationFragment */;

    public GeneralTest(Composite parent, int style) {
        super(parent, style);
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection/* ResolvedBinaryType | METHOD_DECLARATION_PARAMETER | SimpleType */ selection/* ResolvedBinaryType | METHOD_DECLARATION_PARAMETER | SimpleType */) {
        objectString/* ResolvedSourceField | BLOCK | MethodInvocation */.isEmpty/* ResolvedBinaryMethod | BLOCK | MethodInvocation */();
    }

    public Button/* ResolvedBinaryType | METHOD_DECLARATION | SimpleType */ objectMethodWithReturn/* SourceMethod | METHOD_DECLARATION | MethodDeclaration */() {
        final Button/* ResolvedBinaryType | BLOCK | MethodDeclaration */ button/* LocalVariable | BLOCK | MethodDeclaration */ = new Button(null, 0);
        button./* null | BLOCK | MethodDeclaration */
        return button/* LocalVariable | BLOCK | MethodDeclaration */;
    }
    
    public static void staticMethod/* SourceMethod | METHOD_DECLARATION | MethodDeclaration */() {
        final String/* ResolvedBinaryType | BLOCK | SimpleType */ localFieldinStatic/* LocalVariable | BLOCK | VariableDeclarationFragment */;
        staticString/* ResolvedSourceField | BLOCK | MethodInvocation */.isEmpty/* ResolvedBinaryMethod | BLOCK | MethodInvocation */();
    }
}
