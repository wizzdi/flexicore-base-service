package com.flexicore.runningentities;


import com.flexicore.interfaces.FlexiCoreService;
import com.flexicore.model.FileResource;
import com.flexicore.service.impl.FileResourceService;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


@Primary
@Extension
@Component
public class FilesCleaner implements FlexiCoreService, Runnable {

    private boolean stop;
    @Autowired
    private FileResourceService fileResourceService;
    private Logger logger = Logger.getLogger(getClass().getCanonicalName());

    @Value("${flexicore.files.cleaner.checkInterval:3600000}")
    private long deletedFileCleanInterval;

    @Override
    public void run() {
        logger.info("file cleaner started");
        while (!stop) {
            try {
                List<FileResource> files = fileResourceService.getFileResourceScheduledForDelete(OffsetDateTime.now());
                Set<String> deleted = new HashSet<>();
                Set<String> failed = new HashSet<>();
                try {
                    for (FileResource fileResource : files) {
                        File file = new File(fileResource.getFullPath());
                        if (file.delete()) {
                            deleted.add(file.getAbsolutePath());
                        } else {
                            failed.add(file.getAbsolutePath());
                        }
                        fileResource.setSoftDelete(true);
                    }
                    fileResourceService.massMerge(files);
                    if (!deleted.isEmpty()) {
                        logger.info("deleted files: " + deleted);
                    }
                    if (!failed.isEmpty()) {
                        logger.severe("Failed Deleting files: " + failed);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "cleaning files throed unexpected exception ", e);
                }
                Thread.sleep(deletedFileCleanInterval);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "interrupted while sleeping", e);
                stop = true;
            }

        }
        logger.info("file cleaner stopped");

    }

    public void stop() {
        stop = true;
    }
}
