from collections import deque
import codecs

class propagator:
  def __init__(self, net):
    self.net = net

  def propagate(self, time, participants):
    timeSortedEvents = []
    net = self.net

    newEvent = {'id': 'ne_1',
                'class': 'photo-capture-event',
                'time': time,
                'participants': participants}

    queue = deque()
    net.add_node(newEvent['id'], attr_dict = newEvent)
    for node in net.nodes():
      if net.node[node]['class'] != 'person': continue
      net.node[node]['fired'] = False
      net.node[node]['score'] = 0.0
      net.node[node]['queued'] = False
      for p in participants:
        if net.node[node]['id'] != p['id']:
          continue
        #print 'Linking', newEvent['id'], 'with', net.node[node]
        net.add_edge(node, newEvent['id'])
        net.node[node]['score'] = 1.0
        queue.append(node)

    c = 0
    for n in net.nodes():
      if ('score' in net.node[n]) and (net.node[n]['score'] > 0): c += 1

    print 'c =', c


    timeSortedEvents = []
    net = self.net

    for node in net.nodes():
      if net.node[node]['class'] == 'photo-capture-event':
        timeSortedEvents.append (net.node[node])

    timeSortedEvents.sort(lambda a, b: a['time'] - b['time'])

    print 'Ranking....'
    return self.rank(net, queue)

  def rank(self, net, queue):
    tqueue = deque()
    ic = 0
    while True:
      if len(queue)==0:
        if len(tqueue)==0: break
        queue = tqueue
        tqueue = deque()
        ic += 1
        print 'Starting Iteration', ic, len(queue)
        if ic == 3: break

      n = queue.popleft()
      net.node[n]['queued'] = False
      net.node[n]['fired'] = True

      #if len(queue) % 20 == 0: print 'Q Size', len(queue)

      edges = net.edge[n]
      for event in dict.keys(edges):
        participants = net.node[event]['participants']
        up = self.average(net.node[n]['score'], participants, damper=0.1**(ic+1))
        for participant in participants:
          if participant['id'] not in net.node: continue
          pNode = net.node[participant['id']]
          pNode['score'] += up
          if pNode['score'] > 100.0: pNode['score'] = 100.0
          if pNode['fired'] == True: continue
          if pNode['queued'] == True: continue
          pNode['queued'] = True
          tqueue.append(participant['id'])

    ranks = []
    for node in net.nodes():
      if net.node[node]['class'] != 'person': continue
      ranks.append(net.node[node])

    ranks.sort(self.rankSorter)
    return ranks

  def average(self, score, participants, damper=1):
    return damper * score/len(participants)

  def rankSorter(self, a, b):
    if a['score'] > b['score']: return -1
    else: return 1
