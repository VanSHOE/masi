import java.util.List;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
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
	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		// variable to store max bid
		Bid maxBid;
		try {
			maxBid = utilitySpace.getMaxUtilityBid(); // This function gets the maximum possible bid in the given utility space
			if (lastReceivedBid != null && getUtility(lastReceivedBid) >= getUtility(maxBid)) {
				return new Accept(getPartyId(), lastReceivedBid); // If the last received bid is equal or better(not really possible but since this involves floating point arithmetic, even greater is fine) than the maximum possible bid, accept it
			}
			return new Offer(getPartyId(), maxBid); // If the last received bid is not equal or better than the maximum possible bid, offer the maximum possible bid
		} catch (Exception e) {
			e.printStackTrace();
			return new EndNegotiation(getPartyId()); // If something goes wrong, end the negotiation
		}
	}

	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		if (action instanceof Offer) {
			lastReceivedBid = ((Offer) action).getBid(); // If the action is an offer, store the bid as last received bid
		}
	}

	@Override
	public String getDescription() {
		return "Only accepts best utility!";
	}

}
