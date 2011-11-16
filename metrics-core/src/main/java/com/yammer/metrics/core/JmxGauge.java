package com.yammer.metrics.core;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.yammer.metrics.reporting.AbstractPollingReporter;

/**
 * A gauge which exposes an attribute of a JMX MBean.
 */
public class JmxGauge implements GaugeMetric<Object> {
    private static final MBeanServer SERVER = ManagementFactory.getPlatformMBeanServer();
    private ObjectName name;
    private String attribute;

    public JmxGauge(String name, String attribute) throws MalformedObjectNameException {
        this.name = new ObjectName(name);
        this.attribute = attribute;
    }

    @Override
    public Object value() {
        try {
            return SERVER.getAttribute(name, attribute);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void reportTo(final AbstractPollingReporter<T> reporter, final T context) throws IOException {
        reporter.report(this, context);
    }
}
