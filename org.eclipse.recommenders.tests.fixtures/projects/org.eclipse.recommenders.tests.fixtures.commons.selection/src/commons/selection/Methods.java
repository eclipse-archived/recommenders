package commons.selection;

import org.eclipse.swt.widgets.Button;

public class Methods {
    
    private static String staticString;
    
    private String localString;

    public Methods/* SourceMethod | Method Declaration | MethodDeclaration */(Composite/* ResolvedBinaryType | Parameter Declaration | SimpleType */ parent/* ResolvedBinaryType | Parameter Declaration | SimpleType */, int style) {
        super(parent, style);
    }

    public Button/* ResolvedBinaryType | Method Declaration | SimpleType */ objectMethodWithReturn/* SourceMethod | Method Declaration | MethodDeclaration */() {
        final Button/* ResolvedBinaryType | Method Body | MethodDeclaration */ button/* LocalVariable | Method Body | MethodDeclaration */ = new Button(null, 0);
        button./* SourceMethod | Method Body | MethodDeclaration */
        return button/* LocalVariable | Method Body | MethodDeclaration */;
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection/* ResolvedBinaryType | Parameter Declaration | SimpleType */ selection/* ResolvedBinaryType | Parameter Declaration | SimpleType */) {
        localString/* ResolvedSourceField | Method Body | MethodInvocation */.isEmpty/* ResolvedBinaryMethod | Method Body | MethodInvocation */();
    }
    
    public static void staticMethod/* SourceMethod | Method Declaration | MethodDeclaration */() {
        final String/* ResolvedBinaryType | Method Body | SimpleType */ localFieldinStatic/* LocalVariable | Method Body | VariableDeclarationFragment */;
        staticString/* ResolvedSourceField | Method Body | MethodInvocation */.isEmpty/* ResolvedBinaryMethod | Method Body | MethodInvocation */();
    }
}
