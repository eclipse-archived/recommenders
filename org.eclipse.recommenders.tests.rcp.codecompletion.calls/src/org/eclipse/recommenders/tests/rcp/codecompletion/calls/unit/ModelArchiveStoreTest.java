/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.rcp.codecompletion.calls.unit;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Set;

import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.db.CallsModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.db.Manifest;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.db.ModelArchive;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ModelArchiveStoreTest {

    private ArrayList<ITypeName> types;

    @Before
    public void setup() {
        types = new ArrayList<ITypeName>();
        types.add(VmTypeName.get("Lorg/eclipse/swt/widgets/Button"));
        types.add(VmTypeName.get("Ljava/awt/Button"));
        types.add(VmTypeName.get("Ljava/awt/TextField"));
    }

    @Test
    public void testIndexing() {
        final CallsModelArchiveStore store = new CallsModelArchiveStore();
        final ModelArchive archive = mockArchive();
        store.offer(archive);

        Assert.assertTrue(store.hasModel(types.get(0)));
        final IObjectMethodCallsNet model = store.getModel(types.get(0));
        Assert.assertNotNull(model);
        Assert.assertEquals(types.get(0), model.getType());
    }

    @Test
    public void testTypeUnknown() {
        final CallsModelArchiveStore store = new CallsModelArchiveStore();
        final ModelArchive archive = mockArchive();
        store.offer(archive);

        Assert.assertFalse(store.hasModel(VmTypeName.get("Lorg/eclipse/swt/widgets/Text")));
    }

    @Test
    public void testSimpleTypeIndexing() {
        final CallsModelArchiveStore store = new CallsModelArchiveStore();
        final ModelArchive archive = mockArchive();
        store.offer(archive);

        final VmTypeName simpleNameButton = VmTypeName.get("LButton");
        final Set<IObjectMethodCallsNet> models = store.getModelsForSimpleName(simpleNameButton);

        Assert.assertEquals(2, models.size());
    }

    @Test
    public void testSimpleTypeUnknown() {
        final CallsModelArchiveStore store = new CallsModelArchiveStore();
        final ModelArchive archive = mockArchive();
        store.offer(archive);

        final VmTypeName simpleNameText = VmTypeName.get("LText");
        final Set<IObjectMethodCallsNet> models = store.getModelsForSimpleName(simpleNameText);

        Assert.assertEquals(0, models.size());
    }

    private ModelArchive mockArchive() {
        final ModelArchive archive = Mockito.mock(ModelArchive.class);
        Mockito.when(archive.getManifest()).thenReturn(mockManifest());
        Mockito.when(archive.getTypes()).thenReturn(types);
        for (final ITypeName type : types) {
            final IObjectMethodCallsNet mockCallsNet = mockCallsNet(type);
            Mockito.when(archive.load(type)).thenReturn(mockCallsNet);
        }
        return archive;
    }

    private IObjectMethodCallsNet mockCallsNet(final ITypeName type) {
        final IObjectMethodCallsNet callsNet = Mockito.mock(IObjectMethodCallsNet.class);
        Mockito.when(callsNet.getType()).thenReturn(type);
        return callsNet;
    }

    private Manifest mockManifest() {
        final GregorianCalendar calendar = new GregorianCalendar(2011, 5, 1);
        final Manifest manifest = new Manifest("test-model", Version.create(0, 1), calendar.getTime());
        return manifest;
    }
}
