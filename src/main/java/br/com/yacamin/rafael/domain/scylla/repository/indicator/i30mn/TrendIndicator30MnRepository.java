package br.com.yacamin.rafael.domain.scylla.repository.indicator.i30mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.TrendIndicator30MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrendIndicator30MnRepository extends CassandraRepository<TrendIndicator30MnEntity, String> {

    Optional<TrendIndicator30MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<TrendIndicator30MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    List<TrendIndicator30MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqual(String symbol, Instant start, Instant end);

}
