package br.com.yacamin.rafael.domain.scylla.repository.indicator.i30mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.TimeIndicator30MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.VolumeIndicator30MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.TimeIndicator5MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeIndicator30MnRepository extends CassandraRepository<TimeIndicator30MnEntity, String> {

    Optional<TimeIndicator30MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<TimeIndicator30MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    List<TimeIndicator30MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqual(String symbol, Instant start, Instant end);

}
