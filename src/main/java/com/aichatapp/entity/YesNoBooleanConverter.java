package com.aichatapp.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Maps Oracle CHAR(1) 'Y'/'N' to a Java boolean.
 * Oracle has no BOOLEAN column type before 23ai, so Y/N flags are the usual convention.
 */
@Converter
public class YesNoBooleanConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return "Y".equalsIgnoreCase(dbValue.trim());
    }
}
