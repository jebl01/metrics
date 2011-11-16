package com.yammer.metrics.core;

import com.yammer.metrics.reporting.AbstractPollingReporter;

/**
 * A gauge metric is an instantaneous reading of a particular value. To
 * instrument a queue's depth, for example:<br>
 * <pre><code>
 * Queue<String> queue = new ConcurrentLinkedQueue<String>();
 * GaugeMetric<Integer> queueDepth = new GaugeMetric<Integer>() {
 *     public Integer value() {
 *         return queue.size();
 *     }
 * };
 *
 * </code></pre>
 * @param <T> the type of the metric's value
 */
public abstract class AbstractGaugeMetric<T> implements GaugeMetric<T> {
    
    @Override
    public <U> void reportTo(final AbstractPollingReporter<U> reporter, final U context) throws java.io.IOException {
        reporter.report(this, context);
    }
}
