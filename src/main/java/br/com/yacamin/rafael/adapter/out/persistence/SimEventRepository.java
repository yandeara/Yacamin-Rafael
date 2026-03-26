package br.com.yacamin.rafael.adapter.out.persistence;

import br.com.yacamin.rafael.domain.SimEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SimEventRepository extends MongoRepository<SimEvent, String> {
}
