package com.ridelink.ridemanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.ridelink.ridemanagement.repository")
@EnableMongoAuditing
public class MongoConfig {
}
