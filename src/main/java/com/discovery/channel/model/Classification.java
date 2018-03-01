package com.discovery.channel.model;

import com.discovery.channel.database.ClassificationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Classification {
    private static final Logger LOGGER = LoggerFactory.getLogger(Classification.class);

    private static final String CLASSIFICATION_SEPARATOR = "/";

    private int id;
    private String name;
    private CLASSIFICATION_TYPE keyword;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CLASSIFICATION_TYPE getKeyword() {
        return keyword;
    }

    public Classification(int id, String name, CLASSIFICATION_TYPE keyWord) {
        this.id = id;
        this.name = name;
        this.keyword = keyWord;
    }

    public enum CLASSIFICATION_TYPE {
        // T means valid for root type
        T("T"), F("F");

        String name;
        CLASSIFICATION_TYPE(String name) {
            this.name = name;
        }

        public static CLASSIFICATION_TYPE fromName(String name) {
            switch (name) {
                case "T" :
                    return T;
                case "F" :
                    return F;
                default:
                    return null;
            }
        }

        @Override
        public String toString(){
            return name;
        }
    }

    /**
     * Return true iff classification string is valid, that is
     *  a. A record has at least two valid classifications
     *  b. The root classifications must have type "T"
     * @param classificationStr
     * @return
     * @throws SQLException
     */
    public static boolean validateClassification(String classificationStr) throws SQLException {
        // At least two classifications
        String[] classificationNames = classificationStr.split(CLASSIFICATION_SEPARATOR);
        if (classificationNames.length < 2) {
            LOGGER.info("Classification String {} invalid. Need at least two classifications", classificationStr);
            return false;
        }

        // Make sure they exist in Database
        List<Classification> classifications = new ArrayList<>();
        for (String name : classificationNames) {
            Classification classification = ClassificationController.findClassificationByName(name);
            if (classification == null) {
                LOGGER.info("Could not find classification with name {}", name);
                return false;
            }
            classifications.add(classification);
        }

        // Root classification must be of type 'T'
        if (classifications.get(0).getKeyword() != CLASSIFICATION_TYPE.T) {
            LOGGER.info("Classification String {} invalid. Root must be of type 'T'", classificationStr);
            return false;
        }

        return true;
    }

    /**
     * Parse classification string, find them in DB, and return a list of Ids (in order)
     *
     * @param classificationStr
     * @return
     */
    public static List<Integer> parseClassificationStrToIds(String classificationStr) {
        List<Integer> ids = new ArrayList<>();
        Arrays.stream(classificationStr.split(CLASSIFICATION_SEPARATOR)).forEach(aClassification -> {
            try {
                ids.add(ClassificationController.findClassificationByName(aClassification.trim()).getId());
            } catch (SQLException e) {
                LOGGER.error("Failed to query classification with name {}", aClassification);
            }
        });
        return ids;
    }

    /**
     * Build classification string. Format "<class1>/<class2>/<class3>..."
     *
     * @param classifications
     * @return
     */
    public static String buildClassificationString(List<Classification> classifications) {
        List<String> classificationStrs = classifications.
                stream().
                map(Classification::getName).
                collect(Collectors.toList());
        return String.join(CLASSIFICATION_SEPARATOR, classificationStrs);
    }
}
