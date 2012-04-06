import requests
import json

key='72b454f5b9b9fb7c83a6f7b6bfda3e59'
secret='a8f9877166d42fc73a1dda1a7d8704e5'


def get_and_print(url):
  rsp=requests.get(url)
  a=json.loads(rsp.text)
  print json.dumps(a, sort_keys=True, indent=2) + '\n'
  return a

def get_limits():
  url='http://api.face.com/account/limits.json?api_key='+key+'&api_secret='+secret
  get_and_print(url)


## Users

def get_ns():
  url = 'http://api.face.com/account/users.json?api_key='+key+'&api_secret='+secret+'&namespaces=wicknicks'
  get_and_print(url);

Ramesh = 'http://tracker.ics.uci.edu/pics/52d0f1d6ee05fff26b1f6edbb7040f24.640'
Michael = 'http://tracker.ics.uci.edu/pics/8fce0ed597e1a3c9fdf156b201bd7ac0.640'
Nick = 'http://tracker.ics.uci.edu/pics/c88dabb2e160fc4b6c369d679cdd2efa.640'
FB_URL='http://a3.sphotos.ak.fbcdn.net/hphotos-ak-ash4/s720x720/310767_2299459198546_1011126280_32036493_2136624880_n.jpg'

## Face Detection Output

def detect():
  url='http://api.face.com/faces/detect.json?api_key='+key+'&api_secret='+secret+'&urls='+FB_URL
  get_and_print(url)

## Face Recognition Output

def recognize():
  url='http://api.face.com/faces/recognize.json?api_key='+key+'&api_secret='+secret+ \
             '&urls='+Ramesh+'&uids=RameshJain@arjun.satish'
  get_and_print(url)


get_ns()
