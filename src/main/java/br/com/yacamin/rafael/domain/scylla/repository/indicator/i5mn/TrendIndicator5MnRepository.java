package br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.TrendIndicator5MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrendIndicator5MnRepository extends CassandraRepository<TrendIndicator5MnEntity, String> {

    Optional<TrendIndicator5MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<TrendIndicator5MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    Optional<TrendIndicator5MnEntity> findFirstBySymbolOrderByOpenTimeDesc(String symbol);

    void deleteBySymbolAndOpenTimeGreaterThan(String symbol, Instant openTime);
}
