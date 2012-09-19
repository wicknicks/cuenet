from graph import *
from propagator import *
import sys, itertools
from pymongo import Connection
from dateutil import parser as dateparser

connection = Connection('127.0.0.1', 27017)
DB = connection['setareh']

def loadFBRelationships():
  socNet = []
  for rel in DB['fb_relationships'].find():
    u = []
    u.append({'id': rel['id'], 'name': u'Setareh Rad'});
    u.append({'id': rel['relation'], 'name': rel['relation_name']});
    socNet.append(u)
  return socNet


def loadFBPhotos():
  photoNet = [];
  _count = 0
  c = 0
  for photo in DB['fb_photos'].find({'tags.data.name':'Setareh Rad'}):
    u = []
    for tag in photo['tags']['data']:
      tDict = {}
      if 'id' in tag: tDict['id'] = tag['id']
      else: tDict['id'] = 0
      if 'name' in tag: tDict['name'] = tag['name']
      else: tDict['name'] = '__UNKNOWN__'
      u.append(tDict)
      if tag['name'] == 'Setareh Rad': c+=1
    photoNet.append((photo, u))
    _count += 1
    if _count > 1000: break

  return photoNet


def loadData():
  dups = findDups()
  network = Network(label='Setareh-Test-Net')
  g = Graph(data={'type': 'social'})
  rels = loadFBRelationships()
  for pair in rels:
    n1 = Node(pair[0]['name'], pair[0])
    n2 = Node(pair[1]['name'], pair[1])
    if (pair[0]['name'] not in dups) and (pair[1]['name'] not in dups):
      g.node(n1)
      g.node(n2)
      g.edge(Edge(n1, n2, {'weight': 1}))
  #g.printStats();
  print 'FB Relationships Loaded'
  network.load(g)

  pics = loadFBPhotos()
  #dnf = 0
  for u in pics:
    pic = u[1]
    data = {'type': 'temporal', 'id': u[0]['id']}
    if 'created_time' in u[0]:
      ct = dateparser.parse(u[0]['created_time'])
      data['time'] = int(ct.strftime('%s'))
    #else: dnf += 1
    g = Graph(data=data)
    for pair in itertools.permutations(pic, 2):
      n1 = Node(pair[0]['name'], data={'wt': 0})
      n2 = Node(pair[1]['name'], data={'wt': 0})
      if (pair[0]['name'] not in dups) and (pair[1]['name'] not in dups):
        g.node(n1)
        g.node(n2)
        g.edge(Edge(n1, n2, {'weight': 1}))
    network.load(g)

  print len(pics), 'Photos Loaded'
  return network


def simpleTest():
  nodes = [
    Node('Arjun'),
    Node('Adarsh'),
    Node('Ramesh')]
  edges = [
    Edge(nodes[0], nodes[1]),
    Edge(nodes[0], nodes[2]),
    Edge(nodes[2], nodes[0]),
    Edge(nodes[1], nodes[0])];
  g = Graph()
  for n in nodes: g.node(n)
  for e in edges: g.edge(e)

  print g.getNode('Arjun')
  g.printStats()


def findDups():
  users = [u for u in DB['fb_users'].find()]
  names = [u['name'] for u in users]
  names.sort()
  dups = {}
  for x in xrange(0, len(names)-1):
    if names[x] == names[x+1]: dups[names[x]] = True
  print 'Persons with same name:', len(dict.keys(dups))
  return dups


if __name__ == "__main__":
  #simpleTest()
  network = loadData()
  connection.disconnect()
  network.propagate(time=1344749324, participants=['Setareh Rad'])
  network.printStats()
