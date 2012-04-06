from pymongo import Connection
import requests
import re, json, math, pickle, time
import signal, os, sys
import common

connection = Connection('127.0.0.1', 27017)
db = connection['test']

key='72b454f5b9b9fb7c83a6f7b6bfda3e59'
secret='a8f9877166d42fc73a1dda1a7d8704e5'


photos = None
detections = None
dindex = None
uindex = {}
all_user_ids = None

def get_and_print(url):
  rsp=requests.get(url)
  a=json.loads(rsp.text)
  print json.dumps(a, sort_keys=True, indent=2) + '\n'

def list_photos(user_id):
  fb_photos = db['fb_photos'];
  photos = fb_photos.find({'id': user_id})
  pics = [p for p in photos]
  return pics
  
  
def load_photos():
  global all_user_ids
  
  fb_users = db['fb_users']
  name_regex = re.compile(".*satish", re.IGNORECASE)
  users = fb_users.find()
  all_photos = [ ]
  all_user_ids = [ ]
  for user in users:
    user_id = user['id']
    print user['name']
    all_user_ids.append(user_id)
    for p in list_photos(user_id):
      all_photos.append(p)

  return all_photos
  
def load_detection_results():
  detection_results = db['face_detection_results']
  cursor = detection_results.find()
  results = [x for x in cursor]
  return results
  
def build_index(attribute):
  dindex = {}
  for detection in detections:
    dindex[detection[attribute]] = detection
  return dindex

def save_tag(uid, tid, img_url):
  url='http://api.face.com/tags/save.json?api_key=' + key + '&api_secret=' + secret + '&uid=' \
      + uid + '&tids=' + tid + '&urls=' + img_url
  rsp = requests.get(url)
  rsp = json.loads(rsp.text)
  if (rsp['status'] != "success"): 
    print rsp
    
  
def save_pid (tag_save_response):
  pid_refs = db['face_pid_refs']
  pid = {}
  pid['url'] = tag_save_response['photos'][0]['url']
  pid['pid'] = tag_save_response['photos'][0]['pid']

  rcd = pid_refs.find_one({'pid': pid['pid']})
  if rcd == None: 
    pid_refs.insert(pid)
  else: 
    return rcd
  return pid

def load():
  global photos, detections, dindex, all_user_ids, unidex
  
  print 'Loading data....'

  photos = load_photos()
  print 'Number of photos:',  len(photos)
  
  for user_id in all_user_ids:
    uindex[user_id] = True

  detections = load_detection_results()
  print 'Number of detection results:',  len(detections)

  dindex = build_index('url')
  print 'Built index'

ignored = 0

def tag(photo):

  global dindex, uindex, ignored

  url = photo['photo']['source']
  
  if url in dindex:
    detection = dindex[url]
  else: 
    return
    
  detection = dindex[url]
  pos = 0
  for tag in photo['photo']['tags']['data']:
    width = int(photo['photo']['width'])
    height = int(photo['photo']['height'])

    tx = int (float(tag['x']) * width / 100)
    ty = int (float(tag['y']) * height / 100)
    
    det_faces = [common.convert_to_img_coords(t, width, height) for t in detection['tags']]
    ix = common.is_point_contained(det_faces, tx, ty)
    
    if (not(tag['id'] in uindex)) :
      ignored += 1
    
    if ix >= 0 and tag['id'] in uindex :
      face_user = 'fb_' + tag['id'] + '@wicknicks';
      # print 'Found', face_user, 'at', ix, 'tid:', detection['tags'][ix]['tid']
      save_tag(face_user, detection['tags'][ix]['tid'], url);
      time.sleep(0.5)
    #else:
    #  print 'No takers for', pos
    pos += 1
    
    
def train(user_id):  
  url = 'http://api.face.com/faces/train.json?api_key=' + key + '&api_secret=' + secret + \
        '&uids=fb_' + str(user_id) + '@wicknicks' + '&callback_url=http://tracker.ics.uci.edu'
  rsp = requests.get(url)
  print rsp.text
  

load()


p=0
for photo in photos:
  tag(photo)
  # print '-----------------'
  if (p % 25 == 0): print 'p:', p
  if (p > 2500): sys.exit()
  p += 1

print p, ignored

"""
for uid in all_user_ids: 
  print 'Training:', uid
  train(uid);
  time.sleep(3)
  
"""
