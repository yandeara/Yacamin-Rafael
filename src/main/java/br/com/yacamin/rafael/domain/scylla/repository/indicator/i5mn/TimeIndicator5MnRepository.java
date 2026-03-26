package br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.TimeIndicator5MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeIndicator5MnRepository extends CassandraRepository<TimeIndicator5MnEntity, String> {

    Optional<TimeIndicator5MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<TimeIndicator5MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    Optional<TimeIndicator5MnEntity> findFirstBySymbolOrderByOpenTimeDesc(String symbol);

    void deleteBySymbolAndOpenTimeGreaterThan(String symbol, Instant openTime);
}
