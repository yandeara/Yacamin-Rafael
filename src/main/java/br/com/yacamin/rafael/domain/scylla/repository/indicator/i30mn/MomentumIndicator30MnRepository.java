package br.com.yacamin.rafael.domain.scylla.repository.indicator.i30mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.MomentumIndicator30MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.VolumeIndicator30MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.MomentumIndicator5MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MomentumIndicator30MnRepository extends CassandraRepository<MomentumIndicator30MnEntity, String> {

    Optional<MomentumIndicator30MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<MomentumIndicator30MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    List<MomentumIndicator30MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqual(String symbol, Instant start, Instant end);

}
