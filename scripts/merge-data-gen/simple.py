import os, random

def getInstanceFile(dirname):
  files = os.listdir(dirname)
  c = 1 + len(filter(lambda a: (a.find('instance.sim.') >= 0), files))
  return os.path.join(dirname, 'instance.sim.' + str(c))

def create_instances(instancefile, maxnode, depth):
  countAtLevels = []
  for i in range(depth):
    countAtLevels.append(random.randint(2, 8))

  writer = open(instancefile, 'w')
  writer.write('/data/osm/uci.roadnet 1 10000 ')
  for item in countAtLevels: writer.write(' %d ' % item )
  writer.write('\n## 171169284\n')
  writer.write('$$ 1822416005\n')
  writer.write('0_0,0,10000000,R\n')

  #countAtLevels = [8, 2, 8, 2, 6, 2, 5, 3, 5, 4]

  print countAtLevels, reduce(lambda a, b: a*b, countAtLevels), instancefile

  prod = 1
  for k in xrange(len(countAtLevels)):
    count = countAtLevels[k]
    prod = prod * count
    for i in xrange(prod):
      #print str(k) + '_' + str(i%(prod/countAtLevels[k])), '->', str(k+1) + '_' + str(i)
      writer.write(str(k) + '_' + str(i%(prod/countAtLevels[k])) + ' -> ' + str(k+1) + '_' + str(i))
      writer.write('\n')
      maxnode -= 1
      if maxnode == 0: break
    if maxnode == 0: break

  writer.write('.\n')
  writer.close()


if __name__ == "__main__":

  ## params
  depth = 10  
  maxnode = 1000
  
  ## create one instance file
  # instancefile = '/home/arjun/data/cuenet/nodemerge/prime.sim.1000000'
  # create_instances(instancefile, maxnode, depth)

  print "Params: (depth, maxnode)", depth, maxnode

  ## create many instance files
  for i in range(1000):
    instancefile = getInstanceFile('/home/arjun/data/cuenet/multimerge')
    create_instances(instancefile, maxnode, depth)