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

    """for t in timeSortedList:
      if 'id' not in t.data: continue
      print 'TSL', t.data['id'], [str(u) for u in t.getAllNodes()]
    return
    """

    c = 0
    propagationNet = Graph()
    for t in timeSortedList:
      #if 'Setareh Rad' in t.getAllNodes(): c += 1
      propagationNet.node(t)
    #print 'Setareh in', c, 'graphs'


    for e in dict.keys(entityIndex):
      e.data['wt'] = 0.0
      e.score  = 0.0
      e.fired = False
      e.queued = False
      if e in current.getAllNodes():
        e.data['wt'] = 1.0
        e.score = 1.0
      propagationNet.node(e)

    ec = 0
    for entity in dict.keys(entityIndex):
      for graph in timeSortedList:
        for participant in graph.getAllNodes():
          if participant == entity:
            propagationNet.edge(Edge(entity, graph))
            propagationNet.edge(Edge(graph, entity))
            ec += 2

    print 'Ranking....', ec
    self.rank(propagationNet)

  def rank(self, net):
    queue = deque()
    for n in net.getAllNodes():
      if 'wt' not in n.data: continue
      if n.data['wt'] == 1.0: queue.append(n)

    tqueue = deque()
    ic = 0
    while True:
      if len(queue) == 0:
        if len(tqueue) == 0: break
        queue = tqueue
        tqueue = deque()
        ic += 1
        print 'Starting iteration', ic, len(queue)
        if ic == 3: break

      n = queue.popleft()
      n.queued = False
      n.fired = True

      if len(queue)%20==0: print 'Q Size', len(queue)

      edges = net.getOutgoingEdges(n)
      for event in edges:
        participants = event.getAllNodes()
        up = self.average(n.score, participants, damper=0.1**(ic+1))
        #print n, n.score, len(participants), up
        for part in participants:
          pNode = net.getNode(part)
          #if pNode.fired: continue
          pNode.score += up
          if pNode.score > 100.0: pNode.score = 100.0
          if pNode.queued == True: continue
          pNode.queued = True
          tqueue.append(pNode)

    ranks = []
    for entity in net.getAllNodes():
      if isinstance(entity, Graph): continue
      ranks.append( (entity, entity.score) )

    print '------------------'
    print '    RESULTS      '
    print '------------------'
    f = open('results.txt', 'w')
    ranks.sort(lambda a,b: 1 if b[1] > a[1] else -1)
    for r in range(len(ranks)):
      f.write(str(r) + ". " + str(ranks[r][0]) + " " + str(ranks[r][1]) + "\n")
    f.close()

  def average(self, score, participants, damper=1):
    return damper*score/len(participants)

  def printStats(self):
    times = []
    for graph in self.graphs:
      if 'time' not in graph.data: continue
      times.append(graph.data['time'])

    times.sort()

    print 'Aggregate Network contains', len(self.graphs), 'graphs.'
