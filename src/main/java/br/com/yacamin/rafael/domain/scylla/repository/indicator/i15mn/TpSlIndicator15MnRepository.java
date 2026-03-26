package br.com.yacamin.rafael.domain.scylla.repository.indicator.i15mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn.TpSlIndicator15MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.TpSlIndicator30MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TpSlIndicator15MnRepository extends CassandraRepository<TpSlIndicator15MnEntity, String> {

    Optional<TpSlIndicator15MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<TpSlIndicator15MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    List<TpSlIndicator15MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqual(String symbol, Instant start, Instant end);

}
