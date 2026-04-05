package br.com.yacamin.rafael.adapter.out.persistence.gabriel;

import br.com.yacamin.rafael.domain.MarketGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MarketGroupRepository extends MongoRepository<MarketGroup, String> {

    Optional<MarketGroup> findBySlugPrefix(String slugPrefix);
}
