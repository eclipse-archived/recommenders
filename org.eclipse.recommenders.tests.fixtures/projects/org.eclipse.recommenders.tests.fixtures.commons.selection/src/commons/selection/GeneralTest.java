package commons.selection/* PackageFragment | PackageDeclaration */;

import org.eclipse.swt.widgets.Button/* ResolvedBinaryType | ImportDeclaration */;

public class GeneralTest/* SourceType | TypeDeclaration */ {

    private static String/* ResolvedBinaryType | FieldDeclaration */ staticString/* SourceField | FieldDeclaration */;

    private String/* ResolvedBinaryType | FieldDeclaration */ objectString/* SourceField | FieldDeclaration */;

    public static void staticMethod/* SourceMethod | MethodDeclaration */() {
        final String/* ResolvedBinaryType | Block */ localFieldinStatic/* LocalVariable | Block */;
        staticString/* ResolvedSourceField | Block */.isEmpty/* ResolvedBinaryMethod | Block */();
    }

    public void objectMethod() {
        objectString/* ResolvedSourceField | Block */.isEmpty/* ResolvedBinaryMethod | Block */();
    }

    public Button/* ResolvedBinaryType | MethodDeclaration */ objectMethodWithReturn/* SourceMethod | MethodDeclaration */() {
        final Button/* ResolvedBinaryType | Block */ button/* LocalVariable | Block */ = new Button(null, 0);
        button./* null | Block */
        return button;
    }
}
