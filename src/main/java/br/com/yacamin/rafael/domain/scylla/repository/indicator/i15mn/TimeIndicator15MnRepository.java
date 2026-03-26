package br.com.yacamin.rafael.domain.scylla.repository.indicator.i15mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn.TimeIndicator15MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeIndicator15MnRepository extends CassandraRepository<TimeIndicator15MnEntity, String> {

    Optional<TimeIndicator15MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<TimeIndicator15MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

}
