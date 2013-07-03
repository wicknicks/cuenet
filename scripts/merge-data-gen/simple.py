import os, random

def getInstanceFile(dirname):
  files = os.listdir(dirname)
  c = 1 + len(filter(lambda a: (a.find('instance.sim') >= 0), files))
  return os.path.join(dirname, 'instance.sim.' + str(c))


depth = 6
countAtLevels = []
for i in range(depth):
  countAtLevels.append(random.randint(1, 5))

writer = open(getInstanceFile('/data/osm'), 'w')
writer.write('/data/osm/uci.roadnet 1 10000 ')
for item in countAtLevels: writer.write(' %d ' % item )
writer.write('\n## 171169284\n')
writer.write('$$ 1822416005\n')
writer.write('0_0,0,10000000,R\n')

print countAtLevels
#countAtLevels = [1, 2, 3, 1]
prod = 1
for k in xrange(len(countAtLevels)):
  count = countAtLevels[k]
  prod = prod * count
  for i in xrange(prod):
    #print str(k) + '_' + str(i%(prod/countAtLevels[k])), '->', str(k+1) + '_' + str(i)
    writer.write(str(k) + '_' + str(i%(prod/countAtLevels[k])) + ' -> ' + str(k+1) + '_' + str(i))
    writer.write('\n')

writer.write('.\n')
writer.close()
