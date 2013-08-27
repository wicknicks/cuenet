import facebook, json
import codecs

AUTH = "CAACNyZCXDfgEBAH2UvGQmnNv5pRGzmSUntL5sZCYH5T89nZBvAuNoz3op6suzStZBMWtaKmlt1sVBcOlIfyTCRvmSdDFmYNg9cqW8kKX8UktaYNhz061LnjbjOpprImnvilmlM8CXknOl7sQQeIqgvixtj3ZBxKZCQUp7rtczxOTUss3M932r3dg5ixRXHLxMZD"

graph = facebook.GraphAPI(AUTH)
profile = graph.get_object("me")
friends = graph.get_connections("me", "friends")

persons = {"me": profile}

for friend in friends['data']:
  persons[ friend['id'] ] = friend

user_album = {}

count = 0
for p in persons.keys(): 
  count+=1

  if count % 5 == 0: print count, 'profiles scanned'
  if count == 5000: break

  albums = graph.get_connections(p, "albums")

  ualbs = []
  for alb in albums['data']:
    if 'comments' in alb: del alb['comments']
    if 'likes' in alb: del alb['likes']
    if 'location' in alb:
      # print 'location', alb['location']
      ualbs.append(alb)

  if len(ualbs) > 0:
    user_album[p] = ualbs

for u in user_album.keys():
  print u, persons[u]['name'], len(user_album[u])

print '\n', len(user_album), 'Users with location tagged albums\n'

album_photo = {}
all_photos = {}

count = 0
for u in user_album.keys():

  count += 1
  if count % 5 == 0: print count, 'users\' albums crawled'
  if count == 5000: break

  albums = user_album[u]
  for album in albums:
    apics = []
    photos = graph.get_connections(album['id'], "photos")
    for p in photos['data']:
      if 'likes' in p: del p['likes']
      if 'comments' in p: del p['comments']
      all_photos[p['id']] = p
      # print p['id'], type(p['id']), len(all_photos)
      apics.append(p['id'])
    album_photo[album['id']] = apics

print len(album_photo), len(all_photos)

# print user_album[user_album.keys()[0]]
# print album_photo[album_photo.keys()[0]]
# print all_photos[all_photos.keys()[0]]

with codecs.open('/data/facebook/tag-propagation/user_album.txt', 'w') as channel:
  for k in user_album.keys():
    channel.write(k)
    channel.write(" _$_ ") 
    channel.write(json.dumps(user_album[k], sort_keys=True))
    channel.write('\n')

with codecs.open('/data/facebook/tag-propagation/album_photo.txt', 'w') as channel:
  for k in album_photo.keys():
    channel.write(k)
    channel.write(' _$_ ')
    channel.write(json.dumps(album_photo[k], sort_keys=True))
    channel.write('\n')

with codecs.open('/data/facebook/tag-propagation/all_photos.txt', 'w') as channel:
  for k in all_photos.keys():
    channel.write(k)
    channel.write(' _$_ ')
    channel.write(json.dumps(all_photos[k], sort_keys=True))
    channel.write('\n')    


#
# New album
#

for albid in album_photo.keys():
  albtags = set()
  for albphoto in album_photo[albid]:
    photo = all_photos[albphoto]
    if 'tags' not in photo: continue
    for tag in photo['tags']['data']:
      if 'id' not in tag: continue
      if 'name' not in tag: continue
      albtags.add(tag['id'])
      id_user[tag['id']] = tag['name']
  if len(albtags) > 0: album_tags[albid] = list(albtags)

print len(album_tags), len(id_user)

with codecs.open('/data/facebook/tag-propagation/album_tags.txt', 'w') as channel:
  for k in album_tags.keys():
    channel.write(k)
    channel.write(' _$_ ')
    channel.write(json.dumps(album_tags[k], sort_keys=True))
    channel.write('\n')

with codecs.open('/data/facebook/tag-propagation/id_user.txt', 'w') as channel:
  for k in id_user.keys():
    channel.write(k)
    channel.write(' _$_ ')
    channel.write(json.dumps(id_user[k], sort_keys=True))
    channel.write('\n')