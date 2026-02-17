package com.myvet.dataaccess.petlog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter
public class JsonbConverter implements AttributeConverter<Map<String, Object>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        try { return attribute == null ? null : mapper.writeValueAsString(attribute); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try { return dbData == null ? null : mapper.readValue(dbData, new TypeReference<>() {}); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
