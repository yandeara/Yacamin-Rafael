package br.com.yacamin.rafael.domain.scylla.repository.indicator.i30mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.VolumeIndicator30MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.VolumeIndicator5MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VolumeIndicator30MnRepository extends CassandraRepository<VolumeIndicator30MnEntity, String> {

    Optional<VolumeIndicator30MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<VolumeIndicator30MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    List<VolumeIndicator30MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqual(String symbol, Instant start, Instant end);

}
