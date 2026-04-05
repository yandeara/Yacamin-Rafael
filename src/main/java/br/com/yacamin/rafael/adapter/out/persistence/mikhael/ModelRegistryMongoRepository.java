package br.com.yacamin.rafael.adapter.out.persistence.mikhael;

import br.com.yacamin.rafael.domain.mongo.document.ModelRegistryDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ModelRegistryMongoRepository {

    private static final String COLLECTION = "model_registry";

    @Qualifier("mikhaelMongoTemplate")
    private final MongoTemplate mongoTemplate;

    public List<ModelRegistryDocument> findAll() {
        return mongoTemplate.findAll(ModelRegistryDocument.class, COLLECTION);
    }

    public Optional<ModelRegistryDocument> findByFileName(String fileName) {
        Query query = new Query(Criteria.where("fileName").is(fileName));
        return Optional.ofNullable(
                mongoTemplate.findOne(query, ModelRegistryDocument.class, COLLECTION));
    }

    public void save(ModelRegistryDocument doc) {
        mongoTemplate.save(doc, COLLECTION);
    }

    public void deleteByFileName(String fileName) {
        Query query = new Query(Criteria.where("fileName").is(fileName));
        mongoTemplate.remove(query, COLLECTION);
    }
}
