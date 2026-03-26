package br.com.yacamin.rafael.adapter.out.persistence;

import br.com.yacamin.rafael.domain.BinanceStreamConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BinanceStreamConfigRepository extends MongoRepository<BinanceStreamConfig, String> {

    Optional<BinanceStreamConfig> findByStreamCode(String streamCode);

    List<BinanceStreamConfig> findByActiveTrue();
}
