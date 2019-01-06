package quantasma.integrations.data.provider.dukascopy;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import lombok.extern.slf4j.Slf4j;
import quantasma.core.Quote;
import quantasma.integrations.event.AccountState;
import quantasma.integrations.event.Direction;
import quantasma.integrations.event.Event;
import quantasma.integrations.event.EventSink;
import quantasma.integrations.event.OpenedPosition;
import quantasma.integrations.util.MathUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class TransferLiveDataStrategy implements IStrategy {

    private final EventSink eventSink;

    private IEngine engine;
    private IHistory history;
    private IAccount account;

    public TransferLiveDataStrategy(EventSink eventSink) {
        this.eventSink = eventSink;
    }

    @Override
    public void onStart(IContext context) {
        log.info("Starting live data strategy");
        engine = context.getEngine();
        history = context.getHistory();
        account = context.getAccount();
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
        double profitLoss = 0;
        double totalAmount = 0;

        final List<OpenedPosition> openedPositions = new LinkedList<>();
        for (IOrder order : engine.getOrders()) {
            if (order.getState() == IOrder.State.FILLED) {
                profitLoss += order.getProfitLossInAccountCurrency();
                totalAmount += order.getAmount();
                openedPositions.add(createOpenedPosition(order));
            }
        }

        eventSink.flush(Event.openedPositions(openedPositions));
        eventSink.flush(Event.accountState(
                new AccountState(history.getEquity(),
                                 account.getBalance(),
                                 MathUtils.round(profitLoss, 2),
                                 MathUtils.round(totalAmount, 3),
                                 MathUtils.round(account.getUsedMargin(), 2),
                                 account.getAccountCurrency().getCurrencyCode(),
                                 account.getLeverage())));
    }

    private static OpenedPosition createOpenedPosition(IOrder order) {
        return new OpenedPosition(
                order.getInstrument().getName(),
                order.getOrderCommand().isLong() ? Direction.LONG : Direction.SHORT,
                order.getAmount(),
                order.getOpenPrice(),
                order.getStopLossPrice(),
                order.getTakeProfitPrice(),
                order.getProfitLossInPips(),
                order.getProfitLossInAccountCurrency());
    }

    @Override
    public void onMessage(IMessage message) {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        eventSink.flush(Event.quote(
                Quote.bidAsk(symbol(instrument),
                             time(tick),
                             tick.getBid(),
                             tick.getAsk())));

        onAccount(account); // trigger account-related events
    }

    private static String symbol(Instrument instrument) {
        return instrument.getPrimaryJFCurrency().getCurrencyCode() + instrument.getSecondaryJFCurrency().getCurrencyCode();
    }

    private static ZonedDateTime time(ITick tick) {
        return Instant.ofEpochMilli(tick.getTime()).atZone(ZoneOffset.UTC);
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
    }

}