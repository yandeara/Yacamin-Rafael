package br.com.yacamin.rafael.adapter.out.persistence;

import br.com.yacamin.rafael.domain.scylla.entity.Candle1Mn;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface Candle1MnRepository extends CassandraRepository<Candle1Mn, String> {

    Candle1Mn findFirstBySymbolOrderByOpenTimeDesc(String symbol);

    List<Candle1Mn> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqual(
            String symbol, Instant start, Instant end);

    void deleteBySymbolAndOpenTimeGreaterThan(String symbol, Instant openTime);
}
