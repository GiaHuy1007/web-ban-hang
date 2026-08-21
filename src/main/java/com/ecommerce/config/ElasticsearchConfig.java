package com.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(
        basePackages = "com.ecommerce.modules.search.repository"
)
public class ElasticsearchConfig {
}
