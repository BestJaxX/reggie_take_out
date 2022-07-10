package com.itheima.common.converter;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

import lombok.SneakyThrows;

public class String2DateConverter implements Converter<String, Date> {
    @SneakyThrows
    @Override
    public Date convert(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(s);
    }
}

