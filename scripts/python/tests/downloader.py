import requests
import sys, json, os
import time

FILE = 'first.txt'
if len(sys.argv) == 2:
  FILE = sys.argv[1]

if __name__ == "__main__":
  _file = open(FILE, 'r')
  start = int(time.time())
  c = 0
  for line in _file.readlines():
    c += 1
    if c % 100 == 0: print c, 'photos downloaded in', int(time.time())-start, 'seconds'
    obj = json.loads(line)
    if os.path.exists(obj['output']): continue
    r = requests.get(obj['url'])
    _writer = open(obj['output'], 'w')
    _writer.write(r.content)
    _writer.close()
    time.sleep(0.1);
