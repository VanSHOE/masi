import java.util.List;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;

/**
 * This is your negotiation party.
 */
public class askBest extends AbstractNegotiationParty {

	private Bid lastReceivedBid = null;

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		System.out.println("Discount Factor is " + getUtilitySpace().getDiscountFactor());
		System.out.println("Reservation Value is " + getUtilitySpace().getReservationValueUndiscounted());
	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		Bid maxBid = null;
		try {
			maxBid = utilitySpace.getMaxUtilityBid();
			if (lastReceivedBid != null && lastReceivedBid.equals(maxBid)) {
				return new Accept(getPartyId(), lastReceivedBid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Offer(getPartyId(), maxBid);
	}

	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		if (action instanceof Offer) {
			lastReceivedBid = ((Offer) action).getBid();
		}
	}

	@Override
	public String getDescription() {
		return "Only accepts best utility!";
	}

}
