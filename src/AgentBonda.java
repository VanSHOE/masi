import java.util.Arrays;
import java.util.List;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.BidIterator;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
// import for getParties()


/**
 * This is your negotiation party.
 */
public class AgentBonda extends AbstractNegotiationParty {

	private Bid lastReceivedBid = null;
	// create bids array
	private Bid[] bids = new Bid[1000];
	// Array of all possible bids
	private Bid[] allBids;
	// curPtr
	private int curPtr = 0;
	private double cRation;

	NegotiationInfo cInfo;

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);

		cInfo = info;
		// set all bids to null
		Arrays.fill(bids, null);
		// fill totalBids
		BidIterator bidIterator = new BidIterator(info.getUtilitySpace().getDomain());
		allBids = new Bid[(int)info.getUtilitySpace().getDomain().getNumberOfPossibleBids()];
		int i = 0;
		while (bidIterator.hasNext()) {
			allBids[i] = bidIterator.next();
			i++;
		}

		// print length of bids
		System.out.println("Length of bids: " + bids.length);
		// sort on utility
		Arrays.sort(allBids, (Bid b1, Bid b2) -> {
			try {
				return Double.compare(info.getUtilitySpace().getUtility(b2), info.getUtilitySpace().getUtility(b1));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		});


		// find reservation value
		double reservationValue = 0;
		try {
			reservationValue = info.getUtilitySpace().getReservationValueUndiscounted();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Reservation value: " + reservationValue);

		// find the bid with the reservation value and set cratio
		for (int j = 0; j < allBids.length; j++) {
			try {
				if (info.getUtilitySpace().getUtility(allBids[j]) <= reservationValue) {
					cRation = j / (double)allBids.length;
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		// variable to store max bid
		Bid maxBid;
		System.out.println("Time left: " + getTimeLine().getTime());
		// deadline

		try {
			// get time
			double time = getTimeLine().getTime();
			// concession curbid
			int curBid = (int) (cRation * time * bids.length) - 1;
			// print current bid utility
			// Current bid print


			maxBid = allBids[curBid];
			if (lastReceivedBid != null && getUtility(lastReceivedBid) >= getUtility(maxBid)) {
				return new Accept(getPartyId(), lastReceivedBid); // If the last received bid is equal or better(not really possible but since this involves floating point arithmetic, even greater is fine) than the maximum possible bid, accept it
			}
			System.out.println("Current bid: " + maxBid);
			System.out.println("Current bid utility: " + getUtility(maxBid));
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
			bids[curPtr] = lastReceivedBid;
			curPtr = (curPtr + 1) % bids.length;
		}
	}

	@Override
	public String getDescription() {
		return "Decides on the basis of history of bids.";
	}

}
