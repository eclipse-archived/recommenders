package org.eclipse.recommenders.internal.models.rcp;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static org.eclipse.core.net.proxy.IProxyData.HTTP_PROXY_TYPE;
import static org.eclipse.recommenders.utils.Constants.EXT_ZIP;
import static org.eclipse.recommenders.utils.Pair.newPair;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ModelRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class EclipseModelRepositoryTest {

    private static final IProxyData[] NO_PROXY = new IProxyData[0];
    private static final int PROXY_PORT = 80;
    private static final String PROXY_HOST = "proxy.example.net";
    private static final String FIRST_REPO = "http://www.example.org/repo1";
    private static final String SECOND_REPO = "http://www.example.com/repo2";
    private static final String THIRD_REPO = "http://www.example.net/repo3";

    private static final ModelCoordinate COORDINATE = new ModelCoordinate("org.example", "artifact", "model", EXT_ZIP,
            "1.0.0");

    private static final File FIRST_MODEL_ZIP = new File("artifact-1.0.0-1-model.zip");
    private static final File SECOND_MODEL_ZIP = new File("artifact-1.0.0-1-model.zip");

    private static final ImmutableMap<ModelCoordinate, File> EMPTY_REPOSITORY = ImmutableMap.of();

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Mock
    private EventBus bus;

    @Mock
    private IProxyService proxy;

    @Mock
    private ModelsRcpPreferences prefs;

    @Test
    public void testResolvePicksFirstMatch() {
        ModelRepository firstRepo = mockModelRepository(of(COORDINATE, FIRST_MODEL_ZIP));
        ModelRepository secondRepo = mockModelRepository(of(COORDINATE, SECOND_MODEL_ZIP));

        when(prefs.getServerUsername(anyString())).thenReturn(Optional.<String>absent());

        EclipseModelRepository sut = new EclipseModelRepository(temp.getRoot(), proxy, prefs, bus);
        sut.openInternal(asList(newPair(FIRST_REPO, firstRepo), newPair(SECOND_REPO, secondRepo)));
        Optional<File> result = sut.resolve(COORDINATE, true);
        sut.close();

        assertThat(result.get(), is(equalTo(FIRST_MODEL_ZIP)));
    }

    @Test
    public void testResolveSearchesThroughAllRepositories() {
        ModelRepository firstRepo = mockModelRepository(EMPTY_REPOSITORY);
        ModelRepository secondRepo = mockModelRepository(of(COORDINATE, SECOND_MODEL_ZIP));

        when(prefs.getServerUsername(anyString())).thenReturn(Optional.<String>absent());

        EclipseModelRepository sut = new EclipseModelRepository(temp.getRoot(), proxy, prefs, bus);
        sut.openInternal(asList(newPair(FIRST_REPO, firstRepo), newPair(SECOND_REPO, secondRepo)));
        Optional<File> result = sut.resolve(COORDINATE, true);
        sut.close();

        assertThat(result.get(), is(equalTo(SECOND_MODEL_ZIP)));
    }

    /**
     * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=441558">Bug 441558</a>
     */
    @Test
    public void testResolveUpdatesProxySettings() throws Exception {
        ModelRepository firstRepo = mockModelRepository(EMPTY_REPOSITORY);
        ModelRepository secondRepo = mockModelRepository(EMPTY_REPOSITORY);
        ModelRepository thirdRepo = mockModelRepository(EMPTY_REPOSITORY);

        when(prefs.getServerUsername(anyString())).thenReturn(Optional.<String>absent());

        when(proxy.isProxiesEnabled()).thenReturn(true);
        when(proxy.select(new URI(FIRST_REPO))).thenReturn(NO_PROXY);
        IProxyData[] proxyData = mockProxyData(HTTP_PROXY_TYPE, PROXY_HOST, PROXY_PORT, null, null);
        when(proxy.select(new URI(SECOND_REPO))).thenReturn(proxyData);
        when(proxy.select(new URI(THIRD_REPO))).thenReturn(NO_PROXY);

        EclipseModelRepository sut = new EclipseModelRepository(temp.getRoot(), proxy, prefs, bus);
        sut.openInternal(asList(newPair(FIRST_REPO, firstRepo), newPair(SECOND_REPO, secondRepo),
                newPair(THIRD_REPO, thirdRepo)));
        Optional<File> result = sut.resolve(COORDINATE, true);
        sut.close();

        verify(firstRepo).unsetProxy();
        verify(secondRepo).setProxy("http", PROXY_HOST, PROXY_PORT, null, null);
        verify(thirdRepo).unsetProxy();

        assertThat(result.isPresent(), is(false));
    }

    private ModelRepository mockModelRepository(Map<ModelCoordinate, File> contents) {
        ModelRepository repo = Mockito.mock(ModelRepository.class);
        when(repo.resolve(Mockito.any(ModelCoordinate.class), anyBoolean())).thenReturn(Optional.<File>absent());
        for (Entry<ModelCoordinate, File> entry : contents.entrySet()) {
            ModelCoordinate coordinate = entry.getKey();
            File file = entry.getValue();
            when(repo.resolve(Mockito.eq(coordinate), anyBoolean())).thenReturn(Optional.of(file));
        }
        return repo;
    }

    private IProxyData[] mockProxyData(String type, String host, int port, String userId, String password) {
        IProxyData proxyData = mock(IProxyData.class);
        when(proxyData.getType()).thenReturn(type);
        when(proxyData.getHost()).thenReturn(host);
        when(proxyData.getPort()).thenReturn(port);
        when(proxyData.getUserId()).thenReturn(userId);
        when(proxyData.getPassword()).thenReturn(password);
        return new IProxyData[] { proxyData };
    }
}
