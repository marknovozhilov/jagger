package com.griddynamics.jagger.dbapi.dto;

import com.griddynamics.jagger.dbapi.model.MetricGroupNode;
import com.griddynamics.jagger.dbapi.model.LegendNode;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * Represents plot with multiple lines
 * Refers to MetricNode
 *
 * @author "Artem Kirillov" (akirillov@griddynamics.com)
 * @since 5/31/12
 */
public class PlotIntegratedDto implements Serializable {
    private Collection<PlotSingleDto> plotSeries = Collections.emptyList();
    private Collection<MarkingDto> markingSeries = Collections.emptyList();
    private String xAxisLabel;
    private String yAxisLabel;
    private String plotHeader;
    private double yAxisMin;

    private MetricGroupNode<LegendNode> legendTree;

    public MetricGroupNode<LegendNode> getLegendTree() {
        return legendTree;
    }

    public void setLegendTree(MetricGroupNode<LegendNode> legendTree) {
        this.legendTree = legendTree;
    }

    public PlotIntegratedDto() {
    }

    public PlotIntegratedDto(Collection<PlotSingleDto> plotSeries, String xAxisLabel, String yAxisLabel, String plotHeader) {
        this.plotSeries = plotSeries;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.plotHeader = plotHeader;
    }

    public PlotIntegratedDto(Collection<PlotSingleDto> plotSeries, String xAxisLabel, String yAxisLabel, String plotHeader, Collection<MarkingDto> markingSeries) {
        this.plotSeries = plotSeries;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.plotHeader = plotHeader;
        this.markingSeries = markingSeries;
    }

    public Collection<PlotSingleDto> getPlotSeries() {
        return plotSeries;
    }

    public String getXAxisLabel() {
        return xAxisLabel;
    }

    public String getYAxisLabel() {
        return yAxisLabel;
    }

    public String getPlotHeader() {
        return plotHeader;
    }

    public Collection<MarkingDto> getMarkingSeries() {
        return markingSeries;
    }

    public double getYAxisMin() {
        return yAxisMin;
    }

    public void setYAxisMin(double yAxisMin) {
        this.yAxisMin = yAxisMin;
    }

    @Override
    public String toString() {
        return "PlotIntegratedDto{" +
                "plotSeries=" + plotSeries +
                ", markingSeries=" + markingSeries +
                ", xAxisLabel='" + xAxisLabel + '\'' +
                ", yAxisLabel='" + yAxisLabel + '\'' +
                ", plotHeader='" + plotHeader + '\'' +
                '}';
    }
}
