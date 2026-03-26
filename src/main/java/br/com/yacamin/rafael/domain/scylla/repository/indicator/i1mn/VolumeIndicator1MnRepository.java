package br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn.VolumeIndicator1MnEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolumeIndicator1MnRepository extends CassandraRepository<VolumeIndicator1MnEntity, String> {

    Optional<VolumeIndicator1MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<VolumeIndicator1MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);


    Optional<VolumeIndicator1MnEntity> findFirstBySymbolOrderByOpenTimeDesc(String symbol);

    void deleteBySymbolAndOpenTimeGreaterThan(String symbol, java.time.Instant openTime);
}
