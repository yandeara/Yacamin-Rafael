package br.com.yacamin.rafael.domain.scylla.repository.indicator.i15mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn.MicrostructureIndicator15MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MicrostructureIndicator15MnRepository extends CassandraRepository<MicrostructureIndicator15MnEntity, String> {

    Optional<MicrostructureIndicator15MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<MicrostructureIndicator15MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

}
