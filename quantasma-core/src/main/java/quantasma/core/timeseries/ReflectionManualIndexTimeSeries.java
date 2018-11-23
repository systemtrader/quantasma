package quantasma.core.timeseries;

import org.ta4j.core.Bar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.Num;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;

public class ReflectionManualIndexTimeSeries implements ManualIndexTimeSeries {
    protected final TimeSeries timeSeries;

    protected ReflectionManualIndexTimeSeries(TimeSeries timeSeries) {
        this.timeSeries = timeSeries;
    }

    public static ReflectionManualIndexTimeSeries wrap(TimeSeries timeSeries) {
        return new ReflectionManualIndexTimeSeries(timeSeries);
    }

    protected boolean isIndexModified;

    @Override
    public void addBar(Bar bar, boolean replace) {
        if (isIndexModified) {
            throw new RuntimeException("Cannot add bars as indexes are already manipulated");
        }
        timeSeries.addBar(bar, replace);
    }

    @Override
    public void nextIndex() {
        if (stealEndIndex() == totalBarsCount() - 1) {
            throw new RuntimeException(String.format("No next bar available at index [%s] - bars count [%s]", stealBeginIndex() + 1, totalBarsCount()));
        }
        injectEndIndex(stealEndIndex() + 1);
    }

    @Override
    public void resetIndexes() {
        if (stealBeginIndex() < 0) {
            return;
        }

        setIndexModified();

        injectBeginIndex(0);
        injectEndIndex(-1);
    }

    protected void setIndexModified() {
        if (!isIndexModified) {
            isIndexModified = true;
        }
    }

    protected int totalBarsCount() {
        return getBarData().size();
    }

    protected int stealBeginIndex() {
        return (int) getField("seriesBeginIndex");
    }

    protected void injectBeginIndex(int value) {
        setField("seriesBeginIndex", value);
    }

    protected int stealEndIndex() {
        return (int) getField("seriesEndIndex");
    }

    protected void injectEndIndex(int value) {
        setField("seriesEndIndex", value);
    }

    private Object getField(String fieldName) {
        try {
            Class<?> clazz = getBaseTimeSeriesClassRef();
            final Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(timeSeries);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Checked exception", e);
        }
    }

    private void setField(String fieldName, Object value) {
        try {
            Class<?> clazz = getBaseTimeSeriesClassRef();
            final Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(timeSeries, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Checked exception", e);
        }
    }

    private Class<?> getBaseTimeSeriesClassRef() {
        Class<?> clazz = timeSeries.getClass();
        while (clazz != BaseTimeSeries.class) {
            clazz = clazz.getSuperclass();
            if (clazz == Object.class) {
                throw new RuntimeException();
            }
        }
        return clazz;
    }

    // methods below do not modify delegate's logic

    @Override
    public String getName() {
        return timeSeries.getName();
    }

    @Override
    public Bar getBar(int i) {
        return timeSeries.getBar(i);
    }

    @Override
    public int getBarCount() {
        return timeSeries.getBarCount();
    }

    @Override
    public List<Bar> getBarData() {
        return timeSeries.getBarData();
    }

    @Override
    public int getBeginIndex() {
        return timeSeries.getBeginIndex();
    }

    @Override
    public int getEndIndex() {
        return timeSeries.getEndIndex();
    }

    @Override
    public void setMaximumBarCount(int maximumBarCount) {
        timeSeries.setMaximumBarCount(maximumBarCount);
    }

    @Override
    public int getMaximumBarCount() {
        return timeSeries.getMaximumBarCount();
    }

    @Override
    public int getRemovedBarsCount() {
        return timeSeries.getRemovedBarsCount();
    }

    @Override
    public void addBar(Duration timePeriod, ZonedDateTime endTime) {
        timeSeries.addBar(timePeriod, endTime);
    }

    @Override
    public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume, Num amount) {
        timeSeries.addBar(endTime, openPrice, highPrice, lowPrice, closePrice, volume, amount);
    }

    @Override
    public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume) {
        timeSeries.addBar(timePeriod, endTime, openPrice, highPrice, lowPrice, closePrice, volume);
    }

    @Override
    public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume, Num amount) {
        timeSeries.addBar(timePeriod, endTime, openPrice, highPrice, lowPrice, closePrice, volume, amount);
    }

    @Override
    public void addTrade(Num tradeVolume, Num tradePrice) {
        timeSeries.addTrade(tradeVolume, tradePrice);
    }

    @Override
    public void addPrice(Num price) {
        timeSeries.addPrice(price);
    }

    @Override
    public TimeSeries getSubSeries(int startIndex, int endIndex) {
        return timeSeries.getSubSeries(startIndex, endIndex);
    }

    @Override
    public Num numOf(Number number) {
        return timeSeries.numOf(number);
    }

    @Override
    public Function<Number, Num> function() {
        return timeSeries.function();
    }

    @Override
    public Bar getFirstBar() {
        return timeSeries.getFirstBar();
    }

    @Override
    public Bar getLastBar() {
        return timeSeries.getLastBar();
    }

}
