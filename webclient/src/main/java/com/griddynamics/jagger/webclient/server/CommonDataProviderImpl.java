package com.griddynamics.jagger.webclient.server;

import com.griddynamics.jagger.agent.model.DefaultMonitoringParameters;
import com.griddynamics.jagger.engine.e1.aggregator.workload.model.WorkloadProcessLatencyPercentile;
import com.griddynamics.jagger.monitoring.reporting.GroupKey;
import com.griddynamics.jagger.util.Pair;
import com.griddynamics.jagger.webclient.client.components.control.model.*;
import com.griddynamics.jagger.webclient.client.data.MetricRankingProvider;
import com.griddynamics.jagger.webclient.client.dto.MetricNameDto;
import com.griddynamics.jagger.webclient.client.dto.PlotNameDto;
import com.griddynamics.jagger.webclient.client.dto.SessionPlotNameDto;
import com.griddynamics.jagger.webclient.client.dto.TaskDataDto;
import com.griddynamics.jagger.webclient.server.plot.CustomMetricPlotDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.griddynamics.jagger.webclient.client.mvp.NameTokens.*;

/**
 * Created with IntelliJ IDEA.
 * User: amikryukov
 * Date: 11/27/13
 */
public class CommonDataProviderImpl implements CommonDataProvider {

    private EntityManager entityManager;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private CustomMetricPlotDataProvider customMetricPlotDataProvider;
    private Map<GroupKey, DefaultWorkloadParameters[]> workloadPlotGroups;
    private Map<GroupKey, DefaultMonitoringParameters[]> monitoringPlotGroups;

    public Map<GroupKey, DefaultMonitoringParameters[]> getMonitoringPlotGroups() {
        return monitoringPlotGroups;
    }

    public void setMonitoringPlotGroups(Map<GroupKey, DefaultMonitoringParameters[]> monitoringPlotGroups) {
        this.monitoringPlotGroups = monitoringPlotGroups;
    }

    public Map<GroupKey, DefaultWorkloadParameters[]> getWorkloadPlotGroups() {
        return workloadPlotGroups;
    }

    public void setWorkloadPlotGroups(Map<GroupKey, DefaultWorkloadParameters[]> workloadPlotGroups) {
        this.workloadPlotGroups = workloadPlotGroups;
    }

    public CustomMetricPlotDataProvider getCustomMetricPlotDataProvider() {
        return customMetricPlotDataProvider;
    }

    public void setCustomMetricPlotDataProvider(CustomMetricPlotDataProvider customMetricPlotDataProvider) {
        this.customMetricPlotDataProvider = customMetricPlotDataProvider;
    }

    private HashMap<String, Pair<String, String>> standardMetrics;

    @Required
    public void setStandardMetrics(HashMap<String, Pair<String, String>> standardMetrics) {
        this.standardMetrics = standardMetrics;
    }


    public Set<MetricNameDto> getCustomMetricsNames(TaskDataDto tests){
        Set<MetricNameDto> metrics;

        List<String> metricNames = entityManager.createNativeQuery("select metric.name from DiagnosticResultEntity as metric " +
                "where metric.workloadData_id in " +
                "(select workloadData.id from WorkloadData as workloadData " +
                "inner join (select id, taskId, sessionId from TaskData where id in (:ids)) as taskData on " +
                "workloadData.taskId=taskData.taskId and workloadData.sessionId=taskData.sessionId)")
                .setParameter("ids", tests.getIds()).getResultList();

        List<String> validatorNames = entityManager.createNativeQuery("select metric.validator from ValidationResultEntity as metric " +
                "where metric.workloadData_id in " +
                "(select workloadData.id from WorkloadData as workloadData " +
                "inner join (select id, taskId, sessionId from TaskData where id in (:ids)) as taskData on " +
                "workloadData.taskId=taskData.taskId and workloadData.sessionId=taskData.sessionId)")
                .setParameter("ids", tests.getIds()).getResultList();
        metrics = new HashSet<MetricNameDto>(metricNames.size()+validatorNames.size());

        for (String name : metricNames){
            if (name == null) continue;

            MetricNameDto metric = new MetricNameDto();
            metric.setTests(tests);
            metric.setName(name);

            metrics.add(metric);
        }

        for (String name : validatorNames){
            if (name == null) continue;

            MetricNameDto validator = new MetricNameDto();
            validator.setTests(tests);
            validator.setName(name);

            metrics.add(validator);
        }

        return metrics;
    }

