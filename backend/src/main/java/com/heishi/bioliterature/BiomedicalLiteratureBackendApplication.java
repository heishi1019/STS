package com.heishi.bioliterature;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.heishi.bioliterature.mapper")
@SpringBootApplication
public class BiomedicalLiteratureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiomedicalLiteratureBackendApplication.class, args);
    }
}
