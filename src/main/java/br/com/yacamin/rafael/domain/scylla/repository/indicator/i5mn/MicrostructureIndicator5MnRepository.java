package br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.MicrostructureIndicator5MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MicrostructureIndicator5MnRepository extends CassandraRepository<MicrostructureIndicator5MnEntity, String> {

    Optional<MicrostructureIndicator5MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<MicrostructureIndicator5MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    Optional<MicrostructureIndicator5MnEntity> findFirstBySymbolOrderByOpenTimeDesc(String symbol);

    void deleteBySymbolAndOpenTimeGreaterThan(String symbol, Instant openTime);
}
