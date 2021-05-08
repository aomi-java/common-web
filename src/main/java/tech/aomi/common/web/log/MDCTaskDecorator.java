package tech.aomi.common.web.log;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;
import java.util.UUID;

public class MDCTaskDecorator implements TaskDecorator {

    private final String id;

    public MDCTaskDecorator(String id) {
        this.id = id;
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        if (null == MDC.get(id)) {
            MDC.put(id, UUID.randomUUID().toString().replaceAll("-", ""));
        }
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                MDC.setContextMap(contextMap);
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
