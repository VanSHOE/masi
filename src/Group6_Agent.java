import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.BidIterator;
import genius.core.actions.*;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
// import for getParties()


/**
 * This is your negotiation party.
 */
public class Group6_Agent extends AbstractNegotiationParty {

	private Bid lastReceivedBid = null;
	// create bids array
	private LinkedList<Bid>[] Oppbids;
	// Array of all possible bids
	private Bid[] allBids;
	// curPtr
	private int curPtr = 0;
	private double d = 0.5;
	private int totalParties = -1;
	private int curParty = 0;
	private int[][] issueChanges;
	private int[][][] freqTable;
	private HashMap<AgentID, Integer> agent2Index = new HashMap<AgentID, Integer>();
	private HashMap<Integer, AgentID> index2Agent = new HashMap<Integer, AgentID>();
	private List<Issue> allIssues;
	ValueDiscrete[][] allIssueValues;
	NegotiationInfo cInfo;

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		// print all issues
		allIssues = info.getUtilitySpace().getDomain().getIssues();
		// create 2d matrix

		allIssueValues = new ValueDiscrete[allIssues.size()][];
		for (Issue issue : allIssues) {
			System.out.println(issue);
			// print possible values
			// check if instance of issuedescrete
			if (issue instanceof IssueDiscrete) {
				IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
				allIssueValues[issue.getNumber() - 1] = new ValueDiscrete[issueDiscrete.getNumberOfValues()];
				for (int i = 0; i < issueDiscrete.getNumberOfValues(); i++) {
					System.out.println(issueDiscrete.getValue(i));
					allIssueValues[issue.getNumber() - 1][i] = issueDiscrete.getValue(i);
				}
			}
			else {
				System.out.println("Not an issue discrete");
			}

		}
		// print array
		System.out.println(Arrays.deepToString(allIssueValues));
		// calculate possible bids from 2d matrix values * issues
		int totalBids = 1;
		for (int i = 0; i < allIssueValues.length; i++) {
			totalBids *= allIssueValues[i].length;
		}
		System.out.println("Total bids: " + totalBids);

		// construct a bid
		Bid bid = new Bid(info.getUtilitySpace().getDomain());

//		// crash agent

		cInfo = info;

		// set all bids to null
//		Arrays.fill(Oppbids, null);
		// fill totalBids
		BidIterator bidIterator = new BidIterator(info.getUtilitySpace().getDomain());
		allBids = new Bid[(int)info.getUtilitySpace().getDomain().getNumberOfPossibleBids()];
		int i = 0;
		while (bidIterator.hasNext()) {
			allBids[i] = bidIterator.next();
			i++;
		}

		// print length of bids
		System.out.println("Length of bids: " + allBids.length);
//		System.exit(0);
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
		if (action instanceof Inform && totalParties == -1)
		{
			totalParties = (int)((Inform) action).getValue();

			freqTable = new int[totalParties][allIssues.size()][];
			for (int i = 0; i < totalParties; i++)
			{
				for (int j = 0; j < allIssues.size(); j++)
				{
					freqTable[i][j] = new int[((IssueDiscrete) allIssues.get(j)).getNumberOfValues()];
					Arrays.fill(freqTable[i][j], 0);
				}
			}

			// print
			System.out.println("Total parties: " + totalParties);
			System.out.println("Freq table: " + Arrays.deepToString(freqTable));
			issueChanges = new int[totalParties][allIssues.size()];

			for (int i = 0; i < totalParties; i++)
			{
				Arrays.fill(issueChanges[i], 0);
			}

			Oppbids = new LinkedList[totalParties];

			for (int i = 0; i < totalParties; i++)
			{
				Oppbids[i] = new LinkedList<Bid>();
			}

			// crash
//			System.exit(0);
		}
		if (sender != null)
		{
			// check if it exists in hashmaps
			if (!agent2Index.containsKey(sender))
			{
				agent2Index.put(sender, agent2Index.size());
				index2Agent.put(index2Agent.size(), sender);
			}
		}
		System.out.println("Current sender: " + sender + " Action: " + action);

		if (!(action instanceof Offer) && !(action instanceof Accept))
		{
			return;
		}

		if (action instanceof Offer) {
			if (Oppbids.length > 0)
			{
				Bid lastBid = Oppbids[agent2Index.get(sender)].peekLast();
				Bid curBid = ((Offer) action).getBid();

				// if not equal
				if (lastBid != null && !lastBid.equals(curBid))
				{
					for (int i = 0; i < allIssues.size(); i++)
					{
						Value lastVal = lastBid.getValue(i);
						Value curVal = curBid.getValue(i);

						if (lastVal != curVal)
						{
							issueChanges[agent2Index.get(sender)][i]++;
						}
					}
				}
			}
			Oppbids[agent2Index.get(sender)].add(((Offer) action).getBid());
		}
		else {
			if (Oppbids.length > 0)
			{
				Bid lastBid = Oppbids[agent2Index.get(sender)].peekLast();
				Bid curBid = ((Accept) action).getBid();

				// if not equal
				if (lastBid != null && !lastBid.equals(curBid))
				{
					for (int i = 0; i < allIssues.size(); i++)
					{
						Value lastVal = lastBid.getValue(i);
						Value curVal = curBid.getValue(i);

						if (lastVal != curVal)
						{
							issueChanges[agent2Index.get(sender)][i]++;
						}
					}
				}
			}
			Oppbids[agent2Index.get(sender)].add(((Accept) action).getBid());
		}

		// update freq table
		int SIdx = agent2Index.get(sender);
		Bid bid = lastReceivedBid;
		for (int i = 0; i < allIssues.size(); i++)
		{
			IssueDiscrete issue = (IssueDiscrete) allIssues.get(i);
			ValueDiscrete valIdx = ((ValueDiscrete) bid.getValue(issue.getNumber()));
			// search for idx in allissuesvalues
			int idx = -1;
			for (int j = 0; j < issue.getNumberOfValues(); j++)
			{
				if (issue.getValue(j).equals(valIdx))
				{
					idx = j;
					break;
				}
			}

			if (idx >= 0 && idx < issue.getNumberOfValues())
			{
				freqTable[SIdx][i][idx]++;
			}
		}

	}

	@Override
	public String getDescription() {
		return "Decides on the basis of history of bids.";
	}

}
