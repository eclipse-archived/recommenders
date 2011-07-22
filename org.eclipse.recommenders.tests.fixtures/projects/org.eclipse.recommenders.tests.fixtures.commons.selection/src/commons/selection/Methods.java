package commons.selection;

import org.eclipse.swt.widgets.Button;

public class Methods {
    
    private static String staticString;
    
    private String localString;

    public Methods/* SourceMethod | METHOD_DECLARATION | MethodDeclaration */(Composite/* ResolvedBinaryType | METHOD_DECLARATION_PARAMETER | SimpleType */ parent/* ResolvedBinaryType | METHOD_DECLARATION_PARAMETER | SimpleType */, int style) {
        super(parent, style);
    }

    public Button/* ResolvedBinaryType | METHOD_DECLARATION | SimpleType */ objectMethodWithReturn/* SourceMethod | METHOD_DECLARATION | MethodDeclaration */() {
        final Button/* ResolvedBinaryType | BLOCK | MethodDeclaration */ button/* LocalVariable | BLOCK | MethodDeclaration */ = new Button(null, 0);
        button./* null | BLOCK | MethodDeclaration */
        return button/* LocalVariable | BLOCK | MethodDeclaration */;
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection/* ResolvedBinaryType | METHOD_DECLARATION_PARAMETER | SimpleType */ selection/* ResolvedBinaryType | METHOD_DECLARATION_PARAMETER | SimpleType */) {
        localString/* ResolvedSourceField | BLOCK | MethodInvocation */.isEmpty/* ResolvedBinaryMethod | BLOCK | MethodInvocation */();
    }
    
    public static void staticMethod/* SourceMethod | METHOD_DECLARATION | MethodDeclaration */() {
        final String/* ResolvedBinaryType | BLOCK | SimpleType */ localFieldinStatic/* LocalVariable | BLOCK | VariableDeclarationFragment */;
        staticString/* ResolvedSourceField | BLOCK | MethodInvocation */.isEmpty/* ResolvedBinaryMethod | BLOCK | MethodInvocation */();
    }
}
