package com.yammer.metrics.reporting;

import java.io.Writer;
import java.util.Locale;

public final class GraphiteRendererContext
{
    public final String name;
    public final long epoch;
    public final Writer writer;
    public final Locale locale;
    public final String prefix;

    public GraphiteRendererContext(final String name, final String prefix, final long epoch, final Locale locale, final Writer writer)
    {
        this.name = name;
        this.prefix = prefix;
        this.epoch = epoch;
        this.locale = locale;
        this.writer = writer;
    }
}
