import java.util.Arrays;
import java.util.List;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.BidIterator;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
// import for getParties()


/**
 * This is your negotiation party.
 */
public class Group6_Agent extends AbstractNegotiationParty {

	private Bid lastReceivedBid = null;
	// create bids array
	private Bid[] Oppbids = new Bid[1000];
	// Array of all possible bids
	private Bid[] allBids;
	// curPtr
	private int curPtr = 0;
	private double d = 0.5;

	NegotiationInfo cInfo;

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		// print all issues
//		List<Issue> allIssues = info.getUtilitySpace().getDomain().getIssues();
//		// create 2d matrix
//		int[][] allIssueValues = new int[allIssues.size()][];
//		for (Issue issue : allIssues) {
//			System.out.println(issue);
//			// print possible values
//			// check if instance of issuedescrete
//			if (issue instanceof IssueDiscrete) {
//				IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
//				for (int i = 0; i < issueDiscrete.getNumberOfValues(); i++) {
//					System.out.println(issueDiscrete.getValue(i));
//				}
//			}
//
//		}
//		// crash agent
//	 	System.exit(0);
		cInfo = info;

		// set all bids to null
		Arrays.fill(Oppbids, null);
		// fill totalBids
		BidIterator bidIterator = new BidIterator(info.getUtilitySpace().getDomain());
		allBids = new Bid[(int)info.getUtilitySpace().getDomain().getNumberOfPossibleBids()];
		int i = 0;
		while (bidIterator.hasNext()) {
			allBids[i] = bidIterator.next();
			i++;
		}

		// print length of bids
		System.out.println("Length of bids: " + Oppbids.length);
		// sort on utility
		Arrays.sort(allBids, (Bid b1, Bid b2) -> {
			try {
				return Double.compare(info.getUtilitySpace().getUtility(b2), info.getUtilitySpace().getUtility(b1));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		});


//		// find reservation value
//		double reservationValue = 0;
//		try {
//			reservationValue = info.getUtilitySpace().getReservationValue();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("Reservation value: " + reservationValue);
//
//		// find the bid with the reservation value and set cratio
//		for (int j = 0; j < allBids.length; j++) {
//			try {
//				// print each bid
////				System.out.println("Bid: " + allBids[j] + " Utility: " + info.getUtilitySpace().getUtility(allBids[j]));
//				if (info.getUtilitySpace().getUtility(allBids[j]) <= reservationValue) {
//					cRation = j / (double)allBids.length;
//					break;
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		System.out.println("Cratio: " + cRation);


	}

	public double getConcederUtil() {
		// get curTime
		double curTime = timeline.getTime();
		// func is 1 - tan(t*arctan(d))/d
		return 1 - Math.tan(curTime * Math.atan(d)) / d;
//		return 1 - curTime * d + 1;
	}

	public double getJustAcceptProb()
	{
		double curTime = timeline.getTime();
		// func is 0.8*(e^dx - 1) / (e^d - 1)
		return 0.8 * (Math.exp(d * curTime) - 1) / (Math.exp(d) - 1);
	}
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		// variable to store max bid

		System.out.println("Time left: " + getTimeLine().getTime());
		// deadline

		try {
			// get time

			if (getUtility(lastReceivedBid) < 0.3) {
				d += 0.5;
				// clip at 6
				if (d > 10) {
					d = 10;
				}

				// print d
				System.out.println("D: " + d);
			}

			double time = getTimeLine().getTime();
			// concession curbid
			// get concederutil
			double concederUtil = getConcederUtil();
			// print
			System.out.println("Conceder util: " + concederUtil);

			double p = getJustAcceptProb();

			if (lastReceivedBid != null && (getUtility(lastReceivedBid) >= concederUtil || p * getUtility(lastReceivedBid) >= (1 - p) * concederUtil)) {
				return new Accept(getPartyId(), lastReceivedBid); // If the last received bid is equal or better(not really possible but since this involves floating point arithmetic, even greater is fine) than the maximum possible bid, accept it
			}

			// find bid with util greater or equal
			Bid maxBid = allBids[0];
			for (Bid allBid : allBids) {
				if (getUtility(allBid) >= concederUtil) {
					maxBid = allBid;
				} else {
//					maxBid = allBids[i - 1];
					break;
				}
			}

			System.out.println("Current bid: " + maxBid);
			System.out.println("Current bid utility: " + getUtility(maxBid));
			return new Offer(getPartyId(), maxBid); // If the last received bid is not equal or better than the maximum possible bid, offer the maximum possible bid
		} catch (Exception e) {
			e.printStackTrace();
			// accept
			return new Accept(getPartyId(), lastReceivedBid);
		}
	}

	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		// print sender and action
		System.out.println("Current sender: " + sender + " Action: " + action);

		if (action instanceof Offer) {
			Oppbids[curPtr] = lastReceivedBid;
			curPtr = (curPtr + 1) % Oppbids.length;
			// if utility of last received bid less than 0.5 increment d, become aggressive
		}
	}

	@Override
	public String getDescription() {
		return "Decides on the basis of history of bids.";
	}

}
