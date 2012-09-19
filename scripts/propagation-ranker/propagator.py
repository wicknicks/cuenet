from graph import *
from collections import deque

class Network:
  def __init__(self, label='default', data={}):
    self.graphs=[]
    self.label=label
    self.data=data

  def load(self, graph):
    self.graphs.append(graph)

  # Return a list of Nodes sorted by time
  def buildTimeIndex(self, graphs):
    temporal = []
    for g in graphs:
      if g.data['type'] is 'temporal':
        temporal.append(g)
    temporal.sort(lambda a,b: a.data['time']-b.data['time'])
    return temporal

  def unifyEntities(self, graphs):
    entities = {}
    for g in graphs:
      if g.data['type'] is not 'temporal': continue
      nodes = g.getAllNodes()
      for node in nodes:
        if node not in entities: entities[node] = node

    return entities

  def propagate(self, time=0, participants=[]):
    current = Graph(data={'type': 'temporal', 'time': time})
    for p in participants: current.node(Node(p))
    self.graphs.append(current)

    timeSortedList = self.buildTimeIndex(self.graphs)
    #print timeSortedList[0].data['time'], timeSortedList[-1].data['time']

    entityIndex = self.unifyEntities(self.graphs)

    c = 0
    propagationNet = Graph()
    for t in timeSortedList:
      if 'Setareh Rad' in t.getAllNodes(): c += 1
      propagationNet.node(t)
    print 'Setareh in', c, 'graphs'

    for e in dict.keys(entityIndex):
      e.data['wt'] = 0.0
      e.score  = 0.0
      e.fired = False
      if e in current.getAllNodes():
        e.data['wt'] = 1.0
        e.score = 1.0
      propagationNet.node(e)

    for entity in dict.keys(entityIndex):
      for graph in timeSortedList:
        for participant in graph.getAllNodes():
          if participant == entity:
            propagationNet.edge(Edge(entity, graph))
            propagationNet.edge(Edge(graph, entity))

    print 'Ranking....'
    self.rank(propagationNet)

  def rank(self, net):
    queue = deque()
    for n in net.getAllNodes():
      if 'wt' not in n.data: continue
      if n.data['wt'] == 1.0: queue.append(n)

    while len(queue) > 0:
      n = queue.popleft()
      n.fired = True
      edges = net.getOutgoingEdges(n)
      for event in edges:
        participants = event.getAllNodes()
        up = self.average(n.score, participants)
        #print n, n.score, len(participants), up
        for part in participants:
          pNode = net.getNode(part)
          if pNode.fired: continue
          pNode.score += up
          if pNode.score > 1: pNode.score = 1.0
          queue.append(pNode)

    ranks = []
    for entity in net.getAllNodes():
      if isinstance(entity, Graph): continue
      ranks.append( (entity, entity.score) )

    print '------------------'
    print '    RESULTS      '
    print '------------------'
    ranks.sort(lambda a,b: 1 if b[1] > a[1] else -1)
    for r in range(21): print ranks[r][0], ranks[r][1]

  def average(self, score, participants):
    return score/len(participants)

  def printStats(self):
    times = []
    for graph in self.graphs:
      if 'time' not in graph.data: continue
      times.append(graph.data['time'])

    times.sort()

    print 'Aggregate Network contains', len(self.graphs), 'graphs.'
