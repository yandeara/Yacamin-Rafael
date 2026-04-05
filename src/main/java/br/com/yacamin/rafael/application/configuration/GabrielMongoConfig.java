package br.com.yacamin.rafael.application.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "br.com.yacamin.rafael.adapter.out.persistence.gabriel",
        mongoTemplateRef = "gabrielMongoTemplate"
)
public class GabrielMongoConfig {

    @Value("${spring.mongodb.uri}")
    private String uri;

    @Primary
    @Bean
    public MongoDatabaseFactory gabrielMongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(uri);
    }

    @Primary
    @Bean
    public MongoTemplate gabrielMongoTemplate(
            @Qualifier("gabrielMongoDatabaseFactory") MongoDatabaseFactory factory) {
        return new MongoTemplate(factory);
    }
}
