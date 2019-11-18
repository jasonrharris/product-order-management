package com.jasonrharris.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.   Date;
import java.time.LocalDateTime;

@SuppressWarnings("unused") // its auto applied
@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Date> {

    @Override
    public Date convertToDatabaseColumn(LocalDateTime locDate) {
        return locDate == null ? null : Date.from(locDate.toInstant(ZoneOffset.UTC));
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Date sqlDate) {
        return sqlDate == null ? null : LocalDateTime.ofInstant(sqlDate.toInstant(), ZoneId.of("UTC"));
    }
}
