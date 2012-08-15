from pymongo import Connection
import sys

DBNAME = 'jain'

connection = Connection('128.195.54.27', 27017)
db = connection[DBNAME]
collection = db['fb_photos']

def writeTags(tags, msg):
  taggedNames = []
  for tag in tags:
    if 'name' in tag and 'id' in tag:
      taggedNames.append( (tag['name'], tag ['id']) )
    elif 'name' in tag:
      taggedNames.append( (tag['name']) )
    else:
      print 'Tags not found in ', msg['id']
  msg['tags'] = taggedNames
  return 0

def writeComments(comments, msg):
  commentNames = []
  for comment in comments:
    if 'from' in comment:
      if 'id' in comment['from'] and 'name' in comment['from']:
        commentNames.append((comment['from']['id'], comment['from']['name']))
      elif 'name' in comment['from']:
        commentNames.append((comment['from']['name']))
      else:
        print 'Did not find required tags', msg['id']
  msg['comments'] = commentNames
  return 0

def findNames(photo):
  """ a comment """
  msg = {'id': photo['id']};
  if 'tags' in photo:
    if 'data' in photo['tags']:
      tags = photo['tags']['data']
      writeTags(tags, msg);
  if 'comments' in photo:
    if 'data' in photo['comments']:
      writeComments(photo['comments']['data'], msg)
  print msg

count=0;
for photo in collection.find():
  try:
    findNames(photo);
  except:
    print 'Exception ---->', photo['id']
  count += 1
  #if count == 100: sys.exit()
