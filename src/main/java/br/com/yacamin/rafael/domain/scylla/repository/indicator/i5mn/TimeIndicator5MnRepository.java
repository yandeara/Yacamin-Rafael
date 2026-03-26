package br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.TimeIndicator5MnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.yacamin.rafael.domain.scylla.entity.IndicatorKey;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeIndicator5MnRepository extends JpaRepository<TimeIndicator5MnEntity, IndicatorKey> {

    Optional<TimeIndicator5MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<TimeIndicator5MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    Optional<TimeIndicator5MnEntity> findFirstBySymbolOrderByOpenTimeDesc(String symbol);

    void deleteBySymbolAndOpenTimeGreaterThan(String symbol, Instant openTime);
}
