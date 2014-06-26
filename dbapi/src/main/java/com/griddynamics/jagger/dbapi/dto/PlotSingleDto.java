package com.griddynamics.jagger.dbapi.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Represents single line on plot.
 * Refers to MetricNameDto
 *
 * @author "Artem Kirillov" (akirillov@griddynamics.com)
 * @since 5/31/12
 */
public class PlotSingleDto implements Serializable {
    private List<PointDto> plotData = Collections.emptyList();
    private String legend;
    private String color;

    public PlotSingleDto() {
    }

    public PlotSingleDto(List<PointDto> plotData, String legend, String color) {
        this.plotData = plotData;
        this.legend = legend;
        this.color = color;
    }

    public List<PointDto> getPlotData() {
        return plotData;
    }

    public String getLegend() {
        return legend;
    }

    public String getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlotSingleDto)) return false;

        PlotSingleDto that = (PlotSingleDto) o;

        if (legend != null ? !legend.equals(that.legend) : that.legend != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return legend != null ? legend.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PlotSingleDto{" +
                "plotData=" + plotData +
                ", legend='" + legend + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
