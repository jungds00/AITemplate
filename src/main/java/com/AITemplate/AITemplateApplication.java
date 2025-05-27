package com.AITemplate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.AITemplate.repository") // JPA Repository 패키지
@EnableElasticsearchRepositories(basePackages = "com.AITemplate.elasticsearch") // Elasticsearch Repository 패키지
public class AITemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(AITemplateApplication.class, args);
    }
}
