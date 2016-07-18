package com.getsentry.raven.jul;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.getsentry.raven.stub.SentryStub;

public class SentryHandlerIT {
    private static final Logger logger = Logger.getLogger(SentryHandlerIT.class.getName());
    private SentryStub sentryStub;

    @BeforeMethod
    public void setUp() throws Exception {
        sentryStub = new SentryStub();
        sentryStub.removeEvents();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        sentryStub.removeEvents();
    }

    @Test
    public void testInfoLog() throws Exception {
        assertThat(sentryStub.getEventCount(), is(0));
        logger.info("This is a test");
        assertThat(sentryStub.getEventCount(), is(1));
    }

    @Test
    public void testChainedExceptions() throws Exception {
        assertThat(sentryStub.getEventCount(), is(0));
        logger.log(Level.SEVERE, "This is an exception",
                new UnsupportedOperationException("Test", new UnsupportedOperationException()));
        assertThat(sentryStub.getEventCount(), is(1));
    }
}
