package com.example.wagemanager.global.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 공휴일 관련 설정
 */
@Configuration
public class HolidayConfig {

    /**
     * XmlMapper 빈 생성
     * 공휴일 API XML 응답 파싱용
     */
    @Bean
    public XmlMapper xmlMapper() {
        return new XmlMapper();
    }
}
