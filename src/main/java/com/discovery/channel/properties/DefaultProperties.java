package com.discovery.channel.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Qiushan on 2018/1/26.
 */
public class DefaultProperties extends Properties{

    private static Logger logger = LoggerFactory.getLogger(DefaultProperties.class);

    private static DefaultProperties INSTANCE;

    private static Resource PROPERTIES_RESOURCE = new ClassPathResource("defaults.properties");

    // Hide constructor
    private DefaultProperties() {}

    private static void init(){
        INSTANCE = new DefaultProperties();
        try {
            logger.info("Initialising system properties from path {}", PROPERTIES_RESOURCE.getURL());

            File PROPERTIES_FILE = PROPERTIES_RESOURCE.getFile();
            InputStream properties = new FileInputStream(PROPERTIES_FILE);
            INSTANCE.load(properties);
        } catch (IOException e) {
            logger.error("Failed to load system properties", e.getMessage());
        }
    }

    public static synchronized DefaultProperties getInstance() {
        if (INSTANCE == null) {
            init();
        }
        return INSTANCE;
    }

    public int getIntProperty(String key) {
        String strVal = getInstance().getProperty(key);
        if (strVal != null) {
            try {
                return Integer.valueOf(strVal);
            } catch (NumberFormatException e) {
                logger.error("Require int value for property {}", key);
            }
        }
        return -1;
    }
}
