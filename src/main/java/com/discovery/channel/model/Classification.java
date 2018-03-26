package com.discovery.channel.model;

import com.discovery.channel.database.ClassificationController;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class Classification {
    private static final Logger LOGGER = LoggerFactory.getLogger(Classification.class);

    private static final String CLASSIFICATION_SEPARATOR = "/";

    private int id;
    private String name;
    private CLASSIFICATION_TYPE keyword;

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
     * @param classIds
     * @return
     * @throws SQLException
     */
    public static boolean validateClassification(List<Integer> classIds) throws SQLException {
        if (StringUtils.isEmpty(classIds)) {
            return false;
        }
        // At least two classifications
        if (classIds.size() < 2) {
            LOGGER.info("Classification String {} invalid. Need at least two classifications", classIds);
            return false;
        }

        // Make sure they exist in Database
        List<Classification> classifications = new ArrayList<>();
        for (Integer id : classIds) {
            Classification classification = ClassificationController.findClassificationById(id);
            if (classification == null) {
                LOGGER.info("Could not find classification with id {}", id);
                return false;
            }
            classifications.add(classification);
        }

        return validateClassHierarchy(classifications);
    }

    private static boolean validateClassHierarchy(List<Classification> classifications) throws SQLException {
        // Root classification must be of type 'T'
        if (classifications.get(0).getKeyword() != CLASSIFICATION_TYPE.T) {
            LOGGER.info("Classification invalid. Root must be of type 'T'", classifications.get(0).name);
            return false;
        }

        for (int i = 0; i < classifications.size() - 1; i ++) {
            Classification classification = classifications.get(i);
            List<Integer> validChildren = ClassificationController.findChildrenClassificationIds(classification.id);
            if (!validChildren.contains(classifications.get(i + 1).id)) {
                LOGGER.info("Classification {} is not a valid child classification for {}", classifications.get(i + 1).id, classification.id);
                return false;
            }
        }

        return true;
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
