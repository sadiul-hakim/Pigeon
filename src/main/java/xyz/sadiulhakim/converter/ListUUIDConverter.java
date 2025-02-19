package xyz.sadiulhakim.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Converter
@RequiredArgsConstructor
public class ListUUIDConverter implements AttributeConverter<List<UUID>, String> {

    private final ObjectMapper mapper;

    @Override
    public String convertToDatabaseColumn(List<UUID> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception ex) {
            return "";
        }
    }

    @Override
    public List<UUID> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
}
