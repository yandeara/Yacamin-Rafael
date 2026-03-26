package br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn.VolatilityIndicator1MnEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolatilityIndicator1MnRepository extends CassandraRepository<VolatilityIndicator1MnEntity, String> {

    Optional<VolatilityIndicator1MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<VolatilityIndicator1MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);


    Optional<VolatilityIndicator1MnEntity> findFirstBySymbolOrderByOpenTimeDesc(String symbol);

    void deleteBySymbolAndOpenTimeGreaterThan(String symbol, java.time.Instant openTime);
}
