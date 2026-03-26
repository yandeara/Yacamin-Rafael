package br.com.yacamin.rafael.adapter.out.persistence;

import br.com.yacamin.rafael.domain.RealEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RealEventRepository extends MongoRepository<RealEvent, String> {
}
