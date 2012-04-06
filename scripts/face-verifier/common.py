import math

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

def is_point_contained(det_faces, tx, ty):
  i=0;
  for face in det_faces:
    if (face[0] < tx and tx < face[2] and face[1] < ty  and ty < face[3]): 
       return i;
    i = i+1;
  return -1;
