import requests, json
from pymongo import Connection

connection = Connection('127.0.0.1', 27017)
db = connection['skybio']

conf = json.load(open('conf.js', 'r'))
key = conf['key']
secret = conf['secret']

def detect_faces (img_url):
  detection = find_detection_result(img_url)
  if detection != None: 
    return detection
    
  url='http://api.skybiometry.com/fc/faces/detect.json?api_key=' + \
       key + '&api_secret=' + secret + '&urls=' + img_url
  rsp = requests.get(url)
  detection = json.loads(rsp.text)
  if ('error_code' in detection):
    print '\n\n\n', img_url , ':', detection['error_message'], '\n\n'

  return detection
  
def tag_count (detection):
  c = 0
  for p in detection['photos']:
    if 'tags' not in p: continue
    c += len(p['tags'])
  return c
  
def save_tag(uid, tid, img_url):
  url='http://api.skybiometry.com/fc/tags/save.json?api_key=' + \
       key + '&api_secret=' + secret + '&uid=' \
      + uid + '&tids=' + tid + '&urls=' + img_url
  rsp = requests.get(url)
  rsp = json.loads(rsp.text)
  if rsp['status'] != "success": print rsp
  return rsp
  
def recognize(uids, img_url)
  url='http://api.skybiometry.com/fc/tags/faces/recognize.json?api_key=' + \
       key + '&api_secret=' + secret + 
       '&urls=' + img_url + 'uids=' + uids
  rsp = requests.get(url)
  rsp = json.loads(rsp.text)
  if rsp['status'] != "success": print rsp
  return rsp
  
def find_detection_result(url):
  detection_results = db['face_detection_results']
  rcd = detection_results.find_one({'url': url})
  if rcd == None:
    return None;
  else: return rcd

def save_detection_result(url, result):
  if ('error_code' in result):
    print '\n\n\n', result['error_message'], '\n\n'

  detection_results = db['face_detection_results']
  rcd = detection_results.find_one({'url': url})
  if rcd == None:
    result['url'] = url  #add identifier
    detection_results.insert(result)

