# An Opponent-based Conceder Strategy for Multi-Issue Automated Negotiation

## Abstract
In this report, we explain the design and workings of our automated negotiation agent, optimistically named **PleaseWin**. PleaseWin has been designed for the **Stacked Alternating Offers Protocol (SAOP)**, where agents make moves one by one in a round table negotiation. PleaseWin works for any general automated negotiation following SAOP, where the issues can only take discrete values. Some modifications are needed to accommodate continuous values. PleaseWin has been programmed using **GENIUS** (Generic Environment for Negotiation with Intelligent multi-purpose Usage Simulation).

---

## Introduction
In most negotiations, agents participating do not disclose their preferences (called preference profiles) to avoid exploitation. Therefore, negotiating agents operate with incomplete information about their opponents. A bid that is unfavorable to even one agent is likely to be rejected. Thus, the agents try to reach consensus before the deadline.

In SAOP:
1. Any agent can start the negotiation process by making a bid to the agent next to it in the round table.
2. Upon receiving a bid, an agent can:
   - Accept the incoming bid, finalizing the deal if all agents agree, or pass it forward.
   - Reject the bid and propose its own bid to the next agent.
   - End the negotiation, resulting in zero utility for all agents.

The process continues until:
- An agent ends the negotiation.
- All agents come to a consensus.
- The deadline is reached without consensus, leading to zero utility for all agents.

**PleaseWin** utilizes the **BOA framework**, which consists of three interconnected components: bid selection, opponent modeling, and acceptance strategy.

---

## BOA Framework
The BOA framework builds agents with the following components:

1. **Bid Selection**
   - Refers to the strategy of choosing a bid from all possible bids to propose.
   - Example: Some agents prioritize their own benefit, disregarding others.

2. **Opponent Modeling**
   - Involves predicting opponents' strategies to propose mutually beneficial bids.

3. **Acceptance Strategy**
   - Determines whether an offer is accepted or rejected.
   - If rejected, the bid selection process begins unless the deadline is reached.

---

## The PleaseWin Strategy
**PleaseWin** never opts to end the negotiation because any nonzero utility is better than zero utility from a failed negotiation. The strategy is categorized into three components:

### 1. Opponent Modeling
PleaseWin models opponents by storing their move history in a 3D matrix, `Freq`. 

- `Freq(i, j, k)` is the number of times the *i-th* agent proposed the *k-th* value for the *j-th* issue in its bids. All entries are initialized to zero.

#### Hypothetical Weights
Weights for each issue are estimated using the variance of the values proposed by opponents:

hypoWeights[i][j] = 1 / (1 + std_ij)

- `std_ij`: Standard deviation of the values proposed by the *i-th* agent for the *j-th* issue.
- After calculating, weights are normalized for each issue.

---

### 2. Bid Selection
PleaseWin prioritizes itself while predicting opponents' utilities to propose bids that maximize mutual benefit. The process includes:

- Constructing a **comparator function** using opponent model data to arrange bids by preference.
- Calculating means (`Means[i]`) and standard deviations (`stdDevs[i]`) for each agent’s bids.
- Assigning **niceness values** to opponents, giving higher weightage to hardliners.
- Optimizing the equation:

OurUtility^2 * EnemyUtility

Here, `EnemyUtility` is a weighted average of hypothetical utilities predicted by PleaseWin.

---

### 3. Acceptance Strategy
PleaseWin calculates a **minimum acceptable utility** based on the current time-step and aggressiveness (`d`). A bid is accepted if:

1. The utility from the bid is greater than or equal to the minimum acceptable utility.
2. The current round is the last round of negotiation.

The **minimum acceptable utility** is determined by:

F(t, d) = 1 - (tan(t * arctan(d)) / d)

getConcederUtil = max(AgentReservationValue, F(t, d))

- `t`: Time-step (`t ∈ [0, 1]`).
- `d`: Aggressiveness factor (higher values make PleaseWin more aggressive).

---
