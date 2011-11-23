package com.yammer.metrics.reporting;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.TimerMetric;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.util.MetricPredicate;

public class GraphiteReporterTest
{
    private MetricsRegistry registry;
    private GraphiteReporter reporter;
    private OutputStream out;

    @Before
    public void init() throws Exception
    {
        this.registry = new MetricsRegistry();
        this.out = new ByteArrayOutputStream();

        final Clock clock = mock(Clock.class);
        when(clock.time()).thenReturn(123456L);

        final Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(this.out);

        final SocketProvider provider = mock(SocketProvider.class);
        when(provider.get()).thenReturn(socket);

        this.reporter = new GraphiteReporter(this.registry, "prefix", MetricPredicate.ALL, provider, clock);
        this.reporter.printVMMetrics = false;
    }

    @Test
    public void canRenderCounter() throws Exception
    {
        final String expected = "prefix.java.lang.Object.test.count 11 123\n";

        CounterMetric metric = this.registry.newCounter(Object.class, "test");
        metric.inc(11);
        assertOutput(expected);
    }

    @Test
    public void canRenderCustomCounter()
    {
        String expected = "counter.test.11";

        CounterMetric metric = this.registry.newCounter(getClass(), "test");
        metric.inc(11);

        this.reporter.registerRenderer(CounterMetric.class, new GraphiteMetricRenderer<CounterMetric>()
        {
            @Override
            public void renderMetric(CounterMetric counter, GraphiteRendererContext context)
            {
                sendToGraphite("counter.test." + counter.count(), context);
            }
        });
        assertOutput(expected);
    }

    @Test
    public void canRenderHistogram() throws Exception
    {
        final String expected = new StringBuilder()
                .append("prefix.java.lang.Object.test.min 10.00 123\n")
                .append("prefix.java.lang.Object.test.max 10.00 123\n")
                .append("prefix.java.lang.Object.test.mean 10.00 123\n")
                .append("prefix.java.lang.Object.test.stddev 0.00 123\n")
                .append("prefix.java.lang.Object.test.median 10.00 123\n")
                .append("prefix.java.lang.Object.test.75percentile 10.00 123\n")
                .append("prefix.java.lang.Object.test.95percentile 10.00 123\n")
                .append("prefix.java.lang.Object.test.98percentile 10.00 123\n")
                .append("prefix.java.lang.Object.test.99percentile 10.00 123\n")
                .append("prefix.java.lang.Object.test.999percentile 10.00 123\n")
                .toString();

        HistogramMetric metric = this.registry.newHistogram(Object.class, "test");
        metric.update(10);

        assertOutput(expected);
    }

    @Test
    public void canRenderCustomHistogram()
    {
        String expected = "histogram.test.1";

        HistogramMetric metric = this.registry.newHistogram(getClass(), "test");
        metric.update(10);

        this.reporter.registerRenderer(HistogramMetric.class, new GraphiteMetricRenderer<HistogramMetric>()
        {
            @Override
            public void renderMetric(HistogramMetric histogram, GraphiteRendererContext context)
            {
                sendToGraphite("histogram.test." + histogram.count(), context);
            }
        });

        assertOutput(expected);
    }

    @Test
    public void canRendererTimed() throws Exception
    {
        final String expected = new StringBuilder()
                .append("prefix.java.lang.Object.testevent.test.count 0 123\n")
                .append("prefix.java.lang.Object.testevent.test.meanRate 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.1MinuteRate 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.5MinuteRate 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.15MinuteRate 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.min 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.max 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.mean 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.stddev 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.median 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.75percentile 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.95percentile 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.98percentile 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.99percentile 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.999percentile 0.00 123\n")
                .toString();

        this.registry.newTimer(Object.class, "test", "testevent");

        assertOutput(expected);
    }

    @Test
    public void canRendererCustomTimed()
    {
        StringBuilder expected = new StringBuilder();

        expected.append("metered.test.1\n");
        expected.append("timer.test.1\n");

        TimerMetric metric = this.registry.newTimer(getClass(), "test", "testevent");
        metric.update(12, TimeUnit.MILLISECONDS);

        this.reporter.registerRenderer(MeterMetric.class, new GraphiteMetricRenderer<Metered>()
        {
            @Override
            public void renderMetric(Metered meter, GraphiteRendererContext context)
            {
                sendToGraphite("metered.test." + meter.count() + "\n", context);
            }
        });
        this.reporter.registerRenderer(TimerMetric.class, new GraphiteMetricRenderer<TimerMetric>()
        {
            @Override
            public void renderMetric(TimerMetric timer, GraphiteRendererContext context)
            {
                sendToGraphite("timer.test." + timer.count() + "\n", context);
            }
        });

        assertOutput(expected.toString());
    }

    @Test
    public void canRendererMetered() throws Exception
    {
        final String expected = new StringBuilder()
                .append("prefix.java.lang.Object.test.count 0 123\n")
                .append("prefix.java.lang.Object.test.meanRate 0.00 123\n")
                .append("prefix.java.lang.Object.test.1MinuteRate 0.00 123\n")
                .append("prefix.java.lang.Object.test.5MinuteRate 0.00 123\n")
                .append("prefix.java.lang.Object.test.15MinuteRate 0.00 123\n")
                .toString();

        this.registry.newMeter(Object.class, "test", "testevent", TimeUnit.SECONDS);

        assertOutput(expected);
    }

    @Test
    public void canRendererCustomMetered()
    {
        String expected = "metered.test.12";

        MeterMetric metric = this.registry.newMeter(getClass(), "test", "testevent", TimeUnit.SECONDS);
        metric.mark(12);

        this.reporter.registerRenderer(MeterMetric.class, new GraphiteMetricRenderer<Metered>()
        {
            @Override
            public void renderMetric(Metered metered, GraphiteRendererContext context)
            {
                sendToGraphite("metered.test." + metered.count(), context);
            }
        });

        assertOutput(expected);
    }

    @Test
    public void canRendererGauge() throws Exception
    {
        final String expected = "prefix.java.lang.Object.test.value 5 123\n";

        this.registry.newGauge(Object.class, "test", new GaugeMetric<Long>()
        {
            @Override
            public Long value()
            {
                return 5l;
            }
        });

        assertOutput(expected);
    }

    @Test
    public void canRenderCustomGauge()
    {
        String expected = "gauge.test.5";

        this.registry.newGauge(getClass(), "test", new GaugeMetric<Long>()
        {
            @Override
            public Long value()
            {
                return 5l;
            }
        });

        this.reporter.registerRenderer(GaugeMetric.class, new GraphiteMetricRenderer<GaugeMetric<?>>()
        {
            @Override
            public void renderMetric(GaugeMetric<?> metric, GraphiteRendererContext context)
            {
                sendToGraphite("gauge.test." + metric.value().toString(), context);
            }
        });

        assertOutput(expected);
    }

    @Test
    public void canRenderCustomVMMetrics()
    {
        String expected = "prefix.jvm.daemon_thread_count 1.77 123\n";

        this.reporter.printVMMetrics = true;

        this.reporter.registerRenderer(VirtualMachineMetrics.class, new GraphiteMetricRenderer<VirtualMachineMetrics>()
        {
            @Override
            public void renderMetric(VirtualMachineMetrics metric, GraphiteRendererContext context)
            {
                printDoubleField("jvm.daemon_thread_count", 1.76767d, context);
            }
        });
        assertOutput(expected);
    }

    private void assertOutput(String expected)
    {
        this.reporter.run();
        assertEquals(expected, this.out.toString());
    }
}
