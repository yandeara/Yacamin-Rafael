package br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.MomentumIndicator5MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MomentumIndicator5MnRepository extends CassandraRepository<MomentumIndicator5MnEntity, String> {

    Optional<MomentumIndicator5MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<MomentumIndicator5MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    Optional<MomentumIndicator5MnEntity> findFirstBySymbolOrderByOpenTimeDesc(String symbol);

    void deleteBySymbolAndOpenTimeGreaterThan(String symbol, Instant openTime);
}
