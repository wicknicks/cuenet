import ImageDraw
from pymongo import Connection

def get_uids(verify_rsp):
  a = []
  for photo in verify_rsp['photos']:
    if 'tags' not in photo: continue
    for tag in photo['tags']:
      if 'uids' not in tag: continue
      c = []
      for uid in tag['uids']: 
        c.append( (uid['uid'], uid['confidence']) )
      a.append(c)
  return a

def draw_tags(img, data):
  draw = ImageDraw.Draw(img)
  wScale = data['width'] / 100.0
  hScale = data['height'] / 100.0

  for tag in data['tags']:
      
    fWidth = tag['width'] * wScale
    fHeight = tag['height'] * hScale

    cx = tag['center']['x'] * wScale
    cy = tag['center']['y'] * hScale

    left = cx - fWidth / 2
    right = cx + fWidth / 2

    top = cy - fHeight / 2
    bottom = cy + fHeight / 2

    draw.rectangle([left, top, left+fWidth, top+fHeight], fill=None, outline="red")  
    
  del draw 
  img.show();


def draw_rects(img, rects):
  draw = ImageDraw.Draw(img)
  
  for r in rects:
    draw.rectangle(r, outline='red')

  img.show()


