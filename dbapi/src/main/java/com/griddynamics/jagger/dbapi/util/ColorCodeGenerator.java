package com.griddynamics.jagger.dbapi.util;

import com.google.common.collect.ImmutableList;

import static com.griddynamics.jagger.util.MonitoringIdUtils.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author "Artem Kirillov" (akirillov@griddynamics.com)
 * @since 5/31/12
 */
public class ColorCodeGenerator {
    public static final String LATENCY_COLOR = "LATENCY";
    public static final String LATENCY_STD_DEV_COLOR = "LATENCY_STD_DEV";
    public static final String THROUGHPUT_COLOR = "THROUGHPUT";
    private static AtomicInteger counter = new AtomicInteger(0);
    private static ConcurrentMap<String, Integer> sessionsMap = new ConcurrentHashMap<String, Integer>();
    private static final  ImmutableList<String> colorsHexCodes = ImmutableList.of(
            "#000000",
            "#FF8C00",
            "#800000",
            "#FF4500",
            "#808000",
            "#00FF00",
            "#008000",
            "#00FFFF",
            "#008080",
            "#0000FF",
            "#000080",
            "#FF00FF",
            "#800080",
            "#D2691E",
            "#FF0000",
            "#00BFFF",
            "#9ACD32"
    );
    protected ColorCodeGenerator() {
    }

    public static String getHexColorCode(String metricId, String sessionId) {
        String colorId;
        MonitoringId monitoringId = splitMonitoringMetricId(metricId);
        if (monitoringId != null) {
            colorId = monitoringId.getMonitoringName() + sessionId;
        } else {
            colorId = metricId + sessionId;
        }
        int index = Math.abs(counter.getAndIncrement() % colorsHexCodes.size());
        Integer result = sessionsMap.putIfAbsent(colorId, index);
        if (result == null) {
            return colorsHexCodes.get(index);
        } else {
            return colorsHexCodes.get(result);
        }
    }

}
