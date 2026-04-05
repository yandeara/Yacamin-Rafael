package br.com.yacamin.rafael.application.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "br.com.yacamin.rafael.adapter.out.persistence.mikhael",
        mongoTemplateRef = "mikhaelMongoTemplate"
)
public class MikhaelMongoConfig {

    @Value("${mongodb.mikhael.uri}")
    private String uri;

    @Bean
    public MongoDatabaseFactory mikhaelMongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(uri);
    }

    @Bean
    public MongoTemplate mikhaelMongoTemplate(
            @Qualifier("mikhaelMongoDatabaseFactory") MongoDatabaseFactory factory) {
        return new MongoTemplate(factory);
    }
}
