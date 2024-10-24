import java.io.*;
import java.util.*;

/* 
1
20 22
1 7
2 9
3 8
4 7
5 10
5 3
7 4
8 5
9 6
9 7
9 10
10 9
12 15
13 15
13 11
14 17
15 12
15 13
16 18
17 18
18 14
18 16
 */

class Node {
    int value;
    int scc;
    boolean visited = false;

    Node(int value) {
        this.value = value;
    }
}

class SCC {
    int sccNumber;
    boolean visited = false;
    boolean isSink = true;
    boolean isSource = true;
    ArrayList<Node> members = new ArrayList<>();

    SCC(int sccNumber) {
        this.sccNumber = sccNumber;
    }
}

class Graph {
    ArrayList<Node> nodes;
    ArrayList<Node>[] edges;

    Graph(ArrayList<Node> nodes, ArrayList<Node>[] edges) {
        this.nodes = nodes;
        this.edges = edges;
    }
}

public class Main {

    public static Stack<Node> DFS(ArrayList<Node> nodes, ArrayList<Node>[] edges) {
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).visited = false;
        }
        Stack<Node> topOrder = new Stack<>();
        int sccNumber = 0;
        for (Node currNode : nodes) {
            if (!currNode.visited) {
                sccNumber++;
                explore(nodes, edges, currNode, sccNumber, topOrder);
            }
        }
        return topOrder;
    }

    public static void explore(ArrayList<Node> nodes, ArrayList<Node>[] edges, Node node, int sccNumber,
            Stack<Node> topOrder) {
        node.visited = true;
        node.scc = sccNumber;
        for (int neighbor = 0; neighbor < edges[node.value - 1].size(); neighbor++) {
            if (!edges[node.value - 1].get(neighbor).visited) {
                explore(nodes, edges, edges[node.value - 1].get(neighbor), sccNumber, topOrder);
            }
        }
        topOrder.push(node);
    }

    public static Graph reverseGraph(Graph g) {
        ArrayList<Node> nodes = new ArrayList<Node>(g.nodes); // avoid overwriting values in original graph
        ArrayList<Node>[] edges = g.edges;
        ArrayList<Node>[] reversedEdges = new ArrayList[edges.length];

        for (int n = 0; n < reversedEdges.length; n++) {
            reversedEdges[n] = new ArrayList<Node>();
        }

        for (int nodeValMinusOne = 0; nodeValMinusOne < edges.length; nodeValMinusOne++) {
            ArrayList<Node> neighbors = edges[nodeValMinusOne];
            for (int neighborIndex = 0; neighborIndex < neighbors.size(); neighborIndex++) {
                reversedEdges[neighbors.get(neighborIndex).value - 1].add(nodes.get(nodeValMinusOne));
            }
        }
        Graph reversedGraph = new Graph(nodes, reversedEdges);
        return reversedGraph;
    }

    public static void printGraph(Graph g) {
        ArrayList<Node> nodes = g.nodes;
        ArrayList<Node>[] edges = g.edges;
        for (int n = 0; n < nodes.size(); n++) {
            System.out.print("Node s" + g.nodes.get(n).value + "'s neighbors: ");
            ArrayList<Node> neighbors = edges[n];
            for (int j = 0; j < neighbors.size(); j++) {
                System.out.print("s" + neighbors.get(j).value + " ");
            }
            System.out.println();
        }
        System.out.println("--------------------");
    }

    public static void main(String[] args) {
        ArrayList<Graph> graphs = new ArrayList<>();
        ArrayList<Graph> reversedGraphs = new ArrayList<>();
        int numTestCases;

        // try (Scanner s = new Scanner(new File("input.txt"))) {
        Scanner s = new Scanner(System.in);
        numTestCases = s.nextInt(); // this is the number of graphs we need to make

        for (int i = 1; i <= numTestCases; i++) {
            int currGraphN = s.nextInt(); // obtain n and m
            int currGraphM = s.nextInt();

            if (currGraphN == 0) {
                continue;
            }

            ArrayList<Node> currGraphNodes = new ArrayList<>();
            ArrayList<Node>[] currGraphEdges = new ArrayList[currGraphN];

            for (int n = 1; n <= currGraphN; n++) {
                currGraphNodes.add(new Node(n)); // initialize graph with nodes (1-indexed, start at 1)
                                                 // nodes are also indexed by their values since they are added in
                                                 // order
                currGraphEdges[n - 1] = new ArrayList<Node>();
            }
            for (int m = 0; m < currGraphM; m++) {
                int s1Val = s.nextInt();
                int s2Val = s.nextInt();

                currGraphEdges[s1Val - 1].add(currGraphNodes.get(s2Val - 1));
            }
            graphs.add(new Graph(currGraphNodes, currGraphEdges));
        }
        s.close();

        for (Graph g : graphs) {
            Graph reversedGraph = reverseGraph(g);
            reversedGraphs.add(reversedGraph);
        }

        for (int graphIndex = 0; graphIndex < reversedGraphs.size(); graphIndex++) {
            Graph original = graphs.get(graphIndex);
            Graph reversed = reversedGraphs.get(graphIndex);

            Stack<Node> originalTopSorted = DFS(original.nodes, original.edges); // topolgoical sorting of original
                                                                                 // (sources)
            Stack<Node> reversedTopSorted = DFS(reversed.nodes, reversed.edges); // topological sorting of reversed
                                                                                 // (sinks)

            // convert stack of nodes in top order to ArrayList to match format of DFS
            ArrayList<Node> originalTopOrder = new ArrayList<>();
            ArrayList<Node> reversedTopOrder = new ArrayList<>();

            while (!originalTopSorted.empty()) {
                originalTopOrder.add(originalTopSorted.pop());
            }
            while (!reversedTopSorted.empty()) {
                reversedTopOrder.add(reversedTopSorted.pop());
            }

            DFS(originalTopOrder, reversed.edges); // vertices have SCC numbers now - use the original graph's
                                                   // topological ordering on the reversed graph

            ArrayList<Node>[] originalEdges = original.edges;
            ArrayList<Node> nodes = original.nodes;
            ArrayList<Node>[] reversedEdges = reversed.edges;

            // assemble list of SCCs (list of all nodes in a given SCC at index SCCnum-1)
            int maxSCC = 0;
            // find maximum SCC number in nodes
            for (Node n : originalTopOrder) {
                if (n.scc > maxSCC) {
                    maxSCC = n.scc;
                }
            }
            SCC[] stronglyConnectedComponents = new SCC[maxSCC];
            if (stronglyConnectedComponents.length == 1) {
                System.out.println(0);
                continue;
            }

            for (int i = 0; i < stronglyConnectedComponents.length; i++) {
                stronglyConnectedComponents[i] = new SCC(i + 1);
            }
            for (Node n : originalTopOrder) {
                stronglyConnectedComponents[n.scc - 1].members.add(n);
            }

            // find number of sinks
            int sinkCount = 0;
            for (SCC component : stronglyConnectedComponents) {
                for (Node node : component.members) {
                    for (int neighborIndex = 0; neighborIndex < originalEdges[node.value - 1].size(); neighborIndex++) {
                        if (originalEdges[node.value - 1].get(neighborIndex).scc != node.scc) {
                            component.isSink = false;
                        }
                    }
                }
                if (component.isSink) {
                    sinkCount++;
                }
            }

            // find number of sources
            int sourceCount = 0;
            for (SCC component : stronglyConnectedComponents) {
                for (Node node : component.members) {
                    for (int neighborIndex = 0; neighborIndex < reversedEdges[node.value - 1].size(); neighborIndex++) {
                        if (reversedEdges[node.value - 1].get(neighborIndex).scc != node.scc) {
                            component.isSource = false;
                        }
                    }
                }
                if (component.isSource) {
                    sourceCount++;
                }
            }

            System.out.println(Math.max(sourceCount, sinkCount));
        }
    }
}
