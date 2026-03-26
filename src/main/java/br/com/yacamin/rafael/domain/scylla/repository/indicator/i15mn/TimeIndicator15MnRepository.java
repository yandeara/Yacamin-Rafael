package br.com.yacamin.rafael.domain.scylla.repository.indicator.i15mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn.TimeIndicator15MnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.yacamin.rafael.domain.scylla.entity.IndicatorKey;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeIndicator15MnRepository extends JpaRepository<TimeIndicator15MnEntity, IndicatorKey> {

    Optional<TimeIndicator15MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<TimeIndicator15MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

}
