import java.util.*;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.BidIterator;
import genius.core.actions.*;
import genius.core.issue.*;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.timeline.DiscreteTimeline;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;
// import for getParties()

public class Group6_Agent extends AbstractNegotiationParty {

    private Bid lastReceivedBid = null;
    // create bids array
    private LinkedList<Bid>[] Oppbids;
    // Array of all possible bids
    private Bid[] allBids;
    // curPtr
    private int curPtr = 0;
    private double d = 47;
    private int totalEnemies = -1;
    private int curParty = 0;
    private int[][] issueChanges;
    private int[][][] freqTable;
    private HashMap<AgentID, Integer> agent2Index = new HashMap<AgentID, Integer>();
    private HashMap<Integer, AgentID> index2Agent = new HashMap<Integer, AgentID>();
    private HashMap<String, HashMap<String, Integer>> issueValueEvals = new HashMap<String, HashMap<String, Integer>>();
    private List<Issue> allIssues;
    ValueDiscrete[][] allIssueValues;
    NegotiationInfo cInfo;

    double reservationValue;

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);
        // print all issues
        allIssues = info.getUtilitySpace().getDomain().getIssues();
        // create 2d matrix

        allIssueValues = new ValueDiscrete[allIssues.size()][];
        for (int ii = 0; ii < allIssues.size(); ii++) {
            Issue issue = allIssues.get(ii);
            System.out.println(issue);
            // print possible values
            // check if instance of issuedescrete
            if (issue instanceof IssueDiscrete) {
                IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
                allIssueValues[issue.getNumber() - 1] = new ValueDiscrete[issueDiscrete.getNumberOfValues()];
                for (int i = 0; i < issueDiscrete.getNumberOfValues(); i++) {
                    System.out.println(issueDiscrete.getValue(i));
                    allIssueValues[ii][i] = issueDiscrete.getValue(i);
                }
            } else {
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
        allBids = new Bid[(int) info.getUtilitySpace().getDomain().getNumberOfPossibleBids()];
        int i = 0;
        while (bidIterator.hasNext()) {
            allBids[i] = bidIterator.next();
            i++;
        }

        // print length of bids
        System.out.println("Length of bids: " + allBids.length);
//		System.exit(0)

        Map<Objective, Evaluator> eval = ((AdditiveUtilitySpace) utilitySpace).getfEvaluators();
        // print all keys
        for (Objective key : eval.keySet()) {
            System.out.print("Key: " + key);
            EvaluatorDiscrete evalDiscrete = (EvaluatorDiscrete) eval.get(key);
            Set<ValueDiscrete> values = evalDiscrete.getValues();
            System.out.println(" Value: " + evalDiscrete);
            // check if issue exists in issueValueEvals
            if (!issueValueEvals.containsKey(key.toString())) {
                issueValueEvals.put(key.toString(), new HashMap<String, Integer>());
            }
            // add values to issueValueEvals
            for (ValueDiscrete value : values) {
                issueValueEvals.get(key.toString()).put(value.toString(), evalDiscrete.getValue(value));
            }
        }
        System.out.println("Eval: " + eval);

        // TODO: find reservation value
        reservationValue = 0;
        try {
            reservationValue = info.getUtilitySpace().getReservationValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Reservation value: " + reservationValue);
    }


    public double getConcederUtil() {
        // get curTime
        double curTime = timeline.getTime();
        // func is 1 - tan(t*arctan(d))/d
        double val2Ret = 1 - Math.tan(curTime * Math.atan(d)) / d;
        // max of reservation value
        return Math.max(val2Ret, reservationValue);
    }

    public double getJustAcceptProb() {
        double curTime = timeline.getTime();
        if (curTime < 0.8) {
            return 0;
        }
        // func is 0.8*(e^dx - 1) / (e^d - 1)
        return 0.8 * (Math.exp(d * curTime) - 1) / (Math.exp(d) - 1);
    }

    @Override
    public Action chooseAction(List<Class<? extends Action>> validActions) {


        try {
            System.out.println("Time left: " + getTimeLine().getTime());
            DiscreteTimeline discreteTimeline = (DiscreteTimeline) timeline;
            System.out.println("Current rounds left: " + discreteTimeline.getOwnRoundsLeft());
            double[][] hypoWeights = new double[totalEnemies][allIssues.size()];
            // fill

//            for (int i = 0; i < totalEnemies; i++) {
//
//                int sumIss = 0;
//                for (int k = 0; k < allIssues.size(); k++) {
//                    sumIss += issueChanges[i][k];
//                }
//
//                if (sumIss == 0) {
//                    for (int j = 0; j < allIssues.size(); j++) {
//                        hypoWeights[i][j] = 1.0 / allIssues.size();
//                    }
//
//                    continue;
//                }
//
//                for (int j = 0; j < allIssues.size(); j++) {
//                    hypoWeights[i][j] = issueChanges[i][j] / (double) sumIss;
//                    hypoWeights[i][j] = 1 - hypoWeights[i][j];
//                }
//
//                // normalize
//                double sum = 0;
//                for (int j = 0; j < allIssues.size(); j++) {
//                    sum += hypoWeights[i][j];
//                }
//
//                for (int j = 0; j < allIssues.size(); j++) {
//                    hypoWeights[i][j] /= sum;
//                }
//            }

            double[] stdDevs = new double[totalEnemies]; // std dev of freq table
            double[] means = new double[totalEnemies]; // mean of freq table
            double[] niceness = new double[totalEnemies]; // niceness of agent

            for (int i = 0; i < totalEnemies; i++) {
                double sum = 0;
                double sumMeans = 0;
                for (int j = 0; j < allIssues.size(); j++) {
                    int sumAllFreq = 0;
                    for (int k = 0; k < ((IssueDiscrete) allIssues.get(j)).getNumberOfValues(); k++) {
                        sumAllFreq += freqTable[i][j][k];
                    }

                    String issueName = allIssues.get(j).getName();
                    double mean = 0;
                    // TODO: Add issue discrete checks
                    for (int k = 0; k < ((IssueDiscrete) allIssues.get(j)).getNumberOfValues(); k++) {
                        mean += k * freqTable[i][j][k];
                    }
                    mean /= sumAllFreq;

                    double stdDev = 0;
                    for (int k = 0; k < ((IssueDiscrete) allIssues.get(j)).getNumberOfValues(); k++) {
                        for (int l = 0; l < freqTable[i][j][k]; l++) {
                            stdDev += Math.pow(k - mean, 2);
                        }
                    }
                    stdDev /= sumAllFreq;
                    stdDev = Math.sqrt(stdDev);
                    hypoWeights[i][j] = 1.0 / (1.0 + stdDev);
//                    hypoWeights[i][j] = stdDev;

                    double valMean = 0;
                    for (int k = 0; k < ((IssueDiscrete) allIssues.get(j)).getNumberOfValues(); k++) {
                        double valEval = issueValueEvals.get(issueName).get(((IssueDiscrete) allIssues.get(j)).getValue(k).toString());
                        valMean += freqTable[i][j][k] * valEval;
                    }
                    valMean /= sumAllFreq;
                    sumMeans += valMean;
                    sum += stdDev;
                }
                // normalize hypoWeights[i]
                double sumHypo = 0;
                for (int j = 0; j < allIssues.size(); j++) {
                    sumHypo += hypoWeights[i][j];
                }

                for (int j = 0; j < allIssues.size(); j++) {
                    hypoWeights[i][j] /= sumHypo;
                }

                sum /= allIssues.size();
                sumMeans /= allIssues.size();
                stdDevs[i] = sum;
                means[i] = sumMeans;
            }


            for (int i = 0; i < totalEnemies; i++) {
                niceness[i] = 10 * means[i] / (1 + stdDevs[i]) + stdDevs[i] / (1 + means[i]);
            }

            // TODO: Think on what to do after this. Like use the niceness and weights etc. to find some ordering of bids (sort the all bids array)

            Arrays.sort(allBids, (Bid b1, Bid b2) -> {
                try {
                    double myUtil1 = cInfo.getUtilitySpace().getUtilityWithDiscount(b1, timeline);
                    double myUtil2 = cInfo.getUtilitySpace().getUtilityWithDiscount(b2, timeline);
                    double[] util1 = new double[totalEnemies];
                    double[] util2 = new double[totalEnemies];

                    for (int i = 0; i < totalEnemies; i++) {
                        util1[i] = 0;
                        util2[i] = 0;

                        for (int j = 0; j < allIssues.size(); j++) {
                            IssueDiscrete issue = (IssueDiscrete) allIssues.get(j);
                            int issueNum = issue.getNumber();
                            int sumFreq = 0;

                            for (int k = 0; k < issue.getNumberOfValues(); k++) {
                                sumFreq += freqTable[i][j][k];
                            }

                            double issueWeight = hypoWeights[i][j];
                            int val1idx = issue.getValueIndex((ValueDiscrete) b1.getValue(issueNum));
                            int val2idx = issue.getValueIndex((ValueDiscrete) b2.getValue(issueNum));

                            double issueGain1 = issueWeight * freqTable[i][j][val1idx] / (double) sumFreq;
                            double issueGain2 = issueWeight * freqTable[i][j][val2idx] / (double) sumFreq;

                            util1[i] += issueGain1;
                            util2[i] += issueGain2;
                        }
                    }

                    double val1 = 0;
                    double val2 = 0;
                    int nicenessSum = 0;


                    for (int i = 0; i < totalEnemies; i++) {
                        val1 += util1[i] * (1 - niceness[i]);
                        val2 += util2[i] * (1 - niceness[i]);
                        nicenessSum += (1 - niceness[i]);
                    }
                    val1 /= nicenessSum;
                    val2 /= nicenessSum;

                    double compFunc1 = (1 + Math.pow(myUtil1, 3)) * (1 + Math.pow(val1, 1));
                    double compFunc2 = (1 + Math.pow(myUtil2, 3)) * (1 + Math.pow(val2, 1));

                    return Double.compare(compFunc2, compFunc1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            });


            if (lastReceivedBid != null && cInfo.getUtilitySpace().getUtilityWithDiscount(lastReceivedBid, timeline) < 0.5) {
                d += 0.5;
                System.out.println("D: " + d);
            }

            double time = getTimeLine().getTime();
            // concession curbid
            // get concederutil
            double concederUtil = getConcederUtil();
            // print
            System.out.println("Conceder util: " + concederUtil);

            double p = getJustAcceptProb();

            if ((lastReceivedBid != null && (cInfo.getUtilitySpace().getUtilityWithDiscount(lastReceivedBid, timeline) >= concederUtil)) || discreteTimeline.getOwnRoundsLeft() == 0) {
//                System.out.println("Ending negotiation");
//                return new EndNegotiation(getPartyId());
                return new Accept(getPartyId(), lastReceivedBid); // If the last received bid is equal or better(not really possible but since this involves floating point arithmetic, even greater is fine) than the maximum possible bid, accept it
            }

            Bid maxBid = allBids[0];

            System.out.println("Current bid: " + maxBid);
            System.out.println("Current bid utility: " + cInfo.getUtilitySpace().getUtilityWithDiscount(maxBid, timeline));
            return new Offer(getPartyId(), maxBid); // If the last received bid is not equal or better than the maximum possible bid, offer the maximum possible bid
        } catch (Exception e) {
            e.printStackTrace();
            // accept
            return new Accept(getPartyId(), lastReceivedBid);
        }
    }

    @Override
    public void receiveMessage(AgentID sender, Action action) {
        // print
        try {
            System.out.println("Received message from " + sender + " with action " + action);
            super.receiveMessage(sender, action);
            // print sender and action
            if (action instanceof Inform && totalEnemies == -1) {
                totalEnemies = (int) ((Inform) action).getValue() - 1;

                freqTable = new int[totalEnemies][allIssues.size()][];
                for (int i = 0; i < totalEnemies; i++) {
                    for (int j = 0; j < allIssues.size(); j++) {
                        freqTable[i][j] = new int[((IssueDiscrete) allIssues.get(j)).getNumberOfValues()];
                        Arrays.fill(freqTable[i][j], 0);
                    }
                }

                // print
                System.out.println("Total parties: " + totalEnemies);
                System.out.println("Freq table: " + Arrays.deepToString(freqTable));
                issueChanges = new int[totalEnemies][allIssues.size()];

                for (int i = 0; i < totalEnemies; i++) {
                    Arrays.fill(issueChanges[i], 0);
                }

                Oppbids = new LinkedList[totalEnemies];

                for (int i = 0; i < totalEnemies; i++) {
                    Oppbids[i] = new LinkedList<Bid>();
                }
            }
            if (sender != null) {
                // check if it exists in hashmaps
                if (!agent2Index.containsKey(sender)) {
                    agent2Index.put(sender, agent2Index.size());
                    index2Agent.put(index2Agent.size(), sender);
                }
            }
            System.out.println("Current sender: " + sender + " Action: " + action);

            if (!(action instanceof Offer) && !(action instanceof Accept)) {
                System.out.println("receiveMessage end - a");
                return;
            }

            Bid bid = null;
            if (action instanceof Offer) {
                lastReceivedBid = ((Offer) action).getBid();
                bid = ((Offer) action).getBid();
                if (Oppbids[agent2Index.get(sender)].size() > 0) {
                    Bid lastBid = Oppbids[agent2Index.get(sender)].peekLast();
                    Bid curBid = ((Offer) action).getBid();

                    // if not equal
                    if (lastBid != null && !lastBid.equals(curBid)) {
                        for (int i = 0; i < allIssues.size(); i++) {
                            int s = allIssues.get(i).getNumber();
                            Value lastVal = lastBid.getValue(s);
                            Value curVal = curBid.getValue(s);

                            if (lastVal != curVal) {
                                issueChanges[agent2Index.get(sender)][i]++;
                            }
                        }
                    }
                }
                Oppbids[agent2Index.get(sender)].add(((Offer) action).getBid());
            } else {
//            lastReceivedBid = ((Accept) action).getBid();
                bid = ((Accept) action).getBid();
                if (Oppbids[agent2Index.get(sender)].size() > 0) {
                    Bid lastBid = Oppbids[agent2Index.get(sender)].peekLast();
                    Bid curBid = ((Accept) action).getBid();


                    // if not equal
                    if (lastBid != null && !lastBid.equals(curBid)) {
                        for (int i = 0; i < allIssues.size(); i++) {
                            int s = allIssues.get(i).getNumber();
                            Value lastVal = lastBid.getValue(s);
                            Value curVal = curBid.getValue(s);

                            if (lastVal != curVal) {
                                issueChanges[agent2Index.get(sender)][i]++;
                            }
                        }
                    }
                }
                Oppbids[agent2Index.get(sender)].add(((Accept) action).getBid());
            }

            // update freq table
            int SIdx = agent2Index.get(sender);

            if (bid == null) {
                System.out.println("receiveMessage end - b");
                return;
            }

            for (int i = 0; i < allIssues.size(); i++) {
                IssueDiscrete issue = (IssueDiscrete) allIssues.get(i);
                // TODO: We need to make our agent crash friendly with try except
                ValueDiscrete valIdx = ((ValueDiscrete) bid.getValue(issue.getNumber()));
                // search for idx in allissuesvalues
                int idx = -1;
                for (int j = 0; j < issue.getNumberOfValues(); j++) {
                    if (issue.getValue(j).equals(valIdx)) {
                        idx = j;
                        break;
                    }
                }

                if (idx >= 0 && idx < issue.getNumberOfValues()) {
                    freqTable[SIdx][i][idx]++;
                }
            }

            System.out.println("receiveMessage end - c");

        } catch (Exception e) {

            // error
            System.out.println("receiveMessage end - Something went wrong");
//            e.printStackTrace();
        }

    }

    @Override
    public String getDescription() {
        return "Decides on the basis of history of bids.";
    }

}
