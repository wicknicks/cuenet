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
    if self.eid == parent:
      self.subevents.extend(subs)
    for e in self.subevents: e.extend(parent, subs)

  def randomize(self, ne=None, maxsubeventrepeat=3):
    if ne==None: ne = NestedEvent(self.eid)
    for sub in self.subevents:
      for j in range(random.randint(0, maxsubeventrepeat)):
        ne.subevents.append(NestedEvent(sub))
    for sub in ne.subevents: 
      sub.randomize(sub)
    return ne

  def __str__(self):
    s = '(' + str(self.eid) + " ->"
    for e in self.subevents: s += ' ' + str(e)
    return s + ')'

interval = Interval()

if __name__ == '__main__':
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