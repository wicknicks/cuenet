import xml.etree.ElementTree as ET
from rtree import index
import random

def add_node(node):
  nodes[node.attrib['id']] = node

def bound(child):
  bounds = {}
  bounds['maxlat'] = child.attrib['maxlat']
  bounds['maxlon'] = child.attrib['maxlon']
  bounds['minlat'] = child.attrib['minlat']
  bounds['minlon'] = child.attrib['minlon']
  return bounds

def write_location_list(osmfile):
  print 'Loading...', osmfile

  mydoc = ET.parse(osmfile)
  root = mydoc.getroot()

  nodes = {}
  bounds = None

  for child in root:
    if child.tag == 'node': add_node(child)
    if child.tag == 'bounds': bounds = bound(child)

  print 'From OSM:', len(nodes)

  with open(osmfile + '.locations', 'w') as locfile:
    print str(bounds)
    locfile.write(str(bounds) + '\n')
    for node in nodes.values():
      locfile.write(','.join([node.attrib['id'], node.attrib['lat'], node.attrib['lon']]))
      locfile.write('\n')

def sample_locations(locfile):
  locs = open(locfile)
  l = locs.readline() # read bounds line
  
  idx = index.Index()
  locations = {}
  
  count = 0
  for line in locs.readlines():
    parts = line.split(',')
    idx.insert(long(parts[0]), (float(parts[1]), float(parts[2])))
    locations[parts[0]] = 0
  
  bounds = idx.get_bounds()
  print bounds
  print idx.count(idx.get_bounds())

  latmu = (bounds[0] + bounds[2]) / 2
  lonmu = (bounds[1] + bounds[3]) / 2
  LIM = 10
  while LIM > 0:
    lat = random.gauss(latmu, 0.2)
    lon = random.gauss(lonmu, 0.2)
    near = idx.nearest( (lat, lon), 1).next()
    print lat, lon, near
    LIM -= 1

if __name__ == '__main__':
  # write_location_list('/data/osm/karnataka.highway.osm')
  sample_locations('/data/osm/uci.osm.locations')
  