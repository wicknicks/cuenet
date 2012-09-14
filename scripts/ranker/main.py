from graph import *
import sys, itertools
from pymongo import Connection

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
  for photo in DB['fb_photos'].find():
    u = []
    for tag in photo['tags']['data']:
      tDict = {}
      if 'id' in tag: tDict['id'] = tag['id']
      else: tDict['id'] = 0
      if 'name' in tag: tDict['name'] = tag['name']
      else: tDict['name'] = '__UNKNOWN__'
      u.append(tDict)
    photoNet.append(u)
  return photoNet


def loadData():
  dups = findDups()
  g = Graph()
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

  pics = loadFBPhotos()
  for pic in pics:
    for pair in itertools.permutations(pic, 2):
      g = Graph()
      n1 = Node(pair[0]['name'])
      n2 = Node(pair[1]['name'])
      if (pair[0]['name'] not in dups) and (pair[1]['name'] not in dups):
        g.node(n1)
        g.node(n2)
        g.edge(Edge(n1, n2, {'weight': 1}))

  print 'Photos Loaded'


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
    if names[x] == names[x+1]:
      print 'Found person with same name:', names[x]
      dups[names[x]] = True
  return dups


if __name__ == "__main__":
  #simpleTest()
  loadData()
