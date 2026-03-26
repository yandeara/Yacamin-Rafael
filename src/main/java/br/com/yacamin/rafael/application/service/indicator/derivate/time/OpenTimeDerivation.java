package br.com.yacamin.rafael.application.service.indicator.derivate.time;

import br.com.yacamin.rafael.domain.SymbolCandle;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;

@Service
public class OpenTimeDerivation {

    /**
     * minute_of_day ∈ [0,1439]
     */
    public double minuteOfDay(SymbolCandle c) {
        var z = c.getOpenTime().atZone(ZoneOffset.UTC);
        return (double) (z.getHour() * 60 + z.getMinute());
    }

    /**
     * day_of_week ∈ [1..7]
     */
    public double dayOfWeek(SymbolCandle c) {
        return (double) c.getOpenTime().atZone(ZoneOffset.UTC).getDayOfWeek().getValue();
    }

    /**
     * sessionAsia: 1 se hora < 8, senão 0
     */
    public double sessionAsia(SymbolCandle c) {
        int hour = c.getOpenTime().atZone(ZoneOffset.UTC).getHour();
        return hour < 8 ? 1.0 : 0.0;
    }

    /**
     * sessionEurope: 1 se 07 ≤ hora < 13
     */
    public double sessionEurope(SymbolCandle c) {
        int hour = c.getOpenTime().atZone(ZoneOffset.UTC).getHour();
        return (hour >= 7 && hour < 13) ? 1.0 : 0.0;
    }

    /**
     * sessionNy: 1 se 12 ≤ hora < 20
     */
    public double sessionNy(SymbolCandle c) {
        int hour = c.getOpenTime().atZone(ZoneOffset.UTC).getHour();
        return (hour >= 12 && hour < 20) ? 1.0 : 0.0;
    }

    /**
     * sinTime = sin(2π * minuteOfDay/1440)
     */
    public double sinTime(SymbolCandle c) {
        double mod = minuteOfDay(c);
        double angle = (2.0 * Math.PI * (mod / 1440.0));
        return Math.sin(angle);
    }

    /**
     * cosTime = cos(2π * minuteOfDay/1440)
     */
    public double cosTime(SymbolCandle c) {
        double mod = minuteOfDay(c);
        double angle = (2.0 * Math.PI * (mod / 1440.0));
        return Math.cos(angle);
    }

    // =========================================================================
    // V3 ADDITIONS
    // =========================================================================

    /**
     * day_of_month ∈ [1..31]
     */
    public double dayOfMonth(SymbolCandle c) {
        return (double) c.getOpenTime().atZone(ZoneOffset.UTC).getDayOfMonth();
    }

    /**
     * sinDayOfWeek = sin(2π * (dow-1)/7)
     * dow em [1..7] => fase em [0..6]
     */
    public double sinDayOfWeek(SymbolCandle c) {
        double dow = dayOfWeek(c) - 1.0; // [0..6]
        double angle = 2.0 * Math.PI * (dow / 7.0);
        return Math.sin(angle);
    }

    /**
     * cosDayOfWeek = cos(2π * (dow-1)/7)
     */
    public double cosDayOfWeek(SymbolCandle c) {
        double dow = dayOfWeek(c) - 1.0; // [0..6]
        double angle = 2.0 * Math.PI * (dow / 7.0);
        return Math.cos(angle);
    }

    /**
     * overlap_asia_eur: 1 quando Asia e Europe estão "ao mesmo tempo"
     * Definição coerente com suas sessões atuais:
     * - Asia: hour < 8
     * - Europe: 7 <= hour < 13
     * Overlap => hour == 7
     */
    public double overlapAsiaEur(SymbolCandle c) {
        int hour = c.getOpenTime().atZone(ZoneOffset.UTC).getHour();
        return (hour == 7) ? 1.0 : 0.0;
    }

    /**
     * overlap_eur_ny: 1 quando Europe e NY estão "ao mesmo tempo"
     * Definição coerente com suas sessões atuais:
     * - Europe: 7 <= hour < 13
     * - NY:     12 <= hour < 20
     * Overlap => hour == 12
     */
    public double overlapEurNy(SymbolCandle c) {
        int hour = c.getOpenTime().atZone(ZoneOffset.UTC).getHour();
        return (hour == 12) ? 1.0 : 0.0;
    }

    /**
     * candle_in_h1: posição do candle de 15m dentro da hora.
     * Retorna {0,1,2,3} para minutos {0,15,30,45}.
     */
    public double candleInH1(SymbolCandle c) {
        int minute = c.getOpenTime().atZone(ZoneOffset.UTC).getMinute();
        return (double) (minute / 15); // 0..3
    }
}
