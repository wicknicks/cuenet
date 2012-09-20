class propagator:
  def __init__(self, net):
    self.net = net

  def propagate(self, time, participants):
    timeSortedEvents = []
    net = self.net
    print len(net.nodes())
    for node in net.nodes():
      if net.node[node]['class'] == 'photo-capture-event':
        timeSortedEvents.append (net.node[node])

    print len(timeSortedEvents)
    timeSortedEvents.sort(lambda a,b: a['time']-b['time'])


