import networkx as nx
import pygraphviz as pgv
from PIL import Image

maze=nx.sedgewick_maze_graph()

G=pgv.AGraph(strict=False,directed=True)

G.add_nodes_from(maze.nodes())
G.add_edges_from(maze.edges())

G.layout(prog='dot')
G.draw('file.png')

Image.open('file.png').show()