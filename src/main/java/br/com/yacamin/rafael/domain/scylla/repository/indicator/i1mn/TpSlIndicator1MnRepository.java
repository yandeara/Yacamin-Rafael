package br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn.TpSlIndicator1MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.TpSlIndicator5MnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TpSlIndicator1MnRepository extends CassandraRepository<TpSlIndicator1MnEntity, String> {

    Optional<TpSlIndicator1MnEntity> findBySymbolAndOpenTime(String symbol, Instant openTime);

    List<TpSlIndicator1MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(String symbol, Instant start, Instant end);

    List<TpSlIndicator1MnEntity> findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqual(String symbol, Instant start, Instant end);


    Optional<TpSlIndicator1MnEntity> findFirstBySymbolOrderByOpenTimeDesc(String symbol);

    void deleteBySymbolAndOpenTimeGreaterThan(String symbol, java.time.Instant openTime);
}
