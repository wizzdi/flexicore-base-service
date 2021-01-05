/*******************************************************************************
 *  Copyright (C) FlexiCore, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Avishay Ben Natan And Asaf Ben Natan, October 2015
 ******************************************************************************/
package com.flexicore.init;

import com.flexicore.interfaces.ServicePlugin;
import com.flexicore.model.Clazz;
import com.flexicore.model.Tenant;
import com.flexicore.request.ClazzFilter;
import com.flexicore.request.TenantFilter;
import com.flexicore.service.BaseclassService;
import com.flexicore.service.impl.ClassScannerService;
import com.google.common.util.concurrent.AtomicDouble;
import com.wizzdi.flexicore.boot.base.events.PluginsLoadedEvent;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * this is used to make sure everything has been initialized,
 *
 * @author Asaf
 */

@Configuration
@Extension
public class Initializator implements ServicePlugin {

    private static final Logger logger = LoggerFactory.getLogger(Initializator.class.getCanonicalName());

    @Autowired
    private ClassScannerService classScannerService;

    @Value("${flexicore.entities:/home/flexicore/entities}")
    private String entitiesPath;
    @Value("${flexicore.plugins:/home/flexicore/plugins}")
    private String pluginsPath;
    @Value("${flexicore.upload:/home/flexicore/upload}")
    private String uploadPath;
    @Value("${flexicore.users.rootDirPath:/home/flexicore/users/}")
    private String usersPath;


    private static AtomicBoolean initFully = new AtomicBoolean(false);
    private static final AtomicBoolean init=new AtomicBoolean(false);

    public static boolean getInit() {
        return initFully.get();
    }

    public static void setInit() {
        initFully.compareAndSet(false, true);
    }

    public static void setRestarting() {
        initFully.compareAndSet(true, false);
    }


    /**
     * this is called by the container upon init.
     * @param pluginsLoadedEvent event
     * @throws Exception if there is any issue starting the context
     */
    @EventListener
    public void getStartingContext(PluginsLoadedEvent pluginsLoadedEvent) throws Exception {
        if(init.compareAndSet(false,true)){
            createFolderStructure();
            logger.info("registering classes");
            classScannerService.registerClasses();
            logger.info("Initializing classes");
            List<Clazz> clazzes = classScannerService.InitializeClazzes(); // must be done first!

            try {
                classScannerService.createDefaultObjects();
                logger.info("Initializing operations");
                classScannerService.InitializeOperations();

                classScannerService.createSwaggerTags();
                classScannerService.initializeInvokers();

                registerFilterClasses();


            } catch (Exception ex) {
                logger.error( "Error while initializing the system", ex);
            }
        }



    }


    private void registerFilterClasses() {
        BaseclassService.registerFilterClass(TenantFilter.class, Tenant.class);
        BaseclassService.registerFilterClass(ClazzFilter.class, Clazz.class);


    }


    private void createFolderStructure() {
        for (String path : Arrays.asList(entitiesPath, pluginsPath, uploadPath, usersPath)) {
            File file = new File(path);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    logger.warn( "failed creating path: " + file);
                }
            }
        }


    }


}
