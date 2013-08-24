# /data/test_photos/tina/highres/IMG_7217.JPG 1361282622000 33.671506 -117.833928

annotations = {}
exif = {}

times = {}
for line in open('/data/test_photos/tina/highres/exif.cache').readlines():
  p = line.split(' ')
  t = long(p[1])/1000 - 1361282622
  exif[p[0]] = t
  times[p[0]] = t

print 'count = ', len(exif)

vtimes = times.values()
TMAX = 1 * (max(vtimes) - min(vtimes)) / 100
print 'TMAX = ', TMAX

neighbors = {}

for photo in exif.keys():
  neighbors[photo] = []
  for neighbor in exif.keys():
    if photo == neighbor: continue
    if abs(exif[photo] - exif[neighbor]) < TMAX:
      neighbors[photo].append(neighbor)

candidateSet = set()

for key in exif.keys():
  anns = open(key + ".annotations").read()
  anns = anns.replace('"', '')
  anlist = anns.split('\n')
  anlist = filter (lambda i: len(i) > 0, anlist)
  annotations[key] = anlist

  for a in anlist: 
    candidateSet.add(a)

candidateSet = list(candidateSet)

scores = {}
for key in annotations.keys():
  score = [0] * len(candidateSet)
  for ann in annotations[key]:
    score[candidateSet.index(ann)] = 1
  scores[key] = score


initial_probabilities = [0] * len(candidateSet)
for i in range(len(candidateSet)):
  total = 0
  for pic in scores.values():
    total += pic[i]
  initial_probabilities[i] = float(total) / len(scores)

for i in range(len(candidateSet)):
  print i, candidateSet[i], initial_probabilities[i]


tempMatrix = []

for j in range(10):

  tempMatrix = {}
  for objix in exif.keys():
    tempScores = list(initial_probabilities)
    for neighbor in neighbors[objix]:
      timed = abs(times[objix] - times[neighbor])
      # print times[neighbor]
      if timed > TMAX: continue
      tscore = 0
      ix = 0
      for nscore in scores[neighbor]:
        tempScores[ix] += (nscore * (TMAX - float(timed)) / TMAX) / len(neighbors[neighbor])
        ix += 1

    tempMatrix[objix] = tempScores

  scoreString = ''

  for item in tempMatrix[tempMatrix.keys()[10]]: scoreString += "%.3f" % item + "   " 
  print scoreString

  scores = tempMatrix
  print '============================================================='

print tempMatrix.keys()[10]