package org.eclipse.recommenders.tests.rcp.models;

import static com.google.common.base.Optional.absent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ArchiveModelUpdatePolicy;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus;
import org.junit.Test;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.common.collect.Lists;

public class MetadataTest implements PropertyChangeListener {

    private ModelArchiveMetadata sut = new ModelArchiveMetadata();

    private List<PropertyChangeEvent> events = Lists.newArrayList();

    @Test
    public void testGetters() throws IntrospectionException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        BeanInfo info = Introspector.getBeanInfo(ModelArchiveMetadata.class);
        for (PropertyDescriptor d : info.getPropertyDescriptors()) {
            Method m = d.getReadMethod();
            m.invoke(sut);
        }

    }

    @Test
    public void testListener() throws IntrospectionException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        int count = 0;

        sut.addPropertyChangeListener(ModelArchiveMetadata.P_COORDINATE, this);
        sut.setCoordinate("org:org:2");
        assertEquals(++count, events.size());

        sut.addPropertyChangeListener(ModelArchiveMetadata.P_ERROR, this);
        sut.setError("error");
        assertEquals(++count, events.size());

        sut.addPropertyChangeListener(ModelArchiveMetadata.P_MODEL, this);
        sut.setModelArchive(null);
        assertEquals(++count, events.size());

        sut.addPropertyChangeListener(ModelArchiveMetadata.P_STATUS, this);
        sut.setStatus(ModelArchiveResolutionStatus.FAILED);
        assertEquals(++count, events.size());

        sut.addPropertyChangeListener(ModelArchiveMetadata.P_LOCATION, this);
        sut.setLocation(null);
        assertEquals(++count, events.size());

        // smoke

        sut.setArtifact(new DefaultArtifact("org:org:1"));
        assertNotNull(sut.getArtifact());

        sut.setArtifact(null);
        sut.setCoordinate(null);
        count++;

        assertEquals(absent(), sut.getArtifact());
        sut.toString();

        sut.removePropertyChangeListener(this);
        sut.setStatus(ModelArchiveResolutionStatus.FAILED);
        sut.setStatus(ModelArchiveResolutionStatus.FAILED);
        sut.setStatus(ModelArchiveResolutionStatus.FAILED);
        assertEquals(count, events.size());

        ArchiveModelUpdatePolicy.values();
        ModelArchiveResolutionStatus.values();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        events.add(evt);
    }

}
