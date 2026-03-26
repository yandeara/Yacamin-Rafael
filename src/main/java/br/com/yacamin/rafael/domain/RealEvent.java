package br.com.yacamin.rafael.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Evento na collection `events` (padrão Gabriel).
 * Sem campo algorithm (Gabriel não tem).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "events")
public class RealEvent {

    @Id
    private String id;

    private String slug;
    private long timestamp;
    private String type;
    private Object payload;
}
