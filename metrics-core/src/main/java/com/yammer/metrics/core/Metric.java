package com.yammer.metrics.core;

import java.io.IOException;

import com.yammer.metrics.reporting.AbstractPollingReporter;

/**
 * A tag interface to indicate that a class is a metric.
 */
public interface Metric {

    <T> void reportTo(AbstractPollingReporter<T> reporter, T context) throws IOException;
}
