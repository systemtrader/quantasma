package quantasma.core.timeseries;

import org.ta4j.core.Bar;
import quantasma.core.BarPeriod;
import quantasma.core.timeseries.bar.NaNBar;

public class BaseAggregatedTimeSeries extends BaseDescribedTimeSeries implements AggregatedTimeSeries {
    private final MainTimeSeries mainTimeSeries;

    protected BaseAggregatedTimeSeries(MainTimeSeries mainTimeSeries, String name, String symbol, BarPeriod barPeriod) {
        super(name, symbol, barPeriod);
        this.mainTimeSeries = mainTimeSeries;
    }

    protected BaseAggregatedTimeSeries(MainTimeSeries mainTimeSeries, String name, String symbol, BarPeriod barPeriod, int maxBarCount) {
        super(name, symbol, barPeriod, maxBarCount);
        this.mainTimeSeries = mainTimeSeries;
    }

    @Override
    public void addBar(Bar bar, boolean replace) {
        super.addBar(bar, replace);
    }

    @Override
    public Bar getFirstBar() {
        // avoid index manipulation
        return super.getBar(getBeginIndex());
    }

    @Override
    public Bar getLastBar() {
        // avoid index manipulation
        return super.getBar(getEndIndex());
    }

    @Override
    public Bar getBar(int index) {
        if (index == mainTimeSeries.getEndIndex()) {
            return getLastBar();
        }

        final int nthOldElement = mainTimeSeries.getEndIndex() - index;

        if (nthOldElement < getBarCount()) {
            return super.getBar(getEndIndex() - nthOldElement);
        }

        return NaNBar.NaN;
    }

    @Override
    public MainTimeSeries getMainTimeSeries() {
        return mainTimeSeries;
    }

}