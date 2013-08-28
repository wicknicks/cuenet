import json, os, sys
import codecs

user_albums = {}
albums = {}
album_photos = {}
all_photos = {}
album_tags = {}
id_user = {}

with codecs.open('/data/facebook/tag-propagation/user_album.txt') as channel:
  for line in channel.readlines():
    parts = line.split(' _$_ ')
    user_albums[parts[0]] = json.loads(parts[1])
    for albs in user_albums[parts[0]]:
      if 'place' not in albs: continue
      if 'location' not in albs['place']: continue
      if 'latitude' not in albs['place']['location']: continue
      albums[albs['id']] = albs

with codecs.open('/data/facebook/tag-propagation/album_photo.txt') as channel:
  for line in channel.readlines():
    parts = line.split(' _$_ ')
    album_photos[parts[0]] = json.loads(parts[1])

# with codecs.open('/data/facebook/tag-propagation/all_photos.txt') as channel:
#   for line in channel.readlines():
#     parts = line.split(' _$_ ')
#     all_photos[parts[0]] = json.loads(parts[1])

print len(user_albums), len(album_photos), len(all_photos)

with codecs.open('/data/facebook/tag-propagation/album_tags.txt') as channel:
  for line in channel.readlines():
    parts = line.split(' _$_ ')
    album_tags[parts[0]] = json.loads(parts[1])

with codecs.open('/data/facebook/tag-propagation/id_user.txt') as channel:
  for line in channel.readlines():
    parts = line.split(' _$_ ')
    id_user[parts[0]] = json.loads(parts[1])

print len(album_tags), len(id_user)

# candidateSet = set()
# removers = []

# print albums.keys()[0:5]

# c=3
# for album in albums.keys():
#   if album not in album_tags: 
#     removers.append(album)
#     continue
#   for tags in album_tags[album]:
#     # print tags, len(candidateSet)
#     candidateSet.add(tags)


# print 'Candidate Set Size', len(candidateSet)

# for r in removers: 
#   if r in albums: del albums[r]
#   if r in album_tags: del album_tags[r]

# print len(albums), len(candidateSet), len(album_tags)

# with codecs.open('/data/facebook/tag-propagation/albums.txt', 'w') as channel:
#   for k in albums.keys():
#     channel.write(k)
#     channel.write(' _$_ ')
#     channel.write(json.dumps(albums[k], sort_keys=True))
#     channel.write('\n')


# with codecs.open('/data/facebook/tag-propagation/candidates.txt', 'w') as channel:
#   for k in candidateSet:
#     channel.write(k)
#     channel.write('\n')

# with codecs.open('/data/facebook/tag-propagation/album_tags_finals.txt', 'w') as channel:
#   for k in album_tags.keys():
#     channel.write(k)
#     channel.write(' _$_ ')
#     channel.write(json.dumps(album_tags[k], sort_keys=True))
#     channel.write('\n')


tag_albums_count = {}
for album, tags in album_tags.iteritems():
  for tag in tags:
    if tag not in tag_albums_count: tag_albums_count[tag] = 0
    tag_albums_count[tag] = tag_albums_count[tag]+1

c=0
for tag, count in tag_albums_count.iteritems():
  if count == 1: continue
  c+=1

print c, len(tag_albums_count)

with codecs.open('/data/facebook/tag-propagation/candidates_multi_occurrence.txt', 'w') as channel:
  for tag, count in tag_albums_count.iteritems():
    if count == 1: continue
    channel.write(tag)
    channel.write('\n')