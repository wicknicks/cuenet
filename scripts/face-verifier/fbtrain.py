import json, requests, sys
import detector, tagger, common
from pymongo import Connection

connection = Connection('127.0.0.1', 27017)
db = connection['setareh']

def load_users():
  collection = db['fb_users']
  users = collection.find()
  return [(lambda p: {'id':p['id'],'name':p['name']})(u) for u in users]

def load_photos(user_id):
  fb_photos = db['fb_photos']
  photos = fb_photos.find({'tags.data.id': user_id})
  return [p for p in photos]

def detect(photo):
  detection = detector.detect_faces(photo['source'])
  if 'tags' not in detection:
    print detection, '\nExiting!'
    return None #sys.exit()
  return detection

def match_faces(photo, user_id):
  url = photo['source']
  rt = False

  dresults = detect(photo)
  if dresults == None: return rt;
  
  width = dresults['width']
  height = dresults['height']

  det_faces = [detector.convert_to_img_coords(t, width, height) for t in dresults['tags']]
  for tag in photo['tags']['data']:
    tx = int ( float(tag['x']) * width / 100 )
    ty = int ( float(tag['y']) * height / 100 )
    ix = common.is_point_contained(det_faces, tx, ty)
    #if ix < 0:
    #  print 'Did not find user', tag['name'] ,'in', photo['id']
    if ix >= 0 and tag['id'] == user_id:
      face_user = 'fb_' + tag['id'] + '@setoreh'
      detector.save_tag(face_user, dresults['tags'][ix]['tid'], url);
      #print 'Saving ', face_user, url
      rt = rt or True

  return rt

def train(user):
  pics = load_photos(user['id'])
  count = 0
  for pic in pics:
    try:
      rt = match_faces(pic, user['id'])
    except KeyError as ke:
      print 'KeyError', ke, 'in', pic['source']
    else:
      if rt:
        count += 1
        if count % 10 == 0: print count, 'pics added for', user['name']
    if count >= 40:
        print 'Reached 40 pics for ', user['name']
        break

  if count == 0:
    rsp = {'success': 'no_training_set'}
    return rsp;

  rsp = tagger.train_uid(user['id']+'@setoreh')
  rsp = json.loads(rsp.text)
  return rsp

def save_training_results(training_rsp, user):
  collection = db['training']
  training_rsp['fb_id'] = user['id']
  training_rsp['fb_name'] = user['name']
  collection.save(training_rsp)

def main():
  total = 0
  users = load_users()
  for ix in xrange(302, len(users)):
    print ix, users[ix]
    rsp = train(users[ix])
    print 'Training', users[ix]['name'], rsp
    save_training_results(rsp, users[ix])

if __name__ == "__main__":
  main()
