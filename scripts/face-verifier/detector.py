from pymongo import Connection
import requests
import re, json, math, pickle, time
import signal, os
import common, sys

key='72b454f5b9b9fb7c83a6f7b6bfda3e59'
secret='a8f9877166d42fc73a1dda1a7d8704e5'

def get_and_print(url):
  rsp=requests.get(url)
  a=json.loads(rsp.text)
  print json.dumps(a, sort_keys=True, indent=2) + '\n'

connection = Connection('127.0.0.1', 27017)
db = connection['test']

def get_user_name (u_id):
  fb_users = db['fb_users']
  rcd = fb_users.find_one({'id': u_id})
  if rcd == None:
    print 'No record found'
  else:
    return rcd['name']
  

def get_user_id(name):
  fb_users = db['fb_users']
  name_regex = re.compile(name, re.IGNORECASE)
  rcd = fb_users.find_one({'name': name_regex})
  if rcd == None:
    print 'No record found'
  else:
    return rcd['id']
    
def get_all_user_ids():
  fb_users = db['fb_users']
  users = fb_users.find()
  result = [u['id'] for u in users]
  return result

def list_photos(user_id):
  fb_photos = db['fb_photos'];
  photos = fb_photos.find({'id': user_id})
  pics = [p for p in photos]
  return pics
  
def detect_faces (img_url):
  url='http://api.face.com/faces/detect.json?api_key='+key+'&api_secret='+secret+'&urls='+img_url
  rsp = requests.get(url)
  detection = json.loads(rsp.text)
  return detection['photos'][0]
  
def convert_to_img_coords(tag, width, height):
  tw = tag['width']*width/100
  th = tag['height']*height/100
  cx = tag['center']['x']*width/100
  cy = tag['center']['y']*height/100
  
  l = cx - (tw/2)
  r = cx + (tw/2)
  t = cy - (th/2)
  b = cy + (th/2)
  
  if (l < 0): l = 0
  if (r > width): r = width
  if (t < 0): t = 0
  if (b > height): b = height

  return ( int(math.floor(l)), int(math.floor(t)), 
           int(math.ceil(r)), int(math.ceil(b)))

training_stats = [];

count=0;

def detection_results_count():
  detection_results = db['face_detection_results']
  return detection_results.count()

def save_tag(uid, tid, img_url):
  url='http://api.face.com/tags/save.json?api_key=' + key + '&api_secret=' + secret + '&uid=' \
      + uid + '&tids=' + tid + '&urls=' + img_url
  rsp = requests.get(url)
  rsp = json.loads(rsp.text)
  if (rsp['status'] != "success"): 
    print rsp

def find_detection_result(url):
  detection_results = db['face_detection_results']
  rcd = detection_results.find_one({'url': url})
  if rcd == None:
    return None;
  else: return rcd

def save_detection_results(result):
  if ('error_code' in result):
    print '\n\n\n', result['error_message'], '\n\n'
    sys.exit()

  detection_results = db['face_detection_results']
  rcd = detection_results.find_one({'url': result['url']})
  if rcd == None:
    result['tx_time'] = int(time.time()*1000)
    detection_results.insert(result)


def match_faces(test_pic): 
  global uindex
  
  url = test_pic['photo']['source']
  
  #detection_results = find_detection_result(url)
  #if detection_results == None:
  #  detection_results = detect_faces(url)
  #  save_detection_results(detection_results)
  # print 'Faces: ', len(detection_results['tags'])

  detection_results = detect_faces(url)
  
  width = detection_results['width']
  height = detection_results['height']

  det_faces = [convert_to_img_coords(t, width, height) for t in detection_results['tags']]

  global count, user_id
  
  pos = 0
  for tag in test_pic['photo']['tags']['data']:
    tx = int (float(tag['x']) * width / 100)
    ty = int (float(tag['y']) * height / 100)
    ix = common.is_point_contained(det_faces, tx, ty)
    
    if ix >= 0 and tag['id'] in uindex :
      face_user = 'fb_' + tag['id'] + '@wicknicks';
      # print 'Found', face_user, 'at', ix, 'tid:', detection_results['tags'][ix]['tid']
      save_tag(face_user, detection_results['tags'][ix]['tid'], url);
    pos += 1
    if (tag['id'] == user_id and ix != -1): count = count+1
  
  
def main():

  user_ids = get_all_user_ids()
  prior_result_count = detection_results_count()
  uindex = {}
  for user_id in user_ids:
    uindex[user_id] = True

  ix = 0
  for user_id in user_ids:
  #for i in range(0, 10):
  #  user_id = user_ids[i]
    print 'Tagging user:', user_id, str(ix+1) + '/'  + str(len(user_ids))
    ix += 1
    #if ix < 196: continue;
    if ix < 374: continue;
    stats = {}
    count = 0
    stats['id'] = user_id
    stats['name'] = get_user_name(user_id)
    pics = list_photos(user_id)
    # prior_result_count = prior_result_count - (41 if len(pics) > 40 else len(pics))
    # if (prior_result_count <= 0):
    for p in pics: 
      match_faces(p)
      if (count > 40): break
      time.sleep(.1)
    
    stats['count'] = count
    training_stats.append(stats)
    print stats

  """
  print training_stats

  pkl = open('train.stats', 'w')
  pickle.dump(training_stats, pkl)
  """

if __name__ == "__main__": main()
