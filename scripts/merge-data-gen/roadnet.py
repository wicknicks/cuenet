import xml.etree.ElementTree as ET
from collections import deque

def add_node(node):
  nodes[node.attrib['id']] = node

def add_node_map(noderef, edgeid):
  if noderef.attrib['ref'] not in nodemap:
    nodemap[noderef.attrib['ref']] = []
  nodemap[noderef.attrib['ref']].append(edgeid)

def add_edge(edge):
  found = False
  for child in edge:
    if child.tag == 'tag':
      if child.attrib['k'] == 'highway':
        found = True
  if found == False: return

  edges[edge.attrib['id']] = edge
  for child in edge:
    if child.tag == 'nd':
      add_node_map(child, edge.attrib['id'])

def get_node_refs(edge):
  refs = []
  for child in edge:
    if child.tag == 'nd':
      refs.append(child.attrib['ref'])
  return refs

def combine(nodelimit = 50000):
  queue = deque()
  network = []
  
  edge_id = edges.keys()[0]
  print(edge_id)
  for ref in get_node_refs(edges[edge_id]): queue.append(ref)
  network.append(edge_id)
  del edges[edge_id]
  
  lim = len(queue)

  while len(queue) > 0:
    if lim > nodelimit: break
    node = queue.popleft()
    if node in nodemap:
      for edge_id in nodemap[node]: #all connecting edges from this node
        if edge_id not in edges: continue #we have seen it before
        for ref in get_node_refs(edges[edge_id]): 
          queue.append(ref)
          if ref in nodes: lim += 1
        network.append(edge_id)
        del edges[edge_id]

  return network

def prep_kml(network, ofile):
  ofile.write('<?xml version="1.0" encoding="UTF-8"?>\n')
  ofile.write('<kml xmlns="http://earth.google.com/kml/2.0"> <Document>\n')
  
  edgec = len(network)
  nodec = 0
  
  for edge_id in network:
    ofile.write('<Placemark>\n')
    ofile.write(' <LineString>\n')
    ofile.write('  <coordinates>\n')
    for ref in get_node_refs(edges[edge_id]):
      if ref not in nodes: continue
      ofile.write(nodes[ref].attrib['lon'] + "," + nodes[ref].attrib['lat'] + ",0\n")
      nodec += 1

    ofile.write('  </coordinates>\n')
    ofile.write(' </LineString>\n')
    ofile.write('</Placemark>\n')

  ofile.write('</Document> </kml>')

  print('Network places: ', nodec)
  ofile.close()

def prep_file_dump(network, ofile):
  edgec = len(network)
  nodec = 0
  illegal_edges = {}
  for edge_id in network:
    for ref in get_node_refs(edges[edge_id]):
      if ref not in nodes: 
        illegal_edges[edge_id] = True
        continue
      nodec += 1
  ofile.write(str(nodec) + "," + str(edgec) + '\n')

  # Write nodes
  for edge_id in network:
    for ref in get_node_refs(edges[edge_id]):
      if ref not in nodes: continue
      ofile.write(nodes[ref].attrib['id'] + "," + nodes[ref].attrib['lon'] + "," + nodes[ref].attrib['lat'] + "\n")

  # Write edges
  for edge_id in network:
    if edge_id not in illegal_edges:
      ofile.write(edge_id + ' -> ')
      for ref in get_node_refs(edges[edge_id]):
        ofile.write(nodes[ref].attrib['id'] + ' ')
      ofile.write('\n')


  ofile.close()

if __name__ == "__main__":

  filx = '/data/osm/uci.osm'
  print 'Loading...', filx
  mydoc = ET.parse(filx)
  root = mydoc.getroot()

  nodes = {}
  edges = {}
  nodemap = {} #map nodes to edges

  for child in root:
    if child.tag == 'node': add_node(child)
    if child.tag == 'way' : add_edge(child)

  print ('From OSM: ', len(nodes), len(edges), len(nodemap))

  edge_bk = dict(edges)
  net = combine(500)
  edges = edge_bk

  prep_file_dump(net, open('/data/osm/uci.roadnet', 'w'))
  # prep_kml(net, open('/data/osm/karnataka.kml', 'w'))

  # c=len(mydoc.findall("./way/tag[@v='traffic_signals']"))
  # d=len(mydoc.findall("./way/tag[@k='name']"))
  # print (c, 'traffic', d, 'named')