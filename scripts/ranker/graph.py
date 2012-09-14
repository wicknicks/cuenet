
class Node:
  def __init__(self, label, data={}):
    self.label = label
    self.data = data

  def __hash__(self):
    return self.label.__hash__()

  def __eq__(self, other):
    if type(other) == str: return other == self.label
    return self.label == other.label

  def __str__(self):
    return self.label.encode('ascii', 'ignore')

class Edge:
  def __init__(self, node1, node2, data={}):
    self.start = node1;
    self.end = node2;
    self.data = data

class Graph:
  def __init__(self, data={}):
    self.nodes = {}
    self.data = data;

  def node(self, node):
    if node not in self.nodes:
      self.nodes[node] = {}

  def edge(self, edge):
    edges = self.nodes[edge.start]
    if edge.end not in edges:
      edges[edge.end] = edge

  def getNode(self, label):
    return self.nodes[label]

  def getEdge(self, n1, n2):
    if n2 in self.nodes[n1]:
      return self.nodes[n1][n2]
    else: return None

  def printStats(self):
    print 'Number of Nodes', len(self.nodes)
    c = 1
    for n in self.nodes:
      print ' ' + str(c)+')', n
      if len(dict.keys(self.nodes[n])) > 0:
        print '  ',  ['( -> '+str(e)+' )' for e in self.nodes[n]]
      c += 1
