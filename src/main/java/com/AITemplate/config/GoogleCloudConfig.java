package com.AITemplate.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class GoogleCloudConfig {

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        return ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/service-account.json"));
    }
}
