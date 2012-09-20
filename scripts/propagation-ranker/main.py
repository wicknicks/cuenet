import conf
from propagator import *
import networkx as nx
import sys, itertools
from dateutil import parser as dateparser
import pymongo

connection = pymongo.Connection('127.0.0.1', 27017)
DB = connection[conf.DB]

def loadEntities():
  entities = []
  dups = []
  names = []
  entities.append({'id': conf.ID, 'name': conf.NAME, 'class': 'person'})
  for rel in DB['fb_relationships'].find():
    u = {'id': 0, 'name': '__UNKNOWN__', 'class': 'person'}
    if 'relation' in rel: u['id'] = rel['relation']
    if 'relation_name' in rel: u['name'] = rel['relation_name']
    if u['id'] == 0: print 'Relation without an ID!', rel
    entities.append(u)
    names.append(u['name'])
  names.sort()
  for x in xrange(0, len(names)-1):
    if names[x] == names[x+1]: dups.append(names[x])
  print 'Dups: ', dups
  return dups, entities

def loadFBPhotos(q=None):
  photoNet = []
  if q is None: q = {'tags.data.name': conf.NAME}
  pindex = {}

  for photo in DB['fb_photos'].find(q):
    if photo['id'] in pindex: continue
    pindex[photo['id']] = True

    participants = []
    for tag in photo['tags']['data']:
      tDict = {'id': 0, 'name': '__UNKNOWN__'}
      if 'id' in tag: tDict['id'] = tag['id']
      if 'name' in tag: tDict['name'] = tag['name']
      participants.append(tDict)

    net = {'id': photo['id'], 'class': 'photo-capture-event'}
    if 'created_time' not in photo:
      print 'No "created_time" field found in', photo['id']
      continue

    net['time'] = dateparser.parse(photo['created_time'])
    net['time'] = int(net['time'].strftime('%s'))
    net['participants'] = participants
    photoNet.append( net )
    #if len(photoNet) > 10: break

  return photoNet

def construct(dups, entities, photos):
  net = nx.Graph()
  entityIndex = {}

  for e in entities:
    if e['name'] in dups: continue
    net.add_node(e['id'], attr_dict=e)
    entityIndex[e['name']] = e['id']

  for p in photos:
    net.add_node(p['id'], attr_dict=p)
    for parts in p['participants']:
      if parts['name'] in dups:
        #print 'Found Dup', p['id']
        continue
      if parts['name'] not in entityIndex:
        continue
      net.add_edge(entityIndex[parts['name']], p['id'])
      
  print 'EIX:', entityIndex['Mahi Mir']
  return net

if __name__ == "__main__":
  print 'Testing dataset:', conf.NAME
  dups, entities = loadEntities()
  net = construct(dups, entities, loadFBPhotos())
  #print len(dict.keys(net.edge['1389742343']))
  #print len(net.edges())
  p = propagator(net)
  p.propagate(time=1344749324, participants=[{'id':conf.ID, 'name': conf.NAME}])
