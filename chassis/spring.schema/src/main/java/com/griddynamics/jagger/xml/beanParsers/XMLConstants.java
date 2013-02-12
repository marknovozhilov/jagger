package com.griddynamics.jagger.xml.beanParsers;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nmusienko
 * Date: 05.12.12
 * Time: 13:25
 * To change this template use File | Settings | File Templates.
 */
public class XMLConstants {

    public static final String FATAL_DEVIATION_THRESHOLD = "fatalDeviationThreshold";
    public static final String WARNING_DEVIATION_THRESHOLD = "warningDeviationThreshold";
    public static final String WORKLOAD_DECISION_MAKER = "workloadDecisionMaker";
    public static final String MONITORING_PARAMETER_DECISION_MAKER = "monitoringParameterDecisionMaker";
    public static final String WORKLOAD_FEATURE_COMPARATOR = "workloadFeatureComparator";
    public static final String MONITORING_FEATURE_COMPARATOR = "monitoringFeatureComparator";
    public static final String DECISION_MAKER_TYPE = "decisionMakerType";
    public static final String DECISION_MAKER_REF = "decisionMakerRef";
    public static final String WORKLOAD = "workload";
    public static final String MONITORING = "monitoring";
    public static final String COMPARATOR_TYPE = "comparatorType";
    public static final String ID = "id";
    public static final String DEFAULT_REPORTING_SERVICE = "defaultReportingService";
    public static final String CUSTOM_REPORTING_SERVICE = "customReportingService";
    public static final String REPORTING_SERVICE = "reportingService";
    public static final String REPORT_TYPE = "reportType";
    public static final String ROOT_TEMPLATE_LOCATION = "rootTemplateLocation";
    public static final String OUTPUT_REPORT_LOCATION = "outputReportLocation";
    public static final String EXTENSION_PREFIX = "ext_";
    public static final String EXTENSION = "extension";
    public static final String ATTRIBUTE_REF = "ref";
    public static final String BEAN = "bean";
    public static final String COMPARATOR = "comparator";
    public static final String COMPARATOR_CHAIN = "comparatorChain";
    public static final String DECISION_MAKER = "decisionMaker";
    public static final String STRATEGY = "strategy";
    public static final String BASELINE_ID = "baselineId";
    public static final String CUSTOM_SESSION_COMPARATOR = "customSessionComparator";
    public static final String SESSION_COMPARATOR = "sessionComparator";
    public static final String WORST_CASE_DECISION_MAKER = "worstCaseDecisionMaker";
    public static final String WORST_CASE = "worstCase";
    public static final String SESSION_ID = "sessionId";
    public static final String SESSION_ID_PROVIDER = "sessionIdProvider";
    public static final String CUSTOM_BASELINE_SESSION_PROVIDER = "customBaselineSessionProvider";
    public static final String BASELINE_SESSION_PROVIDER = "baselineSessionProvider";
    public static final String TEST_PLAN = "test-plan";
    public static final String CONFIG = "config";
    public static final String TASKS = "tasks";
    public static final String TASK = "task";
    public static final String TESTS = "tests";
    public static final String TEST = "test";
    public static final String USERS = "users";
    public static final String USER = "user";
    public static final String TPS = "tps";
    public static final String INVOCATION = "invocation";
    public static final String VIRTUAL_USER = "virtual-user";
    public static final String VIRTUAL_USER_CLASS_FIELD = "virtualUser";
    public static final String SESSION_EXECUTION_LISTENERS = "session-executionListeners";
    public static final String SESSION_EXECUTION_LISTENERS_CLASS_FIELD = "sessionExecutionListeners";
    public static final String TASK_EXECUTION_LISTENERS = "task-executionListeners";
    public static final String TASK_EXECUTION_LISTENERS_CLASS_FIELD = "taskExecutionListeners";
    public static final String GENERATOR = "generator";
    public static final String GENERATOR_GENERATE = "#{generator.generate()}";
    public static final String LOCAL = "local";
    public static final String MONITORING_ENABLE = "monitoringEnable";
    //listeners beans. must be in scope(locations - default-collectors.conf.xml , default-aggregators.conf.xml)
    public static final String BASIC_COLLECTOR = "basicSessionCollector";
    public static final String WORKLOAD_COLLECTOR = "e1MasterCollector";
    public static final String BASIC_AGGREGATOR = "basicAggregator";
    public static final String WORKLOAD_AGGREGATOR = "e1ScenarioAggregator";
    public static final String MONITORING_AGGREGATOR = "monitoringAggregator";
    public static final String DURATION_LOG_PROCESSOR = "durationLogProcessor";
    //don't change the order!!! will not works
    public static final List<String> STANDARD_SESSION_EXEC_LISTENERS = Arrays.asList(BASIC_COLLECTOR, BASIC_AGGREGATOR);
    public static final List<String> STANDARD_TASK_EXEC_LISTENERS = Arrays.asList(BASIC_COLLECTOR, WORKLOAD_COLLECTOR, BASIC_AGGREGATOR, WORKLOAD_AGGREGATOR, MONITORING_AGGREGATOR);

    public static final String WORKLOAD_LISTENERS_ELEMENT = "listeners";
    public static final String DURATION_COLLECTOR = "durationCollector";
    public static final String INFORMATION_COLLECTOR = "informationCollector";
    public static final String DIAGNOSTIC_COLLECTOR = "diagnosticCollector";

    //don't change the order!!! will not works
    public static final List<String> STANDARD_WORKLOAD_LISTENERS = Arrays.asList(DURATION_COLLECTOR, INFORMATION_COLLECTOR);

    public static final String WORKLOAD_LISTENERS_CLASS   = "collectors";
    public static final String VALIDATOR = "validator";
    public static final String QUERY_EQ = "queryEq";
    public static final String ENDPOINT_EQ = "endpointEq";
    public static final String RESULT_EQ = "resultEq";
    public static final String METRIC_CALCULATOR = "metricCalculator";
    public static final String LIST = "list";
    public static final String CLIENT_PARAMS = "clientParams";
    public static final String METHOD_PARAMS = "methodParams";
    public static final String METHOD = "method";
    public static final String INVOKER = "invoker";
    public static final String INVOKER_CLAZZ = "invokerClazz";
    public static final String ENDPOINT_PROVIDER = "endpointProvider";
    public static final String QUERY_PROVIDER = "queryProvider";
    public static final String LOAD_BALANCER = "loadBalancer";
    public static final String SCENARIO = "scenario";
    public static final String SCENARIO_FACTORY = "scenarioFactory";
    public static final String PARENT = "parent";
    public static final String XSI_TYPE = "xsi:type";
    public static final String PERCENTILES = "percentiles";
    public static final String PERCENTILES_TIME = "percentiles-time";
    public static final String PERCENTILES_GLOBAL = "percentiles-global";
    public static final String TIME_WINDOW_PERCENTILES_KEYS = "timeWindowPercentilesKeys";
    public static final String GLOBAL_PERCENTILES_KEYS = "globalPercentilesKeys";
    public static final String REPORT = "report";
    public static final String CALIBRATOR = "calibrator";
    public static final String CALIBRATION = "calibration";
    public static final String NAME = "name";
    public static final String CLASS = "class";

    public static final String DEFAULT_NAMESPACE = "http://www.griddynamics.com/schema/jagger";
}
