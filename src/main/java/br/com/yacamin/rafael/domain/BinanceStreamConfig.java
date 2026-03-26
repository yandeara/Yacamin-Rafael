package br.com.yacamin.rafael.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "binance_stream_config")
public class BinanceStreamConfig {

    @Id
    private String id;

    private String streamCode;

    private boolean active;
}
