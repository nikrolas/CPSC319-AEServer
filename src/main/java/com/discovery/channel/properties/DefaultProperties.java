package com.discovery.channel.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static String PROPERTIES_PATH = System.getProperty("propertiesPath", "./config/defaults.properties");

    private DefaultProperties() {}

    private static void init(){
        INSTANCE = new DefaultProperties();
        try {
            InputStream inputStream = new FileInputStream(PROPERTIES_PATH);
            INSTANCE.load(inputStream);
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
