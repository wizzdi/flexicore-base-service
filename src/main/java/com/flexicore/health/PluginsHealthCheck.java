package com.flexicore.health;

import com.flexicore.data.jsoncontainers.PluginType;
import com.flexicore.interfaces.HealthCheckPlugin;
import com.flexicore.model.ModuleManifest;
import com.flexicore.service.impl.PluginService;
import org.pf4j.Extension;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;


@Primary
@Extension
@Component
public class PluginsHealthCheck implements HealthCheckPlugin {

    private static final Logger logger = LoggerFactory.getLogger(PluginsHealthCheck.class);

    @Lazy
    @Autowired
    private PluginService pluginService;

    @Autowired
    @Lazy
    private PluginManager pluginManager;

    @PostConstruct
    private void init() {
        logger.info("plugins health check post construct called");
    }

    @Override
    public Health health() {
        Health.Builder responseBuilder = Health.up();
        if (pluginService != null) {
            for (PluginWrapper pluginWrapper : pluginManager.getStartedPlugins()) {
                String version = pluginWrapper.getDescriptor() != null ? pluginWrapper.getDescriptor().getVersion() : "unknown";
                responseBuilder.withDetail(pluginWrapper.getPluginId() + "(" + PluginType.Service.name() + ")", version);
            }
            for (ModuleManifest externalModule : com.flexicore.service.PluginService.externalModules) {
                String version = externalModule.getVersion() != null ? externalModule.getVersion() : "unknown";
                responseBuilder.withDetail(externalModule.getUuid() + "(" + PluginType.External.name() + ")", version);
            }
        }

        return responseBuilder.build();

    }

}
