package br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn.TrendIndicator1MnEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrendIndicator1MnRepository extends CassandraRepository<TrendIndicator1MnEntity, String> {

    Optional<TrendIndicator1MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<TrendIndicator1MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);


    Optional<TrendIndicator1MnEntity> findFirstBySymbolOrderByOpenTimeDesc(String symbol);

    void deleteBySymbolAndOpenTimeGreaterThan(String symbol, java.time.Instant openTime);
}
