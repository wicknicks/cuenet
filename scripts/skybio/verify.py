import tagger, helper
import os, Image, sys, json
from time import time, sleep

HTTP_DIR = 'http://www.ics.uci.edu/~arjun/mm12/pics/'
LOC_DIR = '/home/arjun/Dataset/ramesh/ramesh-mm12-nara/'

pics = ['DSC05577.JPG']
uids = ['fb_6028816@wicknicks', 'fb_1666306302@wicknicks']

for pic in pics:
  url = os.path.join (HTTP_DIR, pic)
  
  #url = 'http://farm1.staticflickr.com/228/513216118_47e1fd1606_b.jpg'
  print 'Tagging', url
  rsp = tagger.recognize(",".join(uids), url)
  print '\n', rsp, '\n\n'
  
  for p in helper.get_uids(rsp): print p
