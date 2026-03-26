package br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn.TpSlIndicator1MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.TpSlIndicator5MnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.yacamin.rafael.domain.scylla.entity.IndicatorKey;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TpSlIndicator1MnRepository extends JpaRepository<TpSlIndicator1MnEntity, IndicatorKey> {

    Optional<TpSlIndicator1MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<TpSlIndicator1MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    List<TpSlIndicator1MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqual(String symbol, Instant start, Instant end);


    Optional<TpSlIndicator1MnEntity> findFirstBySymbolOrderByOpenTimeDesc(String symbol);

    void deleteBySymbolAndOpenTimeGreaterThan(String symbol, java.time.Instant openTime);
}
