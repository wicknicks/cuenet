import json, requests, sys
import detector, tagger

f = open('/data/mugshots/trained.ix', 'r')
cache = {}
for t in f.readlines():
  t = t.strip();
  parts = t.split()
  cache[parts[0]] = parts[1]
  
f.close()

uid = sys.argv[1].split('/')
uid = uid[len(uid)-1]
uid = uid[0:len(uid)-5] + '@wicknicks'

if uid in cache: 
  print uid, 'has already been trained'
  print 'Exiting'
  sys.exit()

print 'Tagging', uid

if len(sys.argv) < 2: sys.exit()
links = open(sys.argv[1], 'r')

count = 0
for img in links.readlines():
  img = img.strip();
  detection = detector.detect_faces(img)
  
  if 'tags' not in detection: 
    print detection
    sys.exit()
  
  if len(detection['tags']) != 1:
    print "# of tags != ", img, len(detection['tags'])
    print json.dumps(detection, sort_keys = True, indent = 2)
    print 
    sys.exit()
    
  tag = detection['tags'][0]['tid']
  rsp = tagger.save_tag(uid, tag, img)
  count += 1

tagger.train_uid(uid)

f=open('/data/mugshots/trained.ix', 'a')
f.seek(0,2)
f.write(uid + ' ' + str(count))
f.write('\n')
f.close()
