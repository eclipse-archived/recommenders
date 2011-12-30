/**
 * Copyright (c) 2011 Sebastian Proksch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.tests.mining.extdoc.couch;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.eclipse.recommenders.mining.extdocs.couch.DocumentsByKey;
import org.eclipse.recommenders.webclient.results.SimpleView;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;

// TODO add PowerMock to 3rd party updatesite, to be able to mock final classes
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(Builder.class)
public class DocumentsByKeyTest {

    public class TestClass {
        public String value = "";
    }

    private GenericType<SimpleView<Test>> type = new GenericType<SimpleView<Test>>() {
    };

    @Mock
    private Builder builder;
    @Mock
    private WebResource resource;

    private DocumentsByKey<Test> uut;

    private ArgumentCaptor<MultivaluedMapImpl> paramCaptor;

    @Before
    public void setup() {
        when(builder.accept(any(MediaType.class))).thenReturn(builder);
        when(builder.type(any(MediaType.class))).thenReturn(builder);

        paramCaptor = forClass(MultivaluedMapImpl.class);
        when(resource.queryParams(paramCaptor.capture())).thenReturn(resource);
        when(resource.accept(any(MediaType.class))).thenReturn(builder);
        when(resource.type(any(MediaType.class))).thenReturn(builder);

        uut = new DocumentsByKey<Test>(resource, "a_key", type);
    }

    @Test
    @Ignore
    public void defaultAre10DocsPerRequest() {
        verifyLimit(10);
    }

    @Test
    @Ignore
    public void numberOfDocsPerRequestCanBeConfigured() {
        uut.setNumberOfDocumentsPerRequest(3);
        verifyLimit(3);
    }

    public void verifyLimit(Integer num) {
        when(builder.get(eq(type))).thenReturn(fullGet());

        uut.iterator().hasNext();

        List<String> actual = paramCaptor.getValue().get("limit");
        List<String> expected = new ArrayList<String>();
        expected.add(num.toString());

        assertEquals(expected, actual);
    }

    public static SimpleView<Test> fullGet() {
        SimpleView<Test> v = new SimpleView<Test>();
        v.rows = Lists.newArrayList();
        return v;
    }

    public static SimpleView<Test> incompleteGet() {
        SimpleView<Test> v = new SimpleView<Test>();
        v.rows = Lists.newArrayList();
        return v;
    }

    public static SimpleView<Test> emptyGet() {
        SimpleView<Test> v = new SimpleView<Test>();
        v.rows = Lists.newArrayList();
        return v;
    }
}