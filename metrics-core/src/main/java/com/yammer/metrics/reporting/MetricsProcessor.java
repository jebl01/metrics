package com.yammer.metrics.reporting;

import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.TimerMetric;

public interface MetricsProcessor<T>
{
    public abstract void processMeter(Metered meter, T context) throws Exception;

    public abstract void processCounter(CounterMetric counter, T context) throws Exception;

    public abstract void processHistogram(HistogramMetric histogram, T context) throws Exception;

    public abstract void processTimer(TimerMetric timer, T context) throws Exception;

    public abstract void processGauge(GaugeMetric<?> gauge, T context) throws Exception;

}