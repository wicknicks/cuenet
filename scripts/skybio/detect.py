import tagger, helper
import os, Image, sys, json
from time import time, sleep

HTTP_DIR = 'http://www.ics.uci.edu/~arjun/skybio/'
LOC_DIR = '/home/arjun/Dataset/ramesh/ramesh-mm12-nara/'

pics = os.listdir(LOC_DIR)

ofile = open('detection_results.js', 'w');
ofile.write('var detections = [')

for FILE in pics:
  
  url = os.path.join(HTTP_DIR, FILE)
  #detection = tagger.detect_faces( url );
  #print count, 'Tag count for', FILE, tagger.tag_count(detection)
  
  #tagger.save_detection_result(url, detection)

  #image = Image.open( os.path.join(LOC_DIR, FILE) );
  #helper.draw_tags(image, detection['photos'][0])
  
  res = tagger.find_detection_result(url)
  if res == None:
    sys.exit()
  else:
    del res['_id']
    res['url'] = res['url'].replace('/skybio/', '/mm12/pics/')
    ofile.write(json.dumps(res))
    ofile.write('\n\n,\n\n')

ofile.write('];')
ofile.close()



"""
LOC_DIR = '/home/arjun/Dataset/ramesh/ramesh-mm12-nara/'
ANN_DIR = '/home/arjun/Sandbox/python/renderface/annotations'

files = os.listdir(LOC_DIR)
files.sort()

for FILE in files[301:]:
  image = Image.open( os.path.join(LOC_DIR, FILE) );
  rects = []

  for line in open( os.path.join(ANN_DIR, FILE + '.annotations') ):
    rects.append( eval(line) )
    
  if len(rects) == 0: continue

  helper.draw_rects(image, rects)
"""
