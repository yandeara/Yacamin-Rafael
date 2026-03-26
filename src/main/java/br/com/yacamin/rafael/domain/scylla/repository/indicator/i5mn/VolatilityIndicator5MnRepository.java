package br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.VolatilityIndicator5MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VolatilityIndicator5MnRepository extends CassandraRepository<VolatilityIndicator5MnEntity, String> {

    Optional<VolatilityIndicator5MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<VolatilityIndicator5MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    Optional<VolatilityIndicator5MnEntity> findFirstBySymbolOrderByOpenTimeDesc(String symbol);

    void deleteBySymbolAndOpenTimeGreaterThan(String symbol, Instant openTime);
}
