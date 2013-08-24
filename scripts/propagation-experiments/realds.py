import math, json

def euclid(pt1, pt2): 
  a = pt1[0] - pt2[0]
  b = pt1[1] - pt2[1]
  return math.sqrt(a * a + b * b)

dataset = open('/data/test_photos/arjun/prop/dataset').readlines()

candidateSet = set()
for line in dataset:
  for p in json.loads(line.split(' ')[-1].strip()): candidateSet.add(p)

candidateSet = list(candidateSet)

scores = {}
times = {}
participants = {}
locations = {}

Jq = { }

for line in dataset:
  parts = line.split(' ')
  locations[parts[0]] = (float(parts[1]), float(parts[2]))
  times[parts[0]] = long(parts[3]) - 1338152019
  scores[parts[0]] = [0] * len(candidateSet)
  participants[parts[0]] = set(json.loads(line.split(' ')[-1].strip()))
  for p in json.loads(line.split(' ')[-1].strip()): 
    scores[parts[0]][candidateSet.index(p)] = 1

for key in scores.keys():
  Jq[key] = {}
  for key2 in scores.keys():
    jq = float(len(participants[key].intersection(participants[key2])))
    jq /= float(len(participants[key].union(participants[key2])))
    Jq[key][key2] = jq

Lmax = 50
Tmax = max(times.values())

# for i in Jq: print i, Jq[i]

current = 'IMG_20121122_192458.jpg'
template = {}
for k in scores.keys(): template[k] = [0] * len(scores[k])    

for iteration in range(10):

  temp = dict(template)
  
  for current in scores.keys():

    allnodes = list(scores.keys())
    allnodes.remove(current)

    for i in range( len(candidateSet) ):

      temp[current][i] = scores[current][i]
      if temp[current][i] == 1: continue

      for neighbor in allnodes:
        
        jq = Jq[current][neighbor]
        if jq == 0: jq = 10

        timed = float(abs(times[neighbor] - times[current]))
        timed = 1 - (timed / Tmax)

        locd = float(euclid(locations[current], locations[neighbor]))
        locd = 1 - (locd / Lmax)

        temp[current][i] +=  locd * timed * scores[neighbor][i]/(jq)

      temp[current][i] /= len(scores)

    ss = ''
    for item in temp[current]: ss += "%.3f" % item + "   " 
    print current[:10], '    ', ss

  print '================================================='
  scores = temp

print candidateSet