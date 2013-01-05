import tagger, helper
import os, Image, sys, json
from time import time, sleep

HTTP_DIR = "http://www.ics.uci.edu/~arjun/mm12/train/"
LOC_DIR = '/home/arjun/Desktop'
pics = ['g3.jpg']

for pic in pics:
  url = os.path.join(HTTP_DIR, pic)
  detection = tagger.detect_faces(url)
  print detection['photos'][0]['tags'][0]['tid']

  image = Image.open( os.path.join(LOC_DIR, pic) );
  helper.draw_tags(image, detection['photos'][0])

  tagrsp = tagger.save_tag('fb_1666306302@wicknicks', detection['photos'][0]['tags'][0]['tid'], url)
  print tagrsp, '\n\n\n'


# Ramesh: fb_6028816@wicknicks
# Gerald: fb_1666306302@wicknicks
