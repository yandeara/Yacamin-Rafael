package br.com.yacamin.rafael.domain.scylla.repository.indicator.i15mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn.TrendIndicator15MnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.yacamin.rafael.domain.scylla.entity.IndicatorKey;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrendIndicator15MnRepository extends JpaRepository<TrendIndicator15MnEntity, IndicatorKey> {

    Optional<TrendIndicator15MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<TrendIndicator15MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

}
