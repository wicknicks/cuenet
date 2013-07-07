import os, random

def getInstanceFile(dirname):
  files = os.listdir(dirname)
  c = 1 + len(filter(lambda a: (a.find('instance.sim') >= 0), files))
  return os.path.join(dirname, 'instance.sim.' + str(c))

locations = 10000
depth = 8

edgeids = set()
nodeids = set()

while len(edgeids) != locations:
  edgeids.add( random.randint(10000, 99999) )
edgeids = list(edgeids)

while len(nodeids) != locations:
  nodeids.add( random.randint(1000000, 9999999) )
nodeids = list(nodeids)

instancefile = getInstanceFile('/home/arjun/data/cuenet/size/tree')
print 'Writing into', instancefile
writer = open(instancefile, 'w')
writer.write('/data/osm/uci.roadnet 1 10000\n')

countAtLevels = [2, 3, 4, 5, 5]
print countAtLevels, reduce(lambda a, b: a*b, countAtLevels), locations

for _loc in range(locations):
  
  #countAtLevels = []
  #for i in range(depth): countAtLevels.append(random.randint(2, 5))

  writer.write('## ' + str(edgeids[_loc]) + '\n')
  writer.write('$$ ' + str(nodeids[_loc]) + '\n')
  writer.write('0_0,0,10000000,R\n')

  #print countAtLevels, reduce(lambda a, b: a*b, countAtLevels)
  prod = 1
  for k in xrange(len(countAtLevels)):
    count = countAtLevels[k]
    prod = prod * count
    for i in xrange(prod):
      writer.write(str(k) + '_' + str(i%(prod/countAtLevels[k])) + ' -> ' + str(k+1) + '_' + str(i))
      writer.write('\n')

  writer.write('.\n')
writer.close()
