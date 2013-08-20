import random, os, sys
import networkx as nx
import pygraphviz as pgv
from PIL import Image
import json

# 
# Implementation of ontological distance for a given subsumption (is-A) hierarchy. 
# Theoretical details in paper:
# 
# @inproceedings{ranwez2006ontological,
#   title={Ontological distance measures for information visualisation on conceptual maps},
#   author={Ranwez, Sylvie and Ranwez, Vincent and Villerd, Jean and Crampes, Michel},
#   booktitle={On the Move to Meaningful Internet Systems 2006: OTM 2006 Workshops},
#   pages={1050--1061},
#   year={2006},
#   organization={Springer}
# }
# 

def draw(z):
  G=pgv.AGraph(strict=False,directed=True)
  G.add_nodes_from(z.nodes())
  G.add_edges_from(z.edges())
  G.layout(prog='dot')
  G.draw('___file.png')
  Image.open('___file.png').show()
  os.remove('___file.png')


def desc(G, node):
  descendants = set()
  descendants.add(node)

  level = [node]
  while len(level) > 0:
    newlevel = []
    for k in level:
        for d in dict.keys(G[k]):
          descendants.add(d)
          newlevel.append(d)
    level = newlevel
  return descendants 

def ancestor(G, node):
  ancestors = set()
  level = [node]
  while len(level) > 0:
    newlevel = []
    for k in level:
      preds = G.predecessors(k)
      for p in preds: ancestors.add(p)
      newlevel.extend(preds)
    level = newlevel
  return ancestors

def exAncestor(G, n1, n2):
  anc1 = ancestor(G, n1)
  anc2 = ancestor(G, n2)
  return anc1.union(anc2) - anc1.intersection(anc2)

def isA_distance(G, n1, n2):
  ancEx = exAncestor(G, n1, n2)
  exclusiveAncestors = set()
  for i in ancEx: 
    for d in desc(G, i):  exclusiveAncestors.add( d )
  union = exclusiveAncestors.union(desc(G, n1)).union(desc(G, n2))
  # print union

  inter = desc(G, n1).intersection(desc(G, n2))
  # print inter

  return union - inter

def compute_matrix(G):
  matrix = {}
  for n1 in G.nodes():
    matrix[n1] = {}
    for n2 in G.nodes():
      matrix[n1][n2] = len(isA_distance(G, n1, n2))
  return matrix


def load_from_file(filename):
  return nx.read_edgelist(filename, nodetype=int, create_using=nx.DiGraph())

if __name__ == '__main__':
  # a = nx.DiGraph()
  # a.add_nodes_from(['T', 's', 'ss', 'c', 'a', 'x', 'y', 'z', 'b'])
  # a.add_edge('T', 's' )
  # a.add_edge('T', 'ss')
  # a.add_edge('T', 'c')
  # a.add_edge('s', 'a')
  # a.add_edge('s', 'x')
  # a.add_edge('s', 'y')
  # a.add_edge('ss', 'y')
  # a.add_edge('ss', 'b')
  # a.add_edge('x', 'z')
  # a.add_edge('y', 'z')
  # draw(a)

  # print desc(a, 'ss')
  # print ancestor(a, 'y')
  # print exAncestor(a, 'x', 'y')
  # print isA_distance(a, 'x', 'y'), len(isA_distance(a, 'x', 'y'))
  # print isA_distance(a, 'T', 'b'), len(isA_distance(a, 'T', 'b'))

  # matrix = compute_matrix(a)
  # print matrix['T']

  a = load_from_file('/data/ranker/ontology_cuenet.txt')
  draw(a) 
  matrix = compute_matrix(a)
  print matrix[1]

  with open('/data/ranker/ontology_cuenet.distances.txt', 'w') as dfile:
    for key in matrix.keys():
      s = json.dumps(matrix[key])
      dfile.write(str(key) + " -> " + s + "\n")
