package com.yammer.metrics.core;


public interface MetricsProcessor<T>
{
    public abstract void processMeter(Metered meter, T context) throws Exception;

    public abstract void processCounter(CounterMetric counter, T context) throws Exception;

    public abstract void processHistogram(HistogramMetric histogram, T context) throws Exception;

    public abstract void processTimer(TimerMetric timer, T context) throws Exception;

    public abstract void processGauge(GaugeMetric<?> gauge, T context) throws Exception;

}