import os, random

def getInstanceFile(dirname):
  files = os.listdir(dirname)
  c = 1 + len(filter(lambda a: (a.find('instance.sim') >= 0), files))
  return os.path.join(dirname, 'instance.sim.' + str(c))

locations = 10
depth = 3

edgeids = set()
nodeids = set()

while len(edgeids) != locations:
  edgeids.add( random.randint(10000, 99999) )
edgeids = list(edgeids)

while len(nodeids) != locations:
  nodeids.add( random.randint(1000000, 9999999) )
nodeids = list(nodeids)

writer = open(getInstanceFile('/data/osm/multi'), 'w')
writer.write('/data/osm/uci.roadnet 1 10000\n')

for _l in range(locations):
  countAtLevels = []
  for i in range(depth):
    countAtLevels.append(random.randint(2, 4))

  writer.write('## ' + str(edgeids[_l]) + '\n')
  writer.write('$$ ' + str(nodeids[_l]) + '\n')
  writer.write('0_0,0,10000000,R\n')

  print countAtLevels
  prod = 1
  for k in xrange(len(countAtLevels)):
    count = countAtLevels[k]
    prod = prod * count
    for i in xrange(prod):
      writer.write(str(k) + '_' + str(i%(prod/countAtLevels[k])) + ' -> ' + str(k+1) + '_' + str(i))
      writer.write('\n')

  writer.write('.\n')
writer.close()
