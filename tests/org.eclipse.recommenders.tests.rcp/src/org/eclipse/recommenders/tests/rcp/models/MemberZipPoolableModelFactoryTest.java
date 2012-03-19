package org.eclipse.recommenders.tests.rcp.models;

import static com.google.common.base.Optional.of;
import static org.eclipse.recommenders.utils.names.VmTypeName.BOOLEAN;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.internal.rcp.models.archive.MemberGsonZipPoolableModelFactory;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.junit.Test;

public class MemberZipPoolableModelFactoryTest {

    private ZipFile zip;
    private JavaElementResolver jdtResolver;
    private IType KEY_TYPE;
    private IMethod KEY_METHOD;
    private IField KEY_FIELD;

    @Test
    public void testZipModelFactory() throws Exception {
        KEY_TYPE = mock(IType.class);
        when(KEY_TYPE.getElementType()).thenReturn(IJavaElement.TYPE);

        KEY_METHOD = mock(IMethod.class);
        when(KEY_METHOD.getElementType()).thenReturn(IJavaElement.METHOD);

        KEY_FIELD = mock(IField.class);
        when(KEY_FIELD.getElementType()).thenReturn(IJavaElement.FIELD);

        jdtResolver = mock(JavaElementResolver.class);

        zip = mock(ZipFile.class);
        when(zip.getInputStream(any(ZipEntry.class))).thenReturn(new ByteArrayInputStream("hello".getBytes()));

        when(jdtResolver.toRecType(any(IType.class))).thenReturn(BOOLEAN, VmTypeName.FLOAT);
        when(jdtResolver.toRecMethod(KEY_METHOD)).thenReturn(of(VmMethodName.NULL));
        when(zip.getEntry("Z.json")).thenReturn(new ZipEntry("Z.json"));

        MemberGsonZipPoolableModelFactory<String> sut = new MemberGsonZipPoolableModelFactory<String>(zip,
                String.class, jdtResolver);

        sut.open();
        sut.hasModel(KEY_TYPE);
        sut.createModel(KEY_TYPE);

        sut.hasModel(KEY_METHOD);
        sut.createModel(KEY_METHOD);

        sut.hasModel(KEY_FIELD);
        sut.createModel(KEY_FIELD);

        // sut.activateModel(KEY1, MODEL);
        // sut.destroyModel(KEY1, MODEL);
        // sut.passivateModel(KEY1, MODEL);
        // sut.validateModel(KEY1, MODEL);

        sut.close();
        verify(zip).close();
        doThrow(new IOException()).when(zip).close();
        sut.close();
    }
}
