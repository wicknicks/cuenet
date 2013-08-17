import random, os, sys
import networkx as nx
import pygraphviz as pgv
from PIL import Image

def grandparent(graph, node):
  grandpas = []
  preds = graph.predecessors(node)
  for pred in preds:
    grandpas.extend(graph.predecessors(pred))
  return grandpas

def create_subsumption_graph(nodecount=10):
  nodes = range(1, nodecount+1)

  ontology = nx.DiGraph()
  n = nodes[random.randint(0, len(nodes)-1)]
  nodes.remove(n)
  print n
  ontology.add_node(n)

  #subsumption tree
  while len(nodes) > 0:
    n = nodes[random.randint(0, len(nodes)-1)]
    nodes.remove(n)
    pnodes = ontology.nodes()
    parent = pnodes[random.randint(0, len(pnodes)-1)]
    ontology.add_edge(parent, n)

  nodes = range(1, nodecount+1)
  multi_parent_nodecount = max(1, int(0.2 * nodecount))
  multi_parent_nodes = []
  print multi_parent_nodecount
  i = multi_parent_nodecount
  while i > 0:
    n = nodes[random.randint(1, len(nodes)-1)]
    nodes.remove(n)
    if len(grandparent(ontology, n)) == 0:
      continue
    multi_parent_nodes.append( n )
    i = i - 1

  for p in multi_parent_nodes:
    gp = grandparent(ontology, p)[0]
    parents = dict.keys(ontology[gp])
    parents.remove(ontology.predecessors(p)[0])
    if len(parents) < 1: continue
    cparent = random.randint(0, len(parents)-1)
    print p, parents[cparent], ontology.predecessors(p)
    ontology.add_edge(parents[cparent], p)

  print 'cycles', len(list(nx.simple_cycles(ontology)))
  return ontology


def populate_hierarchy(sg, nodes, fanout, depth):
  level = sg.nodes()
  while depth > 0:
    newlevel = []
    for n in level:
      for f in range(fanout):
        se = nodes[random.randint(0, len(nodes)-1)]
        nodes.remove(se)
        sg.add_edge(n, se)
        newlevel.append(se)
    level = newlevel
    depth -= 1

def create_subevent_hierarchies(ontology, fanout=2, depth=2):
  nodes = range(1, max(ontology.nodes())+1)

  superevent_count = int (len(nodes) / 3)
  superevents = []
  while superevent_count > 0:
    n = nodes[random.randint(0, len(nodes)-1)]
    nodes.remove(n)
    superevents.append(n)
    superevent_count -= 1
  
  subevent_hierarchies = []

  for event in superevents:
    sg = nx.DiGraph()
    sg.add_node(event)
    subevent_hierarchies.append(sg)
    populate_hierarchy(sg, list(nodes), fanout, depth)  

  return subevent_hierarchies

def serialize_graph(G, filename):
  nx.write_edgelist(G, filename)

def serialize_instance_graph(G, stream):
  if len(G.edges()) == 0:
    for n in G.nodes():
      stream.write(str(n) + " " + str(G.node[n]['instance_id']) + "\n")

  for edge in G.edges(): 
    stream.write(str(edge[0]) + ",")
    stream.write(str(G.node[edge[0]]['instance_id']))
    stream.write(' -> ')
    stream.write(str(edge[1]) + ",")
    stream.write(str(G.node[edge[1]]['instance_id']))
    stream.write("\n")

  for n in G.nodes():
    if len(G.node[n]['entities']) == 0: continue
    stream.write(str(n) + ", [")
    stream.write(','.join(map(lambda a: str(a), G.node[n]['entities'])))
    stream.write("]\n")


def load_from_file(filename):
  return nx.read_edgelist(filename, nodetype=int, create_using=nx.DiGraph())

def draw(z):
  G=pgv.AGraph(strict=False,directed=True)
  G.add_nodes_from(z.nodes())
  G.add_edges_from(z.edges())
  G.layout(prog='dot')
  G.draw('___file.png')
  Image.open('___file.png').show()
  os.remove('___file.png')

def get_root(sgraph):
  answers = []
  for node in sgraph.nodes():
    if len(sgraph.predecessors(node)) == 0: answers.append(node)
  if len(answers) == 0: print('ANSWERS', answers)
  return answers[0]

def instantiate_subevent(subevent, instance_counts):
  root = get_root(subevent)
  I = nx.DiGraph()

  instance_counts[root] += 1
  I.add_node(root, {'instance_id': instance_counts[root], 'entities': []})

  level = [root]
  while len(level) > 0:
    newlevel = []
    for l in level:
      outgoing_nodes = subevent[l]
      for o in outgoing_nodes:
        # there is a 60% chance that a subevent will be instantiated
        if random.randint(1, 100) < 40: continue
        instance_counts[o] += 1
        newlevel.append(o)
        I.add_node(o, {'instance_id': instance_counts[o], 'entities': []})
        I.add_edge(l, o)
    level = newlevel

  return I

def create_instances(ontology, subevents):
  nodes = ontology.nodes()
  instance_counts = [0]
  for i in nodes: instance_counts.append(0)

  smap = {}
  for se in subevents: smap[get_root(se)] = se

  while True:
    n = random.randint(0, len(nodes)-1)
    if n in smap: 
      I = instantiate_subevent(smap[n], instance_counts)
    else: 
      instance_counts[n] += 1
      I = nx.DiGraph()
      I.add_node(n, {'instance_id': instance_counts[n], 'entities': []})
    
    yield I


def pepper_entities(instance, maxEntity):
  # if random.choice([True, False]) == False: break
  i = 0
  while i < len(instance.nodes()):
    if random.choice([True, False]): 
    #if random.randint(1, 100) < 25: 
      i+=1
      continue
    node = instance.nodes()[i]
    instance.node[node]['entities'].append(random.randint(1, maxEntity))
   

if __name__ == '__main__':
  NC = 10      # number of event types
  XN = 50      # maximum number of entities
  IC = 1000000 # number of instances to be generated

  # O = create_subsumption_graph(10)
  # draw(O)
  # serialize_graph(O, '/data/ranker/ontology_edgelist.txt')

  # SH = create_subevent_hierarchies(O)
  # for sg in SH: draw(sg)
  # for i in range(len(SH)): serialize_graph(SH[i], \
  #   '/data/ranker/subevent_edgelist.' + str(NC) + '.' + str(i) + '.txt')

  O = load_from_file('/data/ranker/ontology_edgelist.' + str(NC) + '.txt')
  # draw(O)

  subevents = []
  for i in range(3): 
    subevents.append(load_from_file('/data/ranker/subevent_edgelist.' + \
      str(NC) + '.' + str(i) + '.txt'))

  with open('/data/ranker/instances.' + str(NC) + '.ic.' + str(IC) + '.txt', 'w') as dest:
    instance_count = 0
    for instance in create_instances(O, subevents):
      pepper_entities(instance, XN)
      serialize_instance_graph(instance, dest)
      dest.write('=========================================\n')
      #instances.append(instance)
      instance_count += 1
      if instance_count % 50000 == 0: print instance_count
      if instance_count == IC: break

  print 'Created', IC, 'instances'
      