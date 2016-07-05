package org.eclipse.recommenders.internal.models;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.junit.Test;

public class AetherUtilsTest {

    @Test
    public void testCreateRemoteRepositoryWithoutUserinfo() {
        RemoteRepository repository = AetherUtils.createRemoteRepository("remote", "http://example.org/path/");

        assertThat(repository.getId(), is(equalTo("remote")));
        assertThat(repository.getAuthentication(), is(nullValue()));
    }

    @Test
    public void testCreateRemoteRepositoryWithUsername() {
        RemoteRepository repository = AetherUtils.createRemoteRepository("remote", "http://username@example.org/path/");

        assertThat(repository.getId(), is(equalTo("remote")));
        assertThat(repository.getAuthentication(), is(equalTo(createAuthentication("username", null))));
    }

    @Test
    public void testCreateRemoteRepositoryWithEscapedUsername() {
        RemoteRepository repository = AetherUtils.createRemoteRepository("remote",
                "http://u%25e%3An%40me@example.org/path/");

        assertThat(repository.getId(), is(equalTo("remote")));
        assertThat(repository.getAuthentication(), is(equalTo(createAuthentication("u%e:n@me", null))));
    }

    @Test
    public void testCreateRemoteRepositoryWithUsernameAndEmptyPassword() {
        RemoteRepository repository = AetherUtils.createRemoteRepository("remote",
                "http://username:@example.org/path/");

        assertThat(repository.getId(), is(equalTo("remote")));
        assertThat(repository.getAuthentication(), is(equalTo(createAuthentication("username", null))));
    }

    @Test
    public void testCreateRemoteRepositoryWithUsernameAndPassword() {
        RemoteRepository repository = AetherUtils.createRemoteRepository("remote",
                "http://username:password@example.org/path/");

        assertThat(repository.getId(), is(equalTo("remote")));
        assertThat(repository.getAuthentication(), is(equalTo(createAuthentication("username", "password"))));
    }

    @Test
    public void testCreateRemoteRepositoryWithEncodedUsernameAndPassword() {
        RemoteRepository repository = AetherUtils.createRemoteRepository("remote",
                "http://u%25e%3An%40me:p%40%25%25word@example.org/path/");

        assertThat(repository.getId(), is(equalTo("remote")));
        assertThat(repository.getAuthentication(), is(equalTo(createAuthentication("u%e:n@me", "p@%%word"))));
    }

    private Authentication createAuthentication(String username, String password) {
        return new AuthenticationBuilder().addUsername(username).addPassword(password).build();
    }
}
