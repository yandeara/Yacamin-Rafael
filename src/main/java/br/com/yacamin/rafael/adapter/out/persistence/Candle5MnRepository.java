package br.com.yacamin.rafael.adapter.out.persistence;

import br.com.yacamin.rafael.domain.scylla.entity.Candle5Mn;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.yacamin.rafael.domain.scylla.entity.IndicatorKey;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface Candle5MnRepository extends JpaRepository<Candle5Mn, IndicatorKey> {

    Candle5Mn findFirstBySymbolOrderByOpenTimeDesc(String symbol);

    List<Candle5Mn> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqual(
            String symbol, Instant start, Instant end);

    void deleteBySymbolAndOpenTimeGreaterThan(String symbol, Instant openTime);
}
