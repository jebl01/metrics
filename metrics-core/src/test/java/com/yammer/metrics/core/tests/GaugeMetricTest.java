package com.yammer.metrics.core.tests;

import java.io.IOException;

import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.reporting.AbstractPollingReporter;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GaugeMetricTest {
    final GaugeMetric<String> gauge = new GaugeMetric<String>() {
        @Override
        public String value() {
            return "woo";
        }

        @Override
        public <T> void reportTo(AbstractPollingReporter<T> reporter, T context) throws IOException {}
    };

    @Test
    public void returnsAValue() throws Exception {
        assertThat("a gauge returns a value",
                   gauge.value(),
                   is("woo"));
    }
}
