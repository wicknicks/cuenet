from pymongo import Connection
import sys

DBNAME = 'jain'
if len(sys.argv) > 1:
  DBNAME = sys.argv[1] 
  print DBNAME

TOT = 30
if len(sys.argv) == 3:
  TOT = int(sys.argv[2])
  print TOT
  
connection = Connection('128.195.54.27', 27017)
db = connection[DBNAME]
photos = db['fb_photos']

def start():
  users = {}
  tagIndex = {}
  seen = {}
  
  writer = open("".join([DBNAME,'.pix','.txt']), 'w')
  
  taglessPhotos = 0
  incompleteTagsCount = 0
  c = 0
  
  for photo in photos.find():
    if photo['id'] in seen: continue
    seen[photo['id']] = True
    
    if 'tags' not in photo: 
      taglessPhotos += 1
      continue
    if 'data' not in photo['tags']: 
      taglessPhotos += 1
      continue
    
    for tag in photo['tags']['data']:
      if ('id' not in tag) or ('name' not in tag):
        incompleteTagsCount += 1
        continue;
        
      users[tag['id']] = tag['name']
      if tag['id'] not in tagIndex:
        tagIndex[tag['id']] = []
      tagIndex[tag['id']].append('{"url": "' + photo['source'] + '", "output": "/data/fbphotos/' + photo['id'] + '"}')
    
    #c += 1
    #if (c % 1000 == 0): print c, 'photos read'
    
  print 'Results from', DBNAME
  
  c = 0
  
  for entry in tagIndex:
    if len(tagIndex[entry]) >= TOT:
      writer.write("\n".join(tagIndex[entry][0:TOT]))
      writer.write('\n')
      c += 1
  
  print 'People with more than', TOT, 'photos:', c
  print taglessPhotos, incompleteTagsCount

if __name__ == "__main__":
  start()

