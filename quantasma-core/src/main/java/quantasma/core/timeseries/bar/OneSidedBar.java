package quantasma.core.timeseries.bar;

import org.ta4j.core.Bar;
import org.ta4j.core.num.Num;
import quantasma.core.Quote;

import java.util.function.Function;

public interface OneSidedBar extends Bar {

    default void updateBar(Quote quote, Function<Number, Num> numFunction) {
        addPrice(numFunction.apply(quote.getBid()));
    }

    Function<Number, Num> function();

    Num getOpenPrice();

    Num getMinPrice();

    Num getMaxPrice();

    Num getClosePrice();
}
