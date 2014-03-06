package com.griddynamics.jagger.webclient.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.griddynamics.jagger.webclient.client.components.control.model.MetricNode;
import com.griddynamics.jagger.webclient.client.dto.MetricNameDto;
import com.griddynamics.jagger.webclient.client.dto.PlotSeriesDto;
import com.griddynamics.jagger.webclient.client.dto.SessionPlotNameDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author "Artem Kirillov" (akirillov@griddynamics.com)
 * @since 5/30/12
 */
public interface PlotProviderServiceAsync {

    void getSessionScopePlotData(String sessionId, Collection<SessionPlotNameDto> plotType, AsyncCallback<Map<SessionPlotNameDto, List<PlotSeriesDto>>> async);

    //??? dummy parameter to avoid method signature overlap
    void getPlotDatas(Set<MetricNode> plots, boolean dummy, AsyncCallback<Map<MetricNode,PlotSeriesDto>> async);

    void getPlotDatas(Set<MetricNameDto> plots, AsyncCallback<Map<MetricNameDto,List<PlotSeriesDto>>> async);

//??? not used    void getPlotData(Set<Long> taskId, MetricNameDto plotType, AsyncCallback<List<PlotSeriesDto>> async);

//??? not used    void getPlotData(long taskId, MetricNameDto plotType, AsyncCallback<List<PlotSeriesDto>> async);
}
