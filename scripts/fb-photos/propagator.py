import math, json, random
import dateutil.parser as parser
import pickle

def euclid(pt1, pt2): 
  a = pt1[0] - pt2[0]
  b = pt1[1] - pt2[1]
  return math.sqrt(a * a + b * b)

participant_tags = {}
with open('/data/facebook/tag-propagation/album_tags_finals.txt') as dataset:
  for line in dataset.readlines():
    parts = line.split(' _$_ ')
    participant_tags[parts[0]] = json.loads(parts[1])

candidateSet = set()
with open('/data/facebook/tag-propagation/candidates_multi_occurrence.txt') as dataset:
  for line in dataset.readlines(): 
    candidateSet.add(line.strip())

candidateSet = list(candidateSet)

scores = {}
times = {}
participants = {}
locations = {}
Jq = {}

with open('/data/facebook/tag-propagation/albums.txt') as albums:
  for line in albums.readlines():
    parts = line.split(' _$_ ')
    j = json.loads(parts[1])
    scores[parts[0]] = [0] * len(candidateSet)
    locations[parts[0]] = (j['place']['location']['latitude'], j['place']['location']['latitude'])
    times[parts[0]] = int(parser.parse(j['created_time']).strftime("%s")) - 1147773369
    participants[parts[0]] = set(participant_tags[parts[0]])
    for p in participants[parts[0]]: 
      if p not in candidateSet: continue
      scores[parts[0]][candidateSet.index(p)] = 1
    if sum(scores[parts[0]]) == 0:
      del scores[parts[0]]
      del locations[parts[0]]
      del times[parts[0]]

for key in scores.keys():
  Jq[key] = {}
  for key2 in scores.keys():
    jq = float(len(participants[key].intersection(participants[key2])))
    jq /= float(len(participants[key].union(participants[key2])))
    Jq[key][key2] = jq

tkey = None
tix = None
bkupScore = {}
tkeylist = []

for key in scores:
  if sum(scores[key]) <= 1: continue
  tkeylist.append(key)

tkey = tkeylist[ random.randint(0, len(tkeylist)-1 ) ]
  
bkupScore = list(scores[tkey])
tix = scores[tkey].index(1)
scores[tkey][tix] = 0

print 'tkey', tkey, '; tix', tix

print 'len(scores[tkey])', len(scores[tkey])
print 'len(scores)', len(scores)

with open('bkupScore.logs', 'w') as t:
  t.write(json.dumps(bkupScore))

with open('score.logs', 'w') as t:
  t.write(json.dumps(scores[tkey]))

Lmax = 50
Tmax = max(times.values())

current = scores.keys()[3]
template = {}
for k in scores.keys(): template[k] = [0] * len(scores[k])    

for iteration in range(2):

  print "Iteration #", iteration
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
    #print current[:10], '    ', ss

  print '================================================='
  scores = temp

print len(candidateSet)

with open('result.logs', 'w') as t:
  t.write(json.dumps(scores[tkey]))

results = []
ix = 0
for r in scores[tkey]:
  results.append( [ r, ix ] )
  ix += 1

results.sort()
ix = 0
for r in results:
  if r[1] == tix: print 'Position', len(candidateSet) - ix
  ix += 1


# pickle.dump(scores, open('save.p', 'wb'))

