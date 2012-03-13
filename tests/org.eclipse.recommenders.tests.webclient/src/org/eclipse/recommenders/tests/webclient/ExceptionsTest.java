package org.eclipse.recommenders.tests.webclient;

import org.eclipse.recommenders.webclient.exceptions.ConflictException;
import org.eclipse.recommenders.webclient.exceptions.InvalidRequestException;
import org.eclipse.recommenders.webclient.exceptions.NotFoundException;
import org.eclipse.recommenders.webclient.exceptions.ServerCommunicationException;
import org.eclipse.recommenders.webclient.exceptions.ServerErrorException;
import org.eclipse.recommenders.webclient.exceptions.ServerUnreachableException;
import org.eclipse.recommenders.webclient.exceptions.UnauthorizedAccessException;
import org.junit.Test;

public class ExceptionsTest {

    @Test
    public void test() {
        new ConflictException(null);
        new InvalidRequestException(null);
        new NotFoundException(null);
        new ServerCommunicationException(null);
        new ServerCommunicationException("", null);
        new ServerErrorException(null);
        new ServerUnreachableException(null);
        new ServerUnreachableException("", null);
        new UnauthorizedAccessException(null);
    }

}
