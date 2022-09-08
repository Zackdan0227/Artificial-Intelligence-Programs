import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class MD {

	private static int bottomReward = 0;
	private static int topReward = 0;
	private static int nonterminalStates = 0;
	private static int terminalStates = 0;
	private static int rounds = 0;
	private static int freq = 0;
	private static int M = 0;
	// structure to record nonterminal and terminal states
	private static Hashtable<Integer, State> terminalDict = new Hashtable<Integer, State>();
	private static ArrayList<State> stateArray = new ArrayList<State>();

	// State class for storing reward, action count, and probability
	static class State {
		int s;
		int reward;
		int n;
		ArrayList<Hashtable<Integer, Double>> stateProb;
		Hashtable<Integer, Double> stateProb0;
		Hashtable<Integer, Double> stateProb1;

		State(int n) {
			s = n;
			reward = 0;
			n = 0;
			stateProb = new ArrayList<Hashtable<Integer, Double>>();
			stateProb0 = new Hashtable<Integer, Double>();
			stateProb1 = new Hashtable<Integer, Double>();
		}

		State() {
			s = 0;
			reward = 0;
			n = 0;
			stateProb = new ArrayList<Hashtable<Integer, Double>>();
			stateProb0 = new Hashtable<Integer, Double>();
			stateProb1 = new Hashtable<Integer, Double>();
		}
	}

	// find if there is an action that is count =0
	static int checkActionU(int[][] count, int s) {

		for (int j = 0; j < stateArray.get(s).n; j++) {
			if (count[s][j] == 0) {
				return j;
			}
		}
		return -1;
	}

	// check if it is terminal state
	static boolean checkTerminalState(int choices, State s) {
		if (s.reward == 0) {
			return true;
		}
		return false;
	}

	// given an action, choose a state number to return using randomChoice
	static int actionChooseState(Hashtable<Integer, Double> dict) {
		int[] key = new int[dict.size()];
		double[] p = new double[dict.size()];
		Enumeration keyEnu = dict.keys();
		int j = 0;
		while (keyEnu.hasMoreElements()) {
			key[j] = (int) keyEnu.nextElement();
			j++;
		}
		Enumeration probEnu;
		probEnu = dict.elements();
		int i = 0;
		while (probEnu.hasMoreElements()) {
			double stateProb = (double) probEnu.nextElement();
			p[i] = stateProb;
			i++;
		}

		int index = randomChoice(p);

		return key[index];

	}

	// return null if next state is terminal state
	// update reward if its terminal
	static State getActionNextState(int choices, State state, int[][] Total) {

		int actionStateIndex = actionChooseState(state.stateProb.get(choices));
		if (actionStateIndex <= nonterminalStates - 1) {
			return stateArray.get(actionStateIndex);
		} else {
			State tState = terminalDict.get(actionStateIndex);
			return tState;
		}

	}

	// randomChoice from pseudo code section 7 of project
	static int randomChoice(double[] p) {
		if (p == null) {
			return -1;
		}
		int k = p.length - 1;
		double[] u = new double[p.length];
		u[0] = p[0];
		for (int i = 1; i <= k; i++) {
			u[i] = u[i - 1] + p[i];
		}
		double x = Math.random();

		for (int i = 0; i < k; i++) {
			if (x < u[i]) {
				return i;
			}
		}
		return k;
	}

	// reference to pseudo code from section 6
	static int chooseAction(State s, int[][] count, int[][] total, int M) {

		int n = s.n;
		int sNum = s.s;
		double[] avg = new double[n];
		double[] savg = new double[n];
		double[] up = new double[n];
		double[] p = new double[n];
		int r = checkActionU(count, sNum);
		if (r >= 0) {
			return r;
		}
		for (int i = 0; i <= n - 1; i++) {
			double ct = count[sNum][i];
			double tt = total[sNum][i];
			avg[i] = tt / ct;
		}
		int bottom = bottomReward;
		int top = topReward;
		for (int i = 0; i <= n - 1; i++) {
			savg[i] = 0.25 + 0.75 * (Math.abs((avg[i] - bottom)) / (top - bottom));
		}
		double c = 0;
		for (int i = 0; i < count.length; i++) {
			for (int j = 0; j < stateArray.get(i).n; j++) {
				c += count[i][j];
			}
		}
		for (int i = 0; i <= n - 1; i++) {
			double m = M;
			double div = c / m;
			up[i] = (double) Math.pow(savg[i], div);
		}
		double norm = 0;
		for (int i = 0; i <= n - 1; i++) {
			norm += up[i];
		}
		for (int i = 0; i <= n - 1; i++) {
			p[i] = up[i] / norm;
//			System.out.print(p[i] + " ");
		}
		return randomChoice(p);
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String arg = args[0];
		Scanner reader = new Scanner(new FileInputStream(args[0]));

		int counter = 0;
		// first parsing the 5 parameters
		while (reader.hasNext()) {

			if (counter <= 4) {
				try {
					int s = reader.nextInt();
					switch (counter) {
					case 0:
						nonterminalStates = s;
						break;
					case 1:
						terminalStates = s;
						break;
					case 2:
						rounds = s;
						break;
					case 3:
						freq = s;
						break;
					case 4:
						M = s;
						break;
					}

				} catch (Exception e) {

				}
			}
			// recording the terminal states to a dictionary
			if (counter > 4 && counter % 2 != 0) {
				int state = reader.nextInt();
				State s = new State(state);
				int reward = reader.nextInt();
				s.reward = reward;
				terminalDict.put(state, s);
			}
			counter++;
			if (counter == 8)
				break;
		}
		// calculating the up and bottom reward
		Enumeration terminalEnu;
		terminalEnu = terminalDict.elements();
		int smallest = terminalDict.get(nonterminalStates + 1).reward;
		int biggest = terminalDict.get(nonterminalStates + 1).reward;
		while (terminalEnu.hasMoreElements()) {
			State temp = (MD.State) terminalEnu.nextElement();
			int reward = temp.reward;
			if (reward < smallest) {
				smallest = reward;
			}
			if (reward > biggest) {
				biggest = reward;
			}
		}
		bottomReward = smallest;
		topReward = biggest;
		// parsing the nonterminal state probabilities
		int stateCount = 0;
		int stateName = 0;
		double stateProb = 0;
		int actionCount = 0;
		State temp = new State(-1);
		int actionCountLargest = 0;
		Hashtable<Integer, Double> prob = new Hashtable<Integer, Double>();
		while (reader.hasNext()) {
			int n = 0;
			String stateAction = reader.next();

			int s = Integer.valueOf(stateAction.substring(0, 1));
			int action = Integer.valueOf(stateAction.substring(2));
			if (temp.s < 0) {
				temp.s = s;
			}
			int previousState = temp.s;
			if (temp.s != s) {
				temp.n = actionCount;
				stateArray.add(temp);
				temp = new State(s);
				actionCount = 0;
			}
			actionCount++;
			prob = new Hashtable<Integer, Double>();
			while (reader.hasNext()) {
				int r = 0;
				try {
					stateName = reader.nextInt();
					n++;

				} catch (Exception e) {
					r++;
				}
				try {
					stateProb = reader.nextDouble();

				} catch (Exception e) {
					r++;
				}
				if (r == 0) {
					prob.put(stateName, stateProb);
				}
				if (r > 0) {
					break;
				}
			}
			temp.stateProb.add(prob);
			if (actionCount > actionCountLargest) {
				actionCountLargest = actionCount;
			}

		}
		temp.n = actionCount;
		stateArray.add(temp);
		int[][] Count = new int[nonterminalStates][actionCountLargest];
		int[][] Total = new int[nonterminalStates][actionCountLargest];

		/*
		 * World model simulating the game first find a randomState, then choose the
		 * next state according to the probability
		 */

		for (int rd = 1; rd <= rounds; rd++) {
			boolean termi = true;
			Random generator = new Random();
			int randomIndex = generator.nextInt(stateArray.size());

			State firstRandomState = stateArray.get(randomIndex);
			int choices = -1;
			choices = chooseAction(firstRandomState, Count, Total, M);

			int stateNum = firstRandomState.s;

			Count[stateNum][choices] += 1;
			HashMap<Integer, Integer> countHistory = new HashMap<Integer, Integer>();
			countHistory.put(stateNum, choices);
			// state = based on actionChooseState to find the matching state
			State nextState = getActionNextState(choices, firstRandomState, Total);

			// we encountered a terminal state, update reward and total and skip the next
			// section
			if (nextState.reward != 0) {
				int reward = nextState.reward;
				Total[firstRandomState.s][choices] += reward;
				termi = false;
			}
			// non terminal state
			if (termi) {
				// keep going untill we reached terminal state
				// using a dictionary to record all the steps we took
				// to update the total[][]
				while (checkTerminalState(choices, nextState)) {

					choices = chooseAction(nextState, Count, Total, M);
					stateNum = nextState.s;
					if (!(countHistory.containsKey(stateNum) && countHistory.containsValue(choices))) {
						Count[stateNum][choices] += 1;
						countHistory.put(stateNum, choices);
					}
					nextState = getActionNextState(choices, nextState, Total);
				}
				int rewardT = nextState.reward;
				Iterator historyItr = countHistory.entrySet().iterator();
				while (historyItr.hasNext()) {
					Map.Entry mapElement = (Map.Entry) historyItr.next();

					int stateKey = (int) mapElement.getKey();
					int action = (int) mapElement.getValue();

					Total[stateKey][action] += rewardT;
				}
			}
//			freq = 0;
			if (freq != 0) {

				// output printined every freq rounds;
				// output:
				// The current values of Count and Total Matrices.
				// if(Count[S,A] =0 print(S:U)
				// The best move = largest value of f Total[S,A] / Count[S,A].
				if (rd % freq == 0) {
					System.out.println("After " + rd + " rounds");
					System.out.println("Count: ");
					
					for (int i = 0; i < Count.length; i++) {
						for (int j = 0; j < stateArray.get(i).n; j++) {

							System.out.print("[" + i + "," + j + "]" + "=" + Count[i][j]);
						}
						System.out.println("");
					}
					System.out.println("");
					System.out.println("Total: ");
			
					for (int i = 0; i < Total.length; i++) {
						for (int j = 0; j < stateArray.get(i).n; j++) {
							System.out.print("[" + i + "," + j + "]" + "=" + Total[i][j]);
						}
						System.out.println("");
					}
					System.out.println("");

					ArrayList<String> bestActions = new ArrayList<String>();
					for (int i = 0; i < nonterminalStates; i++) {
						bestActions.add("");
					}

					ArrayList<Integer> zeroCount = new ArrayList<Integer>();
					double big = -123456;
					for (int i = 0; i < Count.length; i++) {
						int act = -123456;
						big = -123456;
						for (int j = 0; j < stateArray.get(i).n; j++) {
							if (Count[i][j] == 0) {
								if (!zeroCount.contains(i))
									zeroCount.add(i);
								big = -123456;
								break;
							} else {
								double ct = Count[i][j];
								double tt = Total[i][j];
								double div = tt / ct;
								if (div > big) {
									big = div;
									act = j;
								}
							}
						}
						if (big != 123456) {
							String t = String.valueOf(i) + ":" + String.valueOf(act) + ".";
							bestActions.set(i, t);
						}
					}

					for (int i = 0; i < zeroCount.size(); i++) {
						int unknownState = zeroCount.get(i);
						String temp1 = String.valueOf(unknownState) + ":" + "U" + ".";
						bestActions.set(unknownState, temp1);
					}
					System.out.print("Best Action: ");
					for (int i = 0; i < bestActions.size(); i++) {
						System.out.print(bestActions.get(i) + " ");
					}
					System.out.println("");

				}

			}

		}
		if (freq == 0) {

			System.out.println("Count: ");

			for (int i = 0; i < Count.length; i++) {
				for (int j = 0; j < stateArray.get(i).n; j++) {

					System.out.print("[" + i + "," + j + "]" + "=" + Count[i][j]);
				}
				System.out.println("");
			}
			System.out.println("");
			System.out.println("Total: ");

			for (int i = 0; i < Total.length; i++) {
				for (int j = 0; j < stateArray.get(i).n; j++) {
					System.out.print("[" + i + "," + j + "]" + "=" + Total[i][j]);
				}
				System.out.println("");
			}
			System.out.println("");

			ArrayList<String> bestActions = new ArrayList<String>();
			for (int i = 0; i < nonterminalStates; i++) {
				bestActions.add("");
			}
			String state0 = "";
			String state1 = "";
			boolean s0 = true;
			boolean s1 = true;
			ArrayList<Integer> zeroCount = new ArrayList<Integer>();
			double big = -123456;
			for (int i = 0; i < Count.length; i++) {
				int act = -123456;
				big = -123456;
				for (int j = 0; j < stateArray.get(i).n; j++) {
					if (Count[i][j] == 0) {
						if (!zeroCount.contains(i))
							zeroCount.add(i);
						big = -123456;
						break;
					} else {
						double ct = Count[i][j];
						double tt = Total[i][j];
						double div = tt / ct;
						if (div > big) {
							big = div;
							act = j;
						}
					}
				}
				if (big != 123456) {
					String t = String.valueOf(i) + ":" + String.valueOf(act) + ".";
					bestActions.set(i, t);
				}
			}

			for (int i = 0; i < zeroCount.size(); i++) {
				int unknownState = zeroCount.get(i);
				String temp1 = String.valueOf(unknownState) + ":" + "U" + ".";
				bestActions.set(unknownState, temp1);
			}
			System.out.print("Best Action: ");
			for (int i = 0; i < bestActions.size(); i++) {
				System.out.print(bestActions.get(i) + " ");
			}
			System.out.println("");

		}
	}

}
