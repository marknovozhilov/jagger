/*
 * Copyright (c) 2010-2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 * http://www.griddynamics.com
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.griddynamics.jagger.engine.e1.reporting;

import com.griddynamics.jagger.dbapi.DatabaseService;
import com.griddynamics.jagger.dbapi.dto.PlotIntegratedDto;
import com.griddynamics.jagger.dbapi.model.MetricGroupNode;
import com.griddynamics.jagger.dbapi.model.MetricNode;
import com.griddynamics.jagger.dbapi.model.PlotNode;
import com.griddynamics.jagger.dbapi.model.RootNode;
import com.griddynamics.jagger.dbapi.util.SessionMatchingSetup;
import com.griddynamics.jagger.reporting.AbstractReportProvider;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;

/**
 * @author Mark Novozhilov
 *         Date: 26.06.2014
 */

public class SessionScopePlotsReporter extends AbstractReportProvider {
    private Logger log = LoggerFactory.getLogger(SessionScopePlotsReporter.class);


    private Map<MetricNode, PlotIntegratedDto> plots = Collections.EMPTY_MAP;

    private DatabaseService databaseService;

    @Required
    public void setDatabaseService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public JRDataSource getDataSource() {

        String sessionId = getSessionIdProvider().getSessionId();
        MetricPlotsReporter.MetricPlotDTOs result = new MetricPlotsReporter.MetricPlotDTOs();

        SessionMatchingSetup sessionMatchingSetup = new SessionMatchingSetup(
                databaseService.getWebClientProperties().isShowOnlyMatchedTests(),
                EnumSet.of(SessionMatchingSetup.MatchBy.ALL));
        RootNode rootNode = databaseService.getControlTreeForSessions(new HashSet<String>(Arrays.asList(sessionId)), sessionMatchingSetup);
        MetricGroupNode<PlotNode> sessionScopeNode = rootNode.getDetailsNode().getSessionScopeNode();

        if (sessionScopeNode.getChildren().isEmpty())
            return new JRBeanCollectionDataSource(Collections.singleton(result));

        Set<MetricNode> allMetrics = new HashSet<MetricNode>(sessionScopeNode.getMetrics());
        try {
            plots = databaseService.getPlotDataByMetricNode(allMetrics);
        } catch (Exception e) {
            log.error("Unable to get plots information for metrics");
        }

        getReport(sessionScopeNode, result);
        return new JRBeanCollectionDataSource(Collections.singleton(result));

    }

    private void getReport(MetricGroupNode metricGroupNode, MetricPlotsReporter.MetricPlotDTOs result) {
        try {

            if (metricGroupNode.getMetricGroupNodeList() != null) {
                for (MetricGroupNode metricGroup : (List<MetricGroupNode>) metricGroupNode.getMetricGroupNodeList())
                    getReport(metricGroup, result);
            }
            if (metricGroupNode.getMetricsWithoutChildren() != null) {

                String groupTitle = metricGroupNode.getDisplayName();
                for (MetricNode node : (List<MetricNode>) metricGroupNode.getMetricsWithoutChildren()) {
                    if (plots.get(node).getPlotSeries().isEmpty()) {
                        log.warn("No plot data for " + node.getDisplayName());
                        continue;
                    }

                    result.getMetricPlotDTOs().add(new MetricPlotsReporter.MetricPlotDTO(
                            node.getDisplayName(),
                            node.getDisplayName(),
                            groupTitle,
                            MetricPlotsReporter.makePlot(plots.get(node))));
                    groupTitle = "";
                }
            }
        } catch (Exception e) {
            log.error("Unable to take plot data for {}", metricGroupNode.getDisplayName());
        }
    }
}