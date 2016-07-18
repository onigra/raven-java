package com.getsentry.raven;

import com.getsentry.raven.event.Breadcrumb;
import com.getsentry.raven.util.CircularFifoQueue;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

/**
 * RavenContext is used to hold {@link ThreadLocal} context data (such as
 * {@link Breadcrumb}s) so that data may be collected in different parts
 * of an application and then sent together when an exception occurs.
 */
public class RavenContext implements Closeable {

    /**
     * Thread local set of active context objects. Note that an {@link IdentityHashMap}
     * is used instead of a Set because there is no identity-set in the Java
     * standard library.
     *
     * A set of active contexts is required in order to support running multiple Raven
     * clients within a single process. In *most* cases this set will contain a single
     * active context object.
     *
     * This must be static and {@link ThreadLocal} so that users can retrieve any active
     * context objects globally, without passing context objects all the way down their
     * stacks. See {@link com.getsentry.raven.event.Breadcrumbs} for an example of how this may be used.
     */
    private static ThreadLocal<IdentityHashMap<RavenContext, RavenContext>> activeContexts =
        new ThreadLocal<IdentityHashMap<RavenContext, RavenContext>>() {
            @Override
            protected IdentityHashMap<RavenContext, RavenContext> initialValue() {
                return new IdentityHashMap<RavenContext, RavenContext>();
            }
    };

    /**
     * The number of {@link Breadcrumb}s to keep in the ring buffer by default.
     */
    private static final int DEFAULT_BREADCRUMB_LIMIT = 100;

    /**
     * Ring buffer of {@link Breadcrumb} objects.
     */
    private CircularFifoQueue<Breadcrumb> breadcrumbs;

    /**
     * Create a new (empty) RavenContext object with the default Breadcrumb limit.
     */
    public RavenContext() {
        this(DEFAULT_BREADCRUMB_LIMIT);
    }

    /**
     * Create a new (empty) RavenContext object with the specified Breadcrumb limit.
     *
     * @param breadcrumbLimit Number of Breadcrumb objects to retain in ring buffer.
     */
    public RavenContext(int breadcrumbLimit) {
        breadcrumbs = new CircularFifoQueue<Breadcrumb>(breadcrumbLimit);
    }

    /**
     * Add this context to the active contexts for this thread.
     */
    public void activate() {
        activeContexts.get().put(this, this);
    }

    /**
     * Remove this context from the active contexts for this thread.
     */
    public void deactivate() {
        activeContexts.get().remove(this);
    }

    /**
     * Clear state from this context.
     */
    public void clear() {
        breadcrumbs.clear();
    }

    /**
     * Calls deactivate.
     */
    @Override
    public void close() {
        deactivate();
    }

    /**
     * Returns all active contexts for the current thread.
     *
     * @return List of active {@link RavenContext} objects.
     */
    public static List<RavenContext> getActiveContexts() {
        Collection<RavenContext> ravenContexts = activeContexts.get().values();
        List<RavenContext> list = new ArrayList<RavenContext>(ravenContexts.size());
        list.addAll(ravenContexts);
        return list;
    }

    /**
     * Return {@link Breadcrumb}s attached to this RavenContext.
     *
     * @return Iterator of {@link Breadcrumb}s.
     */
    public Iterator<Breadcrumb> getBreadcrumbs() {
        return breadcrumbs.iterator();
    }

    /**
     * Record a single {@link Breadcrumb} into this context.
     *
     * @param breadcrumb Breadcrumb object to record
     */
    public void recordBreadcrumb(Breadcrumb breadcrumb) {
        breadcrumbs.add(breadcrumb);
    }

}
