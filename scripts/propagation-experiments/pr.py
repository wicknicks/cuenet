import math

# df = 0.85
# constant = (1-df)/4

# A = 0.25
# B = 0.25
# C = 0.25
# D = 0.25

# PR_A = lambda: constant + (df * (B/2 + C))
# PR_B = lambda: constant + (df * (D/2))
# PR_C = lambda: constant + (df * (D/2 + B/2))
# PR_D = lambda: constant + (df * (A))


# formatter = lambda a: "%.5f" % a

# for i in range(40):
#   w, x, y, z = PR_A(), PR_B(), PR_C(), PR_D()
#   delta = abs(w-A) + abs(B-x) + abs(C-y) + abs(D-z)
#   A, B, C, D = w, x, y, z
#   print i+1, formatter(A), formatter(B), formatter(C), formatter(D), formatter(delta)


# A = 0.5
# B = 0.5

# PR_A = lambda: constant + df * (A)
# PR_B = lambda: constant + df * (B)

# for i in range(100):
#   w, x= PR_A(), PR_B()
#   A, B = w, x
#   print A, B


# #times = [70, 80, 90, 100, 110, 120, 130]
# times = [30, 40, 90, 100, 110, 120, 130]
# # neighbors = [ [1, 2], [0, 2, 3], [0, 1, 3, 4], [1, 2, 4, 5], [2, 3, 5, 6], [3, 4, 6], [4, 5] ]
# neighbors = [ [1], [0], [3, 4], [2, 4, 5], [2, 3, 5, 6], [3, 4, 6], [4, 5] ]
# scores = [ [1.0, 1.0, 0, 0, 0],  
#            [1.0, 0, 1.0, 0, 0], 
#            [0, 0, 1.0, 0, 0], 
#            [0, 0, 1.0, 1.0, 1.0], 
#            [0, 0, 0, 1.0, 1.0], 
#            [0, 0, 0, 1.0, 1.0], 
#            [0, 0, 0, 0, 1.0] ]

# # initial_probabilities = [0.285, 0.142, 0.425, 0.425, 0.571]
# initial_probabilities = [0.2, 0.2, 0.2, 0.2, 0.2]

# tempMatrix = []
# TMAX = 25

# for j in range(2):

#   tempMatrix = []

#   for objix in range(0, len(scores)):
#     tempScores = list(initial_probabilities)
#     for neighbor in neighbors[objix]:
#       timed = abs(times[objix] - times[neighbor])
#       # print times[neighbor]
#       if timed > TMAX: continue
#       tscore = 0
#       ix = 0
#       for nscore in scores[neighbor]:
#         tempScores[ix] = tempScores[ix] + (nscore * (TMAX - float(timed)) / TMAX) / len(neighbors[neighbor])
#         ix += 1

#     tempMatrix.append(tempScores)

#     scoreString = ''
#     for item in tempScores: scoreString += "%.3f" % item + "   " 
#     print scoreString
 
#   scores = tempMatrix
#   print '============================================================='


def euclid(pt1, pt2): 
  a = pt1[0] - pt2[0]
  b = pt1[1] - pt2[1]
  return math.sqrt(a * a + b * b)

times = {'x':70, 'y':80, 'z':90, 'w':100}
Tmax = 200

scores = {'x':[1, 1, 1, 0, 0], 'y':[0, 1, 1, 1, 0], 'z':[0, 0, 0, 1, 1], 'w':[1, 1, 0, 0, 0]}
template = {'x':[0, 0, 0, 0, 0], 'y':[0, 0, 0, 0, 0], 'z':[0, 0, 0, 0, 0], 'w':[0, 0, 0, 0, 0]}
types = {'x': 't1', 'y': 't1', 'z': 't2', 'w': 't2'}

Jq = { 'x':{'x':'-', 'y': 2, 'z': 0, 'w': 2}, 
       'y':{'x':2, 'y': '-', 'z': 1, 'w': 1}, 
       'z':{'x':0, 'y': 1, 'z': '-', 'w': 0}, 
       'w':{'x':2, 'y': 1, 'z': 0, 'w': '-'} }


Od = { 't1': {'t1': 0, 't2': 10},
       't2': {'t1': 10, 't2': 0} }


for iteration in range(10):

  temp = dict(template)
  
  for current in ['x', 'y', 'z', 'w']:

    allnodes = ['x', 'y', 'z', 'w']
    allnodes.remove(current)

    for i in range(5):

      temp[current][i] = scores[current][i]
      if temp[current][i] == 1: continue

      for neighbor in allnodes:
        
        od = Od[types[current]][types[neighbor]] + 1
        # if od == 0: od = 1

        jq = Jq[current][neighbor] + 1
        #if jq == 0: jq = 1

        timed = float(abs(times[neighbor] - times[current]))
        timed = 1 - (timed / Tmax)

        temp[current][i] +=  timed * scores[neighbor][i]/(od * jq)

      temp[current][i] /= 4

    ss = ''
    for item in temp[current]: ss += "%.3f" % item + "   " 
    print current, ss

  print '============================================================='
  scores = temp