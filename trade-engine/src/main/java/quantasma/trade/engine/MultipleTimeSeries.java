package quantasma.trade.engine;

import org.ta4j.core.TimeSeries;
import quantasma.model.CandlePeriod;

import java.io.Serializable;
import java.time.ZonedDateTime;

public interface MultipleTimeSeries extends Serializable {
    void updateBar(ZonedDateTime priceDate, double bid, double ask);

    void updateBar(ZonedDateTime priceDate, double price);

    void createBar(ZonedDateTime priceDate);

    int lastBarIndex();

    String getInstrument();

    TimeSeries getTimeSeries(CandlePeriod period);
}
