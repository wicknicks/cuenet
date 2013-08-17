import random, os, sys
import networkx as nx
import pygraphviz as pgv
from PIL import Image

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


def isA_distance(G):
  return 0

def load_from_file(filename):
  return nx.read_edgelist(filename, nodetype=int, create_using=nx.DiGraph())

if __name__ == '__main__':
  a = nx.DiGraph()
  a.add_nodes_from(['T', 's', 'ss', 'c', 'a', 'x', 'y', 'z', 'b'])
  a.add_edge('T', 's' )
  a.add_edge('T', 'ss')
  a.add_edge('T', 'c')
  a.add_edge('s', 'a')
  a.add_edge('s', 'x')
  a.add_edge('s',' y')
  a.add_edge('ss', 'y')
  a.add_edge('ss', 'b')
  a.add_edge('x', 'z')
  a.add_edge('y', 'z')
  draw(a)

  b = load_from_file('/data/ranker/ontology_edgelist.10.txt')
  draw(b)