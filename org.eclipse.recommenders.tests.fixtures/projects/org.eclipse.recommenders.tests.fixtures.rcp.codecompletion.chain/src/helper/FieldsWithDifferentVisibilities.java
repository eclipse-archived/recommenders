package helper;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FieldsWithDifferentVisibilities {
	protected AtomicBoolean findMe1 = new AtomicBoolean();

    AtomicInteger findMe2 = new AtomicInteger();

    private final AtomicLong findMe3 = new AtomicLong();
}