    public Set<MetricNameDto> getCustomMetricsNames(List<TaskDataDto> tests){
        Set<MetricNameDto> metrics;

        Set<Long> taskIds = new HashSet<Long>();
        for (TaskDataDto tdd : tests) {
            taskIds.addAll(tdd.getIds());
        }

        long temp = System.currentTimeMillis();
        List<Object[]> metricNames = entityManager.createNativeQuery(
                                                                    "select metric.name, task.id from DiagnosticResultEntity as metric " +
                                                                            "    inner join TaskData as task " +
                                                                            "     on task.id in (:ids) and metric.workloadData_id in " +
                                                                            "      (select workloadData.id from WorkloadData as workloadData where workloadData.taskId=task.taskId and workloadData.sessionId=task.sessionId)")
                                                                    .setParameter("ids", taskIds).getResultList();

        log.debug("{} ms spent for fetching {} metrics", System.currentTimeMillis() - temp, metricNames.size());
        temp = System.currentTimeMillis();

        List<Object[]> validatorNames = entityManager.createNativeQuery( "select metric.validator, task.id from ValidationResultEntity as metric " +
                "    inner join TaskData as task " +
                "     on task.id in (:ids) and metric.workloadData_id in " +
                "      (select workloadData.id from WorkloadData as workloadData where workloadData.taskId=task.taskId and workloadData.sessionId=task.sessionId)")
                .setParameter("ids", taskIds).getResultList();
        log.debug("{} ms spent for fetching {} metrics", System.currentTimeMillis() - temp, validatorNames.size());

        metrics = new HashSet<MetricNameDto>(metricNames.size()+validatorNames.size());

        for (Object[] name : metricNames){
            if (name == null || name[0] == null) continue;
            for (TaskDataDto td : tests) {
                if (td.getIds().contains(((BigInteger)name[1]).longValue())) {
                    MetricNameDto metric = new MetricNameDto();
                    metric.setTests(td);
                    metric.setName((String)name[0]);
                    metrics.add(metric);
                    break;
                }
            }
        }

        for (Object[] name : validatorNames){
            if (name == null || name[0] == null) continue;
            for (TaskDataDto td : tests) {
                if (td.getIds().contains(((BigInteger)name[1]).longValue())) {
                    MetricNameDto metric = new MetricNameDto();
                    metric.setTests(td);
                    metric.setName((String)name[0]);
                    metrics.add(metric);
                    break;
                }
            }
        }

        return metrics;
    }

    public Set<MetricNameDto> getLatencyMetricsNames(TaskDataDto tests){
        Set<MetricNameDto> latencyNames;

        List<WorkloadProcessLatencyPercentile> latency = entityManager.createQuery(
                "select s from  WorkloadProcessLatencyPercentile as s where s.workloadProcessDescriptiveStatistics.taskData.id in (:taskIds) " +
                        "group by s.percentileKey " +
                        "having count(s.id)=:size")
                .setParameter("taskIds", tests.getIds())
                .setParameter("size", (long) tests.getIds().size())
                .getResultList();

        latencyNames = new HashSet<MetricNameDto>(latency.size());
        if (!latency.isEmpty()){
            for(WorkloadProcessLatencyPercentile percentile : latency) {
                MetricNameDto dto = new MetricNameDto();
                dto.setName("Latency "+Double.toString(percentile.getPercentileKey())+" %");
                dto.setTests(tests);
                latencyNames.add(dto);
            }
        }
        return latencyNames;
    }

    public Set<MetricNameDto> getLatencyMetricsNames(List<TaskDataDto> tests){
        Set<MetricNameDto> latencyNames;

        Set<Long> testIds = new HashSet<Long>();
        for (TaskDataDto tdd : tests) {
            testIds.addAll(tdd.getIds());
        }

        long temp = System.currentTimeMillis();
        List<WorkloadProcessLatencyPercentile> latency = entityManager.createQuery(
                "select s from  WorkloadProcessLatencyPercentile as s where s.workloadProcessDescriptiveStatistics.taskData.id in (:taskIds) ")
                .setParameter("taskIds", testIds)
                .getResultList();


        log.debug("{} ms spent for Latency Percentile fetching (size ={})", System.currentTimeMillis() - temp, latency.size());

        latencyNames = new HashSet<MetricNameDto>(latency.size());

        if (!latency.isEmpty()){


            for(WorkloadProcessLatencyPercentile percentile : latency) {
                for (TaskDataDto tdd : tests) {

                    if (tdd.getIds().contains(percentile.getWorkloadProcessDescriptiveStatistics().getTaskData().getId())) {
                        MetricNameDto dto = new MetricNameDto();
                        dto.setName("Latency "+Double.toString(percentile.getPercentileKey())+" %");
                        dto.setTests(tdd);
                        latencyNames.add(dto);
                        break;
                    }
                }
            }
        }
        return latencyNames;
    }


