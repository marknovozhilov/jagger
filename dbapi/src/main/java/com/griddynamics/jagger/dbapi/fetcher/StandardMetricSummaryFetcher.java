package com.griddynamics.jagger.dbapi.fetcher;

import com.griddynamics.jagger.dbapi.dto.SummaryMetricValueDto;
import com.griddynamics.jagger.dbapi.dto.SummarySingleDto;
import com.griddynamics.jagger.dbapi.dto.MetricNameDto;
import com.griddynamics.jagger.dbapi.entity.WorkloadTaskData;
import com.griddynamics.jagger.util.StandardMetricsNamesUtil;

import java.util.*;

public class StandardMetricSummaryFetcher extends DbMetricDataFetcher<SummarySingleDto> {

    @Override
    protected Set<SummarySingleDto> fetchData(List<MetricNameDto> standardMetricNames) {

        Set<SummarySingleDto> resultSet = new HashSet<SummarySingleDto>();

        resultSet.addAll(getRestMetrics(standardMetricNames));

        return resultSet;
    }

    private Collection<? extends SummarySingleDto> getRestMetrics(List<MetricNameDto> restMetricNames) {

        if (restMetricNames.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> taskIds = new HashSet<Long>();
        for (MetricNameDto metricName : restMetricNames) {
            taskIds.addAll(metricName.getTaskIds());
        }

        List<WorkloadTaskData> workloadTaskDatas = (List<WorkloadTaskData>)entityManager.createNativeQuery("select * " +
                "        from WorkloadTaskData as wtd join TaskData as td on td.id in (:ids)" +
                "         and wtd.taskId=td.taskId and wtd.sessionId=td.sessionId", WorkloadTaskData.class)
                .setParameter("ids", taskIds)
                .getResultList();

        if (workloadTaskDatas.isEmpty()) {
            log.warn("Could not find data for {}", restMetricNames);
            return Collections.emptyList();
        }

        Set<SummarySingleDto> resultSet = new HashSet<SummarySingleDto>();
        for (MetricNameDto metricName : restMetricNames) {

            SummarySingleDto metricDto = new SummarySingleDto();
            resultSet.add(metricDto);
            metricDto.setMetricName(metricName);
            metricDto.setValues(new HashSet<SummaryMetricValueDto>());
            for (WorkloadTaskData workloadTaskData : workloadTaskDatas) {

                // check if this WorkloadTaskData suit to given MetricNameDto
                if (metricName.getTest().getTaskName().equals(workloadTaskData.getScenario().getName())
                        && metricName.getTest().getSessionIds().contains(workloadTaskData.getSessionId())) {

                    SummaryMetricValueDto mvd = new SummaryMetricValueDto();
                    mvd.setSessionId(Long.parseLong(workloadTaskData.getSessionId()));
                    String metricId = metricName.getMetricName();

                    String value = null;
                    if (metricId.equals(StandardMetricsNamesUtil.LATENCY_ID)) {
                        value = workloadTaskData.getAvgLatency().toString();
                    } else if (metricId.equals(StandardMetricsNamesUtil.ITERATION_SAMPLES_ID)) {
                        value = workloadTaskData.getSamples().toString();
                    } else if (metricId.equals(StandardMetricsNamesUtil.SUCCESS_RATE_ID)) {
                        value = workloadTaskData.getSuccessRate().toString();
                    } else if (metricId.equals(StandardMetricsNamesUtil.THROUGHPUT_ID)) {
                        value = workloadTaskData.getThroughput().toString();
                    } else if (metricId.equals(StandardMetricsNamesUtil.FAIL_COUNT_ID)) {
                        value = workloadTaskData.getFailuresCount().toString();
                    } else if (metricId.equals(StandardMetricsNamesUtil.LATENCY_STD_DEV_ID)) {
                        value = workloadTaskData.getStdDevLatency().toString();
                    }

                    if (value != null)  {
                        mvd.setValue(value);
                        metricDto.getValues().add(mvd);
                    }
                }
            }
        }

        return resultSet;
    }
}
