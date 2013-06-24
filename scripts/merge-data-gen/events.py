import random, math

class Interval:
  def __init__(self):
    self.start = 1
    self.end = 10000

class NestedEvent:
  def __init__(self, _id):
    self.eid = _id
    self.subevents = []

  def extend(self, parent, subs):
    # print 'extend eid', self.eid
    if self.eid == parent:
      self.subevents.extend(subs)
    for e in self.subevents: e.extend(parent, subs)

  def randomize(self, ne=None, maxsubeventrepeat=3):
    print 'eid', self.eid, type(self.eid)
    if ne==None: ne = NestedEvent(self.eid)
    for sub in self.subevents:
      r = range(random.randint(0, maxsubeventrepeat))
      print 'R', sub.eid, r
      for j in r:
        _rsub = NestedEvent(sub.eid)
        ne.subevents.append(_rsub)
        sub.randomize(_rsub)
    return ne

  def __str__(self):
    s = '(' + str(self.eid) 
    if len(self.subevents) > 0: s += " ->"
    for e in self.subevents: s += ' ' + str(e)
    return s + ')'

if __name__ == '__main__':
  ne = NestedEvent(1)
  ne.extend(1, map(lambda a: NestedEvent(a), [2, 3]))
  ne.extend(2, map(lambda a: NestedEvent(a), [4, 5]))
  ne.extend(3, map(lambda a: NestedEvent(a), [7, 6]))
  ne.extend(4, map(lambda a: NestedEvent(a), [8]))
  ne.extend(5, map(lambda a: NestedEvent(a), [9]))
  print 'NE', ne
  r = ne.randomize()
  print 'NER', r

if __name__ == '__main__2': #don't run this
  interval = Interval()

  roadnetfile = open('/data/osm/uci.roadnet')
  line = roadnetfile.readline().strip()
  roadnodes, roadedges = map(lambda a: int(a), line.split(','))
  print 'ROAD', roadnodes, 'nodes & ', roadedges, 'edges'

  nodes = {}
  edges = {}
  for line in roadnetfile.readlines():
    if line.find('->') > 0: 
      parts = line.split(' -> ')
      edges[parts[0]] = filter(lambda a: a != '\n', parts[1].split(' '))
    else: 
      parts = line.split(',')
      nodes[parts[0]] = (float(parts[1]), float(parts[2]))

  print(len(nodes), len(edges))

  ontfile = open('/data/osm/events.ont')
  line = ontfile.readline().strip()
  ontconfig = map(lambda a: int(a), line.split(','))

  atomicevents = set()
  halfevents = set()
  fullevents = set()
  events = set()
  nestedevents = []
  nestedeventsindex = {}

  for line in ontfile.readlines():
    if line.find('###') == 0: 
      parts = line.split(' ')
      if parts[1] == 'HALF': halfevents.add(int(parts[2]))
      if parts[1] == 'FULL': fullevents.add(int(parts[2]))
      current = NestedEvent(int(parts[2]))
      nestedevents.append(current)
      nestedeventsindex[int(parts[2])] = current
    else:
      e, subs = line.split(' -> ')
      e = int(e)
      subs = map(lambda a: int(a), subs.split(','))
      events.add(e)
      current.extend(e, map(lambda a: NestedEvent(a), subs))

  ontfile.close()

  ne = nestedeventsindex[nestedeventsindex.keys()[0]].randomize()
  print nestedeventsindex[nestedeventsindex.keys()[0]]
  print ne

  # allevents = set()
  # for i in range(ontconfig[0]): allevents.add(i)
  # atomicevents = allevents - events
  # print('ATOM', sorted(atomicevents) , len(nestedevents))
  # print('HALF', sorted(halfevents), len(halfevents))
  # print('FULL', sorted(fullevents), len(fullevents))

  # print('INTERVAL', interval.start, interval.end)

  # # for one edge
  # testedge = edges.keys()[0]
  # for node in edges[testedge]:
  #   spans = random.randint(2, 5)
  #   for i in range(spans):
  #     eid = random.randint(0, ontconfig[0]-1)
  #     print (eid, node, i*interval.end/spans, (i+1) * interval.end/spans, spans)
  #     if eid in nestedeventsindex: print eid, 'nested'
  #   print ''