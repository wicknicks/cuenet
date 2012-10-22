import conf, geocoder
from collections import deque
from propagator import *
import networkx as nx
import sys, itertools
from dateutil import parser as dateparser
import pymongo, pickle

from email.utils import parseaddr

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
  #print 'Dups: ', dups
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

def getAllAddresses(addrstr):
  tuples = [parseaddr(addr) for addr in addrstr.split(',')]
  return tuples

def getTime(timestr):
  return dateparser.parse(timestr).strftime('%s')

def emailToEvent(e):
  event = {'id': e['uid'], 'class': 'email', 'participants': []}
  if 'cc' in e: event['participants'].extend(getAllAddresses(e['cc']))
  if 'from' in e: event['participants'].extend(getAllAddresses(e['from']))
  if 'to' in e: event['participants'].extend(getAllAddresses(e['to']))
  if 'time' in e: e['time'] = getTime(e['date'])
  return event

def loadEmails():
  emails = [e for e in DB['emails'].find()
  events = [emailToEvent(e) for e in emails]

  ## group by email addresses
  emailDict = {}
  for ev in events:
    for parts in ev['participants']:
      part = (parts[0].lower(), parts[1].lower())
      if len(part[0]) < 1: continue
      if part[1] in emailDict:
        if part[0] not in emailDict[part[1]]:
          emailDict[part[1]].append(part[0])
      else:
        emailDict[part[1]]=[part[0]]

  count = 0
  for key in emailDict:
    if len(emailDict[key]) > 1:
      count += 1

  #print count, len(events)
  return emailDict, events

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

  #print 'EIX:', entityIndex['Mahi Mir']
  return net

def getUserInfo(uid):
  return DB['fb_users'].find_one({"id": uid})

def computeDistance(gc, address):
  cache = gc.cache
  if address in cache:
    latlon = cache[address]
  else:
    #print 'Requesting', address
    latlon = gc.geocode(address)
    cache[address] = latlon

  latlon = (float(latlon['latitude']), float(latlon['longitude']))
  pos = (33.686887, -117.825348)
  return ((latlon[1]-pos[1])**2 + (latlon[0]-pos[0])**2)**0.5

def joinEntityLists(index1, l2, keyfunc2, valfunc1, valfunc2):
  inv = {}
  for k in index1:
    vals = index1[k]
    for v in vals:
      if v.lower() in inv:
        #print 'Found', v, 'in inverted index'
        inv[v.lower()]['data'].append(k)
      else: inv[v] = {'data': [k], 'checked': False}

  iresult = []
  for item in l2:
    keys = keyfunc2(item)
    if isinstance(keys, str) or isinstance(keys, unicode):
      if keys.lower() in inv:
        #print 'Yes', keys
        ixitem = inv[keys]['data']
        iresult.append( preptuple(valfunc2(item), keys, valfunc1(ixitem)) )
        inv[keys]['checked'] = True
      else:
        iresult.append( preptuple(valfunc2(item), keys.lower(), None) )
    if isinstance(keys, list):
      for l in keys:
        ixitem = inv[l.lower()]['data']
        iresult.append( preptuple(valfunc2(item), l.lower(), valfunc1(ixitem)) )
        inv[l]['checked'] = True

  for v in inv:
    if inv[v]['checked'] == False:
      iresult.append(  preptuple(None, v, valfunc1(inv[v]['data']))  )

  inv.clear()
  fbIndex = {}
  #Aggregate names:
  for r in iresult:
    if 'em_id' in r:
      for em in r['em_id']:
        if em in inv: inv[em].append(r)
        else: inv[em] = [r]
    if 'fb_id' in r:
      fbIndex[em] = r['fb_id']

  print len(fbIndex), 'entities common to both FB & Email'
  fi = codecs.open('fbindex.txt', 'w', 'utf-8')
  for f in fbIndex:
    fi.write(f + ' ' + str(fbIndex[f]) + '\n')
  fi.close()

  result = []
  f = codecs.open('sortedmails.txt', 'w', 'utf-8')
  while len(inv) > 0:
    key, value = inv.popitem()
    #print 'POPITEM', key, value
    kqueue = deque()
    names = set()
    emails = set()
    for v in value:
      names.add(v['name'])
      for e in v['em_id']:
        kqueue.append(e)
        emails.add(e)
      if 'fb_id' in v: fbid = v['fb_id']
    while len(kqueue) > 0:
      k = kqueue.popleft()
      if k not in inv: continue
      data = inv[k]
      #print data
      for d in data:
        names.add(d['name'])
        for es in d['em_id']:
          if es not in emails:
            kqueue.append(es)
            emails.add(es)
      del inv[k]
      #print 'data[', k, '] =>', data, '\n'

    #print '\n\nNames: ', names
    #print 'Emails: ', emails

    xx = {'names': list(names), 'emails': emails}
    for ns in names:
      for item in l2:
        k = keyfunc2(item)
        if ns == k:
          xx['fb_id'] = item['id']
    """
    for e in emails:
      if e in fbIndex:
        xx['fb_id'] = fbIndex[e]
        break
    """
    result.append(xx)

    f.write(str(result[-1]) + '\n')

  f.close()
  return result

def preptuple (d1, key, d2):
  data = {}
  if d1 != None: data['fb_id'] = d1['fb_id']
  data['name'] = key
  if d2 != None: data['em_id'] = d2['em_id']
  return data

def fbEntityToStarEntity(fb):
  return {'fb_id': fb['id']}

def emEntityToStarEntity(em):
  return {'em_id': em}

if __name__ == "__main__":
  print 'Testing dataset:', conf.NAME

  entityIndex, emails = loadEmails()
  dups, entities = loadEntities()

  print len(entities), 'entities from FB'
  print len(entityIndex), 'entities in Email Index'

  #print 'EIX', entityIndex['alexander.behm@gmail.com']

  p = joinEntityLists(entityIndex, entities, \
                      lambda t: t['name'].lower(), \
                      emEntityToStarEntity, fbEntityToStarEntity)

  f = codecs.open('join.txt', 'w', 'utf-8')
  for pp in p:
    f.write(str(pp) + "\n")
  f.close()

  """
  q = {'$or': [
    {'tags.data.name': conf.NAME},
    {'from.name': conf.NAME},
    {"tags.data.name" : "Hooman Homayoun"}
    ]}
  net = construct(dups, entities, loadFBPhotos(q))
  #print len(net.node[conf.ID])
  #print len(dict.keys(net.edge['1389742343']))
  #print len(net.edges())

  event = DB['fb_events'].find_one({"eid" : "259950784109104"})
  print event['attendees']['data'][0]

  p = propagator(net)

  time=1344749324
  participants = [{'id':conf.ID, 'name': conf.NAME},
                  {'id':'545161407', 'name': 'Hooman Homayoun'}]
  ranks = p.propagate(time, participants)

  #ranks = p.propagate(1344749324, event['attendees']['data'])

  f = codecs.open('sethoom' + '_results.txt', 'w', 'utf-8')

  gc = geocoder.Geocoder()
  gc.cache = pickle.load(open('cache.geo', 'r'))

  for x in xrange(0, len(ranks)):
    user = getUserInfo(ranks[x]['id'])
    loc = '""'
    dist = -1
    if 'location' in user:
      loc = '"' + user['location']['name'] + '"'
      dist = computeDistance(gc, user['location']['name'])

    f.write(ranks[x]['name'] + "," + str(ranks[x]['score']) + \
            "," + str(dist) + ", " + loc + "\n")
  f.close()

  #o = open('cache.geo', 'w')
  #pickle.dump(gc.cache, o)
  #o.close()
  """
