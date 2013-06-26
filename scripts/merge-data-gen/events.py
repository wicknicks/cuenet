import random, math, sys

class Interval:
  def __init__(self):
    self.start = 1
    self.end = 10000

class NestedEvent:
  GenCounts = []
  def __init__(self, _id):
    self.eid = _id
    self.subevents = []
    self.instance = None

  def extend(self, parent, subs):
    # print 'extend eid', self.eid
    if self.eid == parent:
      self.subevents.extend(subs)
    for e in self.subevents: e.extend(parent, subs)

  def gen_id(self):
    self.instance = str(self.eid) + '_' + str(NestedEvent.GenCounts[self.eid])
    NestedEvent.GenCounts[self.eid] += 1

  def randomize(self, ne=None, maxsubeventrepeat=3):
    # print 'eid', self.eid, type(self.eid)
    if ne==None: 
      ne = NestedEvent(self.eid)
      ne.gen_id()
    for sub in self.subevents:
      r = range(random.randint(0, maxsubeventrepeat))
      # print 'R', sub.eid, r
      for j in r:
        _rsub = NestedEvent(sub.eid)
        _rsub.gen_id()
        ne.subevents.append(_rsub)
        sub.randomize(_rsub)
    return ne

  def str_eid(self):
    if self.instance: return str(self.instance)
    return str(self.eid)

  def serialize(self, writer):
    if len(self.subevents) == 0: return
    for e in self.subevents:
      writer.write(self.instance)
      writer.write(' -> ')
      writer.write(e.instance)
      writer.write('\n')
      e.serialize(writer)

  def __str__(self):
    if len(self.subevents) == 0: return self.str_eid()
    s = '(' + self.str_eid()
    for e in self.subevents: 
      s += ' ' + str(e)
      # if len(s) > 10000: return 'ouch'
    return s + ')'

def testRandomize():
  ne = NestedEvent(1)
  NestedEvent.GenCounts = [0] * 10
  ne.extend(1, map(lambda a: NestedEvent(a), [2, 3]))
  ne.extend(2, map(lambda a: NestedEvent(a), [4, 5]))
  ne.extend(3, map(lambda a: NestedEvent(a), [7, 6]))
  ne.extend(4, map(lambda a: NestedEvent(a), [8]))
  ne.extend(5, map(lambda a: NestedEvent(a), [9]))
  print 'NE', ne
  r = ne.randomize()
  print 'NER', r

  r.serialize(sys.stdout)

#testRandomize()

if __name__ == '__main__':
  interval = Interval()

  eventfile = open('/data/osm/instance.sim', 'w')

  _roadnetfile = '/data/osm/uci.roadnet'
  eventfile.write(_roadnetfile + " " + str(interval.start) + " " + str(interval.end) + '\n')

  roadnetfile = open(_roadnetfile)
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
  NestedEvent.GenCounts = [0] * ontconfig[0];

  atomicevents = set()
  halfevents = set()
  fullevents = set()
  events = set()
  nestedevents = []
  nestedeventsindex = {}

  for line in ontfile.readlines():
    if line.find('###') == 0: 
      parts = line.split(' ')
      eid = int(parts[2])
      if parts[1] == 'HALF': halfevents.add(eid)
      if parts[1] == 'FULL': fullevents.add(eid)
      current = NestedEvent(eid)
      nestedevents.append(current)
      nestedeventsindex[eid] = current
    else:
      e, subs = line.split(' -> ')
      e = int(e)
      subs = map(lambda a: int(a), subs.split(','))
      events.add(e)
      current.extend(e, map(lambda a: NestedEvent(a), subs))

  ontfile.close()

  # ne = nestedeventsindex[nestedeventsindex.keys()[0]].randomize()
  # print nestedeventsindex[nestedeventsindex.keys()[0]]
  # print ne

  allevents = set()
  for i in range(ontconfig[0]): allevents.add(i)
  atomicevents = allevents - events
  
  # for one edge
  count = 0
  for edgeix in range(10):
    testedge = edges.keys()[edgeix]
    eventfile.write('## ' + str(testedge) + '\n')
    # print 'testedge:', testedge
    for node in edges[testedge]:
      eventfile.write('$$ ' + str(node))
      eventfile.write('\n')
      spans = random.randint(2, 5)
      for i in range(spans):
        eid = random.randint(0, ontconfig[0]-1)
        
        ''' skip very large events'''
        if eid in fullevents: continue
        elif eid in halfevents: pass
        else:
          ## generate instances for atomic events
          _instanceid = str(eid) + '_' + str(NestedEvent.GenCounts[eid])
          NestedEvent.GenCounts[eid] += 1
        
        count+=1

        if eid in nestedeventsindex: 
          _randomevent = nestedeventsindex[eid].randomize()
          _instanceid = _randomevent.instance
          # print eid, node, i*interval.end/spans, (i+1) * interval.end/spans, 'R'
          eventfile.write(_instanceid + ',' + str(i*interval.end/spans) + ',' + str((i+1) * interval.end/spans))
          eventfile.write(',R\n')
          _randomevent.serialize(eventfile)
          eventfile.write('.\n')
        else:
          # print eid, node, i*interval.end/spans, (i+1) * interval.end/spans
          eventfile.write(_instanceid + ',' + str(i*interval.end/spans)+ ',' + str((i+1) * interval.end/spans))
          eventfile.write('\n')

  print count, 'events generated'
  eventfile.close()