    /**
     * one db call method
     * @param sessionIds
     * @param taskDataDtos
     * @return
     */
    public Map<TaskDataDto, List<MonitoringPlotNode>> getMonitoringPlotNodes(Set<String> sessionIds, List<TaskDataDto> taskDataDtos) {
        try {
            Map<TaskDataDto, List<BigInteger>>  monitoringIds = getMonitoringIds(sessionIds, taskDataDtos);
            if (monitoringIds.isEmpty()) {
                return Collections.EMPTY_MAP;
            }



            Map<TaskDataDto, List<MonitoringPlotNode>> result = getMonitoringPlotNames(sessionIds, monitoringPlotGroups.entrySet(), monitoringIds);

            if (result.isEmpty()) {
                return Collections.EMPTY_MAP;
            }

            log.debug("For sessions {} are available these plots: {}", sessionIds, result);
            return result;

        } catch (Exception e) {
            log.error("Error was occurred during task scope plots data getting for session IDs " + sessionIds + ", tasks  " + taskDataDtos, e);
            throw new RuntimeException(e);
        }
    }


    private List<BigInteger> getMonitoringIds(Set<String> sessionIds, TaskDataDto taskDataDto) {

        List<BigInteger> monitoringTaskIds = (List<BigInteger>) entityManager.createNativeQuery(
                "select td.id from TaskData as td where td.sessionId in (:sessionIds) and td.taskId in " +
                    "(" +
                         "select pm.monitoringId from PerformedMonitoring as pm where pm.sessionId in (:sessionIds) and parentId in " +
                            "(" +
                                "select wd.parentId from WorkloadData as wd where wd.sessionId in (:sessionIds) and wd.taskId in " +
                                    "(" +
                                        "select td2.taskId from TaskData as td2 where td2.sessionId in (:sessionIds) and td2.id in (:ids)" +
                                    ")" +
                            ")" +
                    ")")
                .setParameter("ids", taskDataDto.getIds())
                .setParameter("sessionIds", sessionIds)
                .getResultList();

        return monitoringTaskIds;
    }

    /**
     * Fetch all Monitoring tasks ids for all tests
     * @param sessionIds all sessions
     * @param taskDataDtos all tests
     * @return list of monitoring task ids
     */
    private Map<TaskDataDto, List<BigInteger>> getMonitoringIds(Set<String> sessionIds, List<TaskDataDto> taskDataDtos) {

        List<Long> taskIds = new ArrayList<Long>();
        for (TaskDataDto tdd : taskDataDtos) {
            taskIds.addAll(tdd.getIds());
        }


        List<Object[]> monitoringTaskIds = entityManager.createNativeQuery(
                "select test.id, selected.testId from TaskData as test inner join" +
                        " (" +
                        "  select td2.id as testId, pm.monitoringId, pm.sessionId from PerformedMonitoring as pm join TaskData as td2" +
                        "    on pm.sessionId in (:sessionIds) and td2.sessionId in (:sessionIds) and (td2.id, pm.parentId) in" +
                        "    (" +
                        "        select td2.id, wd.parentId from WorkloadData as wd join TaskData as td2" +
                        "      on td2.id in (:ids) and wd.sessionId in (:sessionIds) and wd.taskId=td2.taskId" +
                        "    )" +
                        " ) as selected on test.taskId=selected.monitoringId and test.sessionId=selected.sessionId"
        )
                .setParameter("ids", taskIds)
                .setParameter("sessionIds", sessionIds)
                .getResultList();

        Map<TaskDataDto, List<BigInteger>> result = new HashMap<TaskDataDto, List<BigInteger>>();
        if (monitoringTaskIds.isEmpty()) {
            return Collections.EMPTY_MAP;
        }


        for (Object[] ids : monitoringTaskIds) {
            for (TaskDataDto tdd : taskDataDtos) {
                if (tdd.getIds().contains(((BigInteger)ids[1]).longValue())) {
                    if (!result.containsKey(tdd)) {
                        result.put(tdd, new ArrayList<BigInteger>());
                    }
                    result.get(tdd).add(((BigInteger)ids[0]));
                    break;
                }
            }
        }

        return result;
    }


