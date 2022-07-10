package com.itheima.common.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;

public class String2LocalDateTimeConverter implements Converter<String, LocalDateTime>  {
	@Override
    public LocalDateTime convert(String s) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(s, fmt);
    }

}
