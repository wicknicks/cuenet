import dateutil.parser as parser
import pickle

ALBUMS = range(0, 1000)
CANDIDATES = range(0, 50)
ALBUM_COUNT = len(ALBUMS)

def euclid(pt1, pt2): 
  a = pt1[0] - pt2[0]
  b = pt1[1] - pt2[1]
  return math.sqrt(a * a + b * b)


def compute_max_delta(temp, scores):
  deltas = []
  for tkey in temp.keys():
    tscores = temp[tkey]
    oscores = scores[tkey]
    s = 0
    for i in range(len(tscores)):
      s += tscores[i] - oscores[i]
    deltas.append( abs (s) )
  return max(deltas)

participant_tags = {}
for line in range(0, ALBUM_COUNT):
  album_id = ALBUMS[line]
  participants = [ CANDIDATES[random.randint(1, len(CANDIDATES)) - 1] for i in range(random.randint(2, 4)) ]
  participant_tags[album_id] = set(participants)

candidateSet = list(CANDIDATES)

scores = {}
times = {}
participants = {}
locations = {}
Jq = {}

for album_id in participant_tags.keys():
  scores[album_id] = [0] * len(candidateSet)
  locations[album_id] = ( random.randint(0, 360) - 180, random.randint(0, 360) - 180 )
  times[album_id] = random.randint(0, 10000)
  participants[album_id] = set(participant_tags[album_id])
  for p in participants[album_id]:    
    if p not in candidateSet: continue
    scores[album_id][candidateSet.index(p)] = 1
  if sum(scores[album_id]) == 0:
      print album_id, 'has 0 total score'
      del scores[album_id]
      del locations[album_id]
      del times[album_id]

for key in scores.keys():
  Jq[key] = {}
  for key2 in scores.keys():
    jq = float(len(participants[key].intersection(participants[key2])))
    jq /= float(len(participants[key].union(participants[key2])))
    Jq[key][key2] = jq


template = {}
for k in scores.keys(): template[k] = [0] * len(scores[k])    


L_frac = 1.0 / euclid((0, 0), (200, 200))
T_frac = 1.0 / max(times.values())

iteration = 1
mdelta = 1
while mdelta > 0.0001:

  print "Iteration #", iteration
  iteration += 1
  #temp = dict(template)
  temp = {}
  for k in scores.keys(): temp[k] = [0] * len(scores[k])      
  
  for current in scores.keys():

    allnodes = list(scores.keys())
    allnodes.remove(current)

    for i in range( len(candidateSet) ):

      temp[current][i] = scores[current][i]
      if temp[current][i] == 1: continue
      else: temp[current][i] = 0

      for neighbor in allnodes:
        
        jq = Jq[current][neighbor]
        if jq == 0: jq = 10

        timed = float(abs(times[neighbor] - times[current]))
        timed = T_frac * timed
        if timed > 1: timed = 0
        else: timed = 1 - timed

        locd = float(euclid(locations[current], locations[neighbor]))
        locd = L_frac * locd
        if locd > 1: locd = 0
        else: locd = 1 - locd

        temp[current][i] +=  locd * timed * scores[neighbor][i]/(jq)

      temp[current][i] /= len(scores)

  mdelta = compute_max_delta(temp, scores)
  print '=================================================', mdelta

  scores = temp