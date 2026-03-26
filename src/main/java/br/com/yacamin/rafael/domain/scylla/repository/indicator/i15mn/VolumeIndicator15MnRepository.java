package br.com.yacamin.rafael.domain.scylla.repository.indicator.i15mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn.VolumeIndicator15MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VolumeIndicator15MnRepository extends CassandraRepository<VolumeIndicator15MnEntity, String> {

    Optional<VolumeIndicator15MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<VolumeIndicator15MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

}
