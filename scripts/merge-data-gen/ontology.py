# generate ontology. given the number of events, generate fake subevent relations

import random

class Writer:
  def __init__(self, fname):
    self.ofile = open(fname, 'w')

  def write_header(self, count, maxdepth, maxsubs):
    self.ofile.write(str(count) + ",")
    self.ofile.write(str(maxdepth) + ",")
    self.ofile.write(str(maxsubs) + "\n")

  def start(self, comment=''):
    self.ofile.write('###' + comment + '\n')

  def write_sub(self, event, sub):
    self.ofile.write(str(event) + " -> " + str(sub) + "\n")

  def write_sublist(self, event, subevents):
    s = ','.join(map(lambda i: str(i), subevents))
    self.ofile.write(str(event) + " -> " + s + "\n")

  def close(self):
    self.ofile.close()

def generate_half_depth(event, events, seen, writer, depth, maxdepth=5, maxsubs=10):
  if maxdepth == depth: return seen

  subs = []
  for i in range(maxsubs):
    r = abs(random.gauss(0, 1))
    if r < 2 * 1/(1+maxdepth-depth): 
      break

    sub = events[random.randint(0, len(events)-1)]
    if sub in seen: continue
    subs.append(sub)
    seen[sub] = True

  if len(subs) > 0: 
    # print(event, "SUBS", subs)
    writer.write_sublist(event, subs)

  for sub in subs:
    seen.update(generate_half_depth(sub, events, seen, writer, depth+1, maxdepth, maxsubs))

  return seen

def generate_full_depth(event, events, seen, writer, maxdepth=5, maxsubs=10):
  if maxdepth == 0:
    if abs(random.gauss(0, 1)) > 1: 
      return seen

  subs = []
  thresh = 0
  for i in range(maxsubs):
    if maxdepth == 0: thresh = 0.25
    else: thresh = i*.25
    if abs(random.gauss(0, 1)) < thresh: break

    if len(events) <= 0: break

    sub = events[random.randint(0, len(events)-1)]
    if sub in seen: continue
    subs.append(sub)
    seen[sub] = True

  if len(subs) > 0: 
    # print(event, "SUBS", subs)
    writer.write_sublist(event, subs)

  for sub in subs:
    seen.update(generate_full_depth(sub, events, seen, writer, maxdepth-1, maxsubs))

  return seen
  
def generate_zone(count, writer, maxdepth=5, maxsubs=10):
  var = 1.0
  zone=[0, 0, 0]
  events = [i for i in range(count)] 
  for i in range(count):
    r = abs(random.gauss(0, var)) 
    if r < 0.75 * var: 
      zone[0] += 1
      # print('SOLO', i)
    elif 0.75 * var < r < 1.5*var: 
      zone[1] += 1
      # print('HALF DEPTH', i)
      writer.start(' HALF ' + str(i))
      generate_half_depth(i, events, {i: True}, writer, 0, maxdepth, maxsubs)
    else: 
      zone[2] += 1
      # print('FULL DEPTH', i)
      writer.start(' FULL ' + str(i))
      generate_full_depth(i, events, {i: True}, writer, maxdepth, maxsubs)
  
  print(zone)

if __name__ == '__main__':
  writer = Writer('events.ont')
  generate_zone(100, writer, 3, 3)
  writer.close()

  # writer = Writer('events.ont')
  # depth = generate(10, writer)
  # print ('Max Depth of Ont Tree', depth)
  # writer.close()