    @Override
    public List<MonitoringSessionScopePlotNode> getMonitoringPlotNodesNew(Set<String> sessionIds) {

        List<MonitoringSessionScopePlotNode> monitoringPlotNodes;
        try {

            monitoringPlotNodes = getMonitoringPlotNamesNew(sessionIds);
            log.debug("For sessions {} are available these plots: {}", sessionIds, monitoringPlotNodes);
        } catch (Exception e) {
            log.error("Error was occurred during task scope plots data getting for session IDs " + sessionIds, e);
            throw new RuntimeException(e);
        }

        if (monitoringPlotNodes == null) {
            return Collections.EMPTY_LIST;
        }
        return monitoringPlotNodes;
    }


    @Override
    public Map<TaskDataDto, List<MetricNode>> getTestMetricsMap(final List<TaskDataDto> tddos) {

        Long time = System.currentTimeMillis();
        List<MetricNameDto> list = new ArrayList<MetricNameDto>();
        for (TaskDataDto taskDataDto : tddos){
            for (String standardMetricName : standardMetrics.keySet()){
                MetricNameDto metric = new MetricNameDto();
                metric.setName(standardMetricName);
                metric.setTests(taskDataDto);
                list.add(metric);
            }
        }

        ExecutorService pool = null;

        try {
            pool = Executors.newFixedThreadPool(2);

            Future<Set<MetricNameDto>> latencyMetricNamesFuture = pool.submit(
                    new Callable<Set<MetricNameDto>>(){

                        @Override
                        public Set<MetricNameDto> call() throws Exception {
                            return getLatencyMetricsNames(tddos);
                        }
                    }
            );

            Future<Set<MetricNameDto>> customMetricNamesFuture = pool.submit(
                    new Callable<Set<MetricNameDto>>(){

                        @Override
                        public Set<MetricNameDto> call() throws Exception {
                            return getCustomMetricsNames(tddos);
                        }
                    }
            );

            list.addAll(latencyMetricNamesFuture.get());
            list.addAll(customMetricNamesFuture.get());
        } catch (Exception e) {
            log.error("Exception occurs while fetching MetricNames for tests : ", e);
            throw new RuntimeException(e);
        } finally {
            if (pool != null) {
                pool.shutdown();
            }
        }

        log.info("For tasks {} was found {} metrics names for {} ms", new Object[]{tddos, list.size(), System.currentTimeMillis() - time});

        Map<TaskDataDto, List<MetricNode>> result = new HashMap<TaskDataDto, List<MetricNode>>();

        for (MetricNameDto mnd : list) {
            for (TaskDataDto tdd : tddos) {
                if (tdd.getIds().containsAll(mnd.getTaskIds())) {
                    if (!result.containsKey(tdd)) {
                        result.put(tdd, new ArrayList<MetricNode>());
                    }
                    MetricNode mn = new MetricNode();
                    mn.setMetricName(mnd);
                    mn.setId(SUMMARY_PREFIX + tdd.getTaskName() + mnd.getName());
                    mn.setDisplayName(mnd.getDisplay());
                    result.get(tdd).add(mn);
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public Map<TaskDataDto, List<PlotNode>> getTestPlotsMap(Set<String> sessionIds, List<TaskDataDto> taskList) {

        Map<TaskDataDto, List<PlotNode>> result = new HashMap<TaskDataDto, List<PlotNode>>();

        List<PlotNameDto> plotNameDtoSet = new ArrayList<PlotNameDto>();
        try {

            Map<TaskDataDto, Boolean> isWorkloadMap = isWorkloadStatisticsAvailable(sessionIds, taskList);
            for (Map.Entry<TaskDataDto, Boolean> entry: isWorkloadMap.entrySet()) {
                if (entry.getValue()) {
                    for (Map.Entry<GroupKey, DefaultWorkloadParameters[]> monitoringPlot : workloadPlotGroups.entrySet()) {
                        plotNameDtoSet.add(new PlotNameDto(entry.getKey(), monitoringPlot.getKey().getUpperName()));
                    }
                }
            }

            List<PlotNameDto> customMetrics = customMetricPlotDataProvider.getPlotNames(taskList);

            plotNameDtoSet.addAll(customMetrics);

            log.debug("For sessions {} are available these plots: {}", sessionIds, plotNameDtoSet);

            for (PlotNameDto pnd : plotNameDtoSet) {
                for (TaskDataDto tdd : taskList) {
                    if (tdd.getIds().containsAll(pnd.getTaskIds())) {
                        if (!result.containsKey(tdd)) {
                            result.put(tdd, new ArrayList<PlotNode>());
                        }
                        PlotNode pn = new PlotNode();
                        pn.setPlotName(pnd);
                        pn.setId(METRICS_PREFIX + tdd.getTaskName() + pnd.getPlotName());
                        pn.setDisplayName(pnd.getDisplay());
                        result.get(tdd).add(pn);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error was occurred during task scope plots data getting for session IDs " + sessionIds + ", tasks : " + taskList, e);
            throw new RuntimeException(e);
        }

        return result;
    }

    private List<SessionPlotNode> getMonitoringPlotNames(Set<String> sessionIds, Map.Entry<GroupKey, DefaultMonitoringParameters[]> monitoringParameters) {

        List<SessionPlotNode> resultList = new ArrayList<SessionPlotNode>();

        List<String> parametersList = new ArrayList<String>();
        for (DefaultMonitoringParameters mp: monitoringParameters.getValue()) {
            parametersList.add(mp.getDescription());
        }

        List<Object[]> agentIdentifierObjects =
                entityManager.createNativeQuery("select ms.boxIdentifier, ms.systemUnderTestUrl from MonitoringStatistics as ms" +
                        "  where ms.sessionId in (:sessionId)" +
                        " and ms.description in (:parametersList)" +
                        " group by ms.boxIdentifier, ms.systemUnderTestUrl")
                        .setParameter("sessionId", sessionIds)
                        .setParameter("parametersList", parametersList)
                        .getResultList();

        Set<String> differentAgentIdentifiers = new LinkedHashSet<String>();
        for (Object[] object: agentIdentifierObjects) {
            String identy = object[0] == null ? object[1].toString() : object[0].toString();
            differentAgentIdentifiers.add(identy);
        }

        for (String agentIdenty: differentAgentIdentifiers) {
            SessionPlotNode plotNode = new SessionPlotNode();
            plotNode.setPlotNameDto(new SessionPlotNameDto(sessionIds, monitoringParameters.getKey().getUpperName() + AGENT_NAME_SEPARATOR + agentIdenty));
            plotNode.setDisplayName(agentIdenty);
            String id = METRICS_PREFIX + monitoringParameters.getKey().getUpperName() + agentIdenty;
            plotNode.setId(id);
            resultList.add(plotNode);
        }

        MetricRankingProvider.sortPlotNodes(resultList);
        return resultList;
    }

    private List<MonitoringSessionScopePlotNode> getMonitoringPlotNamesNew(Set<String> sessionIds) {

        List<Object[]> agentIdentifierObjects =
                entityManager.createNativeQuery("select ms.boxIdentifier, ms.systemUnderTestUrl, ms.description from MonitoringStatistics as ms" +
                        "  where ms.sessionId in (:sessionId)" +
                        " group by ms.description, ms.boxIdentifier, ms.systemUnderTestUrl")
                        .setParameter("sessionId", sessionIds)
                        .getResultList();

        if (agentIdentifierObjects.size() == 0) {
            return Collections.EMPTY_LIST;
        }

        Map<String, MonitoringSessionScopePlotNode> tempMap = new HashMap<String, MonitoringSessionScopePlotNode>();

        Set<Map.Entry<GroupKey, DefaultMonitoringParameters[]>> set = monitoringPlotGroups.entrySet();
        for (Object[] objects : agentIdentifierObjects) {

            String groupKey = findMonitoringKey((String)objects[2], set);
            if (groupKey == null) {

                continue;
            }

            if (!tempMap.containsKey(groupKey)) {

                MonitoringSessionScopePlotNode monitoringPlotNode = new MonitoringSessionScopePlotNode();
                monitoringPlotNode.setId(MONITORING_PREFIX + groupKey);
                monitoringPlotNode.setDisplayName(groupKey);
                monitoringPlotNode.setPlots(new ArrayList<SessionPlotNode>());
                tempMap.put(groupKey, monitoringPlotNode);
            }

            MonitoringSessionScopePlotNode monitoringPlotNode = tempMap.get(groupKey);

            SessionPlotNode plotNode = new SessionPlotNode();
            String agentIdenty = objects[0] == null ? objects[1].toString() : objects[0].toString();
            plotNode.setPlotNameDto(new SessionPlotNameDto(sessionIds, groupKey + AGENT_NAME_SEPARATOR + agentIdenty));
            plotNode.setDisplayName(agentIdenty);
            String id = METRICS_PREFIX + groupKey + agentIdenty;
            plotNode.setId(id);

            if (!monitoringPlotNode.getPlots().contains(plotNode))
                monitoringPlotNode.getPlots().add(plotNode);

        }

        ArrayList<MonitoringSessionScopePlotNode> result = new ArrayList<MonitoringSessionScopePlotNode>(tempMap.values());
        for (MonitoringSessionScopePlotNode ms : result) {
            MetricRankingProvider.sortPlotNodes(ms.getPlots());
        }
        MetricRankingProvider.sortPlotNodes(result);

        return result;
    }

    private List<PlotNode> getMonitoringPlotNames(Set<String> sessionIds, Map.Entry<GroupKey, DefaultMonitoringParameters[]> monitoringParameters, TaskDataDto taskDataDto, List<BigInteger> monitoringIds) {
        List<PlotNode> resultList = new ArrayList<PlotNode>();

        List<String> parametersList = new ArrayList<String>();
        for (DefaultMonitoringParameters mp: monitoringParameters.getValue()) {
            parametersList.add(mp.getDescription());
        }

        List<Object[]> agentIdentifierObjects =
                entityManager.createNativeQuery("select ms.boxIdentifier, ms.systemUnderTestUrl from MonitoringStatistics as ms" +
                "  where ms.sessionId in (:sessionIds) " +
                        "and ms.taskData_id in (:taskIds) and ms.description in (:parametersList)" +
                " group by ms.boxIdentifier, ms.systemUnderTestUrl")
        .setParameter("sessionIds", sessionIds)
        .setParameter("parametersList", parametersList)
        .setParameter("taskIds", monitoringIds)
                .getResultList();

        Set<String> differentAgentIdentifiers = new LinkedHashSet<String>();
        for (Object[] object: agentIdentifierObjects) {
            String identy = object[0] == null ? object[1].toString() : object[0].toString();
            differentAgentIdentifiers.add(identy);
        }

        for (String agentIdenty: differentAgentIdentifiers) {
            PlotNode plotNode = new PlotNode();
            plotNode.setPlotName(new PlotNameDto(taskDataDto, monitoringParameters.getKey().getUpperName() + AGENT_NAME_SEPARATOR + agentIdenty));
            plotNode.setDisplayName(agentIdenty);
            String id = METRICS_PREFIX + taskDataDto.getTaskName() + monitoringParameters.getKey().getUpperName() + agentIdenty;
            plotNode.setId(id);
            resultList.add(plotNode);
        }

        MetricRankingProvider.sortPlotNodes(resultList);
        return resultList;
    }

    private Map<TaskDataDto, List<MonitoringPlotNode>> getMonitoringPlotNames(Set<String> sessionIds, Set<Map.Entry<GroupKey, DefaultMonitoringParameters[]>> monitoringParameters, Map<TaskDataDto, List<BigInteger>> monitoringIdsMap) {

        List<String> parametersList = new ArrayList<String>();

        Map<String, List<String>> map = new HashMap<String, List<String>>();

        for (Map.Entry<GroupKey, DefaultMonitoringParameters[]> entry : monitoringParameters) {
            map.put(entry.getKey().getUpperName(), new ArrayList<String>());
            for (DefaultMonitoringParameters mp: entry.getValue()) {
                parametersList.add(mp.getDescription());
                map.get(entry.getKey().getUpperName()).add(mp.getDescription());
            }
        }

        List<BigInteger> monitoringIds = new ArrayList<BigInteger>();
        for (List<BigInteger> mIds : monitoringIdsMap.values()) {
            monitoringIds.addAll(mIds);
        }

        List<Object[]> agentIdentifierObjects =
                entityManager.createNativeQuery("select ms.boxIdentifier, ms.systemUnderTestUrl, ms.taskData_id, ms.description  from MonitoringStatistics as ms" +
                        "  where " +
                        " ms.taskData_id in (:taskIds) " +
                        " group by ms.taskData_id, ms.description, boxIdentifier, systemUnderTestUrl")
                        .setParameter("taskIds", monitoringIds)
                        .getResultList();

        Map<TaskDataDto, List<MonitoringPlotNode>> resultMap = new HashMap<TaskDataDto, List<MonitoringPlotNode>>();

        Set<TaskDataDto> taskSet = monitoringIdsMap.keySet();

        for (Object[] objects : agentIdentifierObjects) {
            BigInteger testId = (BigInteger)objects[2];
            for (TaskDataDto tdd : taskSet) {
                if (monitoringIdsMap.get(tdd).contains(testId)) {
                    if (!resultMap.containsKey(tdd)) {
                        resultMap.put(tdd, new ArrayList<MonitoringPlotNode>());
                    }

                    List<MonitoringPlotNode> mpnList = resultMap.get(tdd);
                    String monitoringKey = findMonitoringKey((String)objects[3], monitoringParameters);
                    if (monitoringKey == null) {
                        log.warn("Could not find monitoing key for description: '{}' and monitoing task id: '{}'", objects[3], objects[2]);
                        break;
                    }
                    String identy = objects[0] == null ? objects[1].toString() : objects[0].toString();

                    PlotNode plotNode = new PlotNode();
                    plotNode.setPlotName(new PlotNameDto(tdd, monitoringKey + AGENT_NAME_SEPARATOR + identy));
                    plotNode.setDisplayName(identy);
                    String id = METRICS_PREFIX + tdd.getTaskName() + monitoringKey + identy;
                    plotNode.setId(id);

                    boolean present = false;
                    for (MonitoringPlotNode mpn : mpnList) {
                        if (mpn.getDisplayName().equals(monitoringKey)) {
                            if (!mpn.getPlots().contains(plotNode))
                                mpn.getPlots().add(plotNode);
                            present = true;
                            break;
                        }
                    }

                    if (!present) {
                        MonitoringPlotNode monitoringPlotNode = new MonitoringPlotNode();
                        monitoringPlotNode.setId(MONITORING_PREFIX + tdd.getTaskName() + monitoringKey);
                        monitoringPlotNode.setDisplayName(monitoringKey);
                        resultMap.get(tdd).add(monitoringPlotNode);
                        monitoringPlotNode.setPlots(new ArrayList<PlotNode>());
                        if (!monitoringPlotNode.getPlots().contains(plotNode))
                            monitoringPlotNode.getPlots().add(plotNode);
                    }
                    break;
                }
            }
        }


        // sorting
        for (TaskDataDto tdd : taskSet) {
            List<MonitoringPlotNode> mpnList = resultMap.get(tdd);
            if (mpnList == null) continue;
            MetricRankingProvider.sortPlotNodes(mpnList);
            for (MonitoringPlotNode mpn : mpnList) {
                MetricRankingProvider.sortPlotNodes(mpn.getPlots());
            }
        }
        return resultMap;
    }

    private String findMonitoringKey(String description, Set<Map.Entry<GroupKey, DefaultMonitoringParameters[]>> monitoringParameters) {
        for (Map.Entry<GroupKey, DefaultMonitoringParameters[]> entry : monitoringParameters) {
            for (DefaultMonitoringParameters dmp : entry.getValue()) {
                if (dmp.getDescription().equals(description)) {
                    return entry.getKey().getUpperName();
                }
            }
        }
        return null;
    }


    private boolean isWorkloadStatisticsAvailable(Set<String> sessionIds, TaskDataDto tests) {
        long timestamp = System.currentTimeMillis();
        long workloadStatisticsCount = (Long) entityManager.createQuery("select count(tis.id) from TimeInvocationStatistics as tis where tis.taskData.sessionId in (:sessionIds) and tis.taskData.id in (:tests)")
                .setParameter("tests", tests.getIds())
                .setParameter("sessionIds", sessionIds)
                .getSingleResult();

        if (workloadStatisticsCount < tests.getIds().size()) {
            log.info("For task ID {} workload statistics were not found in DB for {} ms", tests.getTaskName(), System.currentTimeMillis() - timestamp);
            return false;
        }

        return true;
    }

    private Map<TaskDataDto, Boolean> isWorkloadStatisticsAvailable(Set<String> sessionIds, List<TaskDataDto> tests) {

        List<Long> testsIds = new ArrayList<Long>();
        for (TaskDataDto tdd : tests) {
            testsIds.addAll(tdd.getIds());
        }


        List<Object[]> objects  = entityManager.createQuery("select tis.taskData.id, count(tis.id) from TimeInvocationStatistics as tis where tis.taskData.id in (:tests)")
                .setParameter("tests", testsIds)
                .getResultList();


        if (objects.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        Map<TaskDataDto, Integer> tempMap = new HashMap<TaskDataDto, Integer>(tests.size());
        for (TaskDataDto tdd : tests) {
            tempMap.put(tdd, 0);
        }

        for (Object[] object : objects) {
            for (TaskDataDto tdd : tests) {
                if (tdd.getIds().contains((Long) object[1])) {
                    int value = tempMap.get(tdd);
                    tempMap.put(tdd, ++value);
                }
            }
        }

        Map<TaskDataDto, Boolean> resultMap = new HashMap<TaskDataDto, Boolean>(tests.size());
        for (Map.Entry<TaskDataDto, Integer> entry : tempMap.entrySet()) {
            resultMap.put(entry.getKey(), entry.getValue() < entry.getKey().getIds().size());
        }

        return resultMap;
    }

    private boolean isMonitoringStatisticsAvailable(String sessionId) {
        long timestamp = System.currentTimeMillis();
        long monitoringStatisticsCount = (Long) entityManager.createQuery("select count(ms.id) from MonitoringStatistics as ms where ms.sessionId=:sessionId")
                .setParameter("sessionId", sessionId)
                .getSingleResult();

        if (monitoringStatisticsCount == 0) {
            log.info("For session {} monitoring statistics were not found in DB for {} ms", sessionId, System.currentTimeMillis() - timestamp);
            return false;
        }

        return true;
    }

    @Override
    public List<TaskDataDto> getTaskDataForSessions(Set<String> sessionIds) {

        long timestamp = System.currentTimeMillis();
        List<Object[]> list = entityManager.createNativeQuery
                (
                        "select taskData.id, commonTests.name, commonTests.description, taskData.taskId , commonTests.clock, commonTests.clockValue, commonTests.termination" +
                                " from "+
                                "( "+
                                "select test.name, test.description, test.version, test.sessionId, test.taskId, test.clock, test.clockValue, test.termination from " +
                                "( "+
                                "select " +
                                "l.*, s.name, s.description, s.version " +
                                "from "+
                                "(select * from WorkloadTaskData where sessionId in (:sessions)) as l "+
                                "left outer join "+
                                "(select * from WorkloadDetails) as s "+
                                "on l.scenario_id=s.id "+
                                ") as test " +
                                "inner join " +
                                "( " +
                                "select t.* from "+
                                "( "+
                                "select " +
                                "l.*, s.name, s.description, s.version " +
                                "from "+
                                "(select * from WorkloadTaskData where sessionId in (:sessions)) as l "+
                                "left outer join "+
                                "(select * from WorkloadDetails) as s "+
                                "on l.scenario_id=s.id " +
                                ") as t "+
                                "group by "+
                                "t.termination, t.clock, t.clockValue, t.name, t.version "+
                                "having count(t.id)>=:sessionCount" +

                                ") as testArch " +
                                "on "+
                                "test.clock=testArch.clock and "+
                                "test.clockValue=testArch.clockValue and "+
                                "test.termination=testArch.termination and "+
                                "test.name=testArch.name and "+
                                "test.version=testArch.version "+
                                ") as commonTests "+
                                "left outer join "+
                                "(select * from TaskData where sessionId in (:sessions)) as taskData "+
                                "on "+
                                "commonTests.sessionId=taskData.sessionId and "+
                                "commonTests.taskId=taskData.taskId "
                ).setParameter("sessions", sessionIds)
                .setParameter("sessionCount", (long) sessionIds.size()).getResultList();

        //group tests by description
        HashMap<String, TaskDataDto> map = new HashMap<String, TaskDataDto>(list.size());
        HashMap<String, Integer> mapIds = new HashMap<String, Integer>(list.size());
        for (Object[] testData : list){
            BigInteger id = (BigInteger)testData[0];
            String name = (String) testData[1];
            String description = (String) testData[2];
            String taskId = (String)testData[3];
            String clock = testData[4] + " (" + testData[5] + ")";
            String termination = (String) testData[6];


            int taskIdInt = Integer.parseInt(taskId.substring(5));
            String key = description+name;
            if (map.containsKey(key)){
                map.get(key).getIds().add(id.longValue());

                Integer oldValue = mapIds.get(key);
                mapIds.put(key, (oldValue==null ? 0 : oldValue)+taskIdInt);
            }else{
                TaskDataDto taskDataDto = new TaskDataDto(id.longValue(), name, description);
                taskDataDto.setClock(clock);
                taskDataDto.setTerminationStrategy(termination);
                //merge
                if (map.containsKey(name)){
                    taskDataDto.getIds().addAll(map.get(name).getIds());

                    taskIdInt = taskIdInt + mapIds.get(name);
                }
                map.put(key, taskDataDto);
                mapIds.put(key, taskIdInt);
            }
        }

        if (map.isEmpty()){
            return Collections.EMPTY_LIST;
        }

        PriorityQueue<Object[]> priorityQueue= new PriorityQueue<Object[]>(mapIds.size(), new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                return ((Comparable)o1[0]).compareTo(o2[0]);
            }
        });

        for (String key : map.keySet()){
            TaskDataDto taskDataDto = map.get(key);
            if (taskDataDto.getIds().size() == sessionIds.size()){
                priorityQueue.add(new Object[]{mapIds.get(key), taskDataDto});
            }
        }

        ArrayList<TaskDataDto> result = new ArrayList<TaskDataDto>(priorityQueue.size());
        while (!priorityQueue.isEmpty()){
            result.add((TaskDataDto)priorityQueue.poll()[1]);
        }

        log.info("For sessions {} was loaded {} tasks for {} ms", new Object[]{sessionIds, result.size(), System.currentTimeMillis() - timestamp});
        return result;
    }
}
