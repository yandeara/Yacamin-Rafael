package br.com.yacamin.rafael.domain.scylla.repository.indicator.i30mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.MicrostructureIndicator30MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.VolumeIndicator30MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MicrostructureIndicator30MnRepository extends CassandraRepository<MicrostructureIndicator30MnEntity, String> {

    Optional<MicrostructureIndicator30MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<MicrostructureIndicator30MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    List<MicrostructureIndicator30MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqual(String symbol, Instant start, Instant end);


}
