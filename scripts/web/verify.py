import requests
import json
from optparse import OptionParser

key='72b454f5b9b9fb7c83a6f7b6bfda3e59'
secret='a8f9877166d42fc73a1dda1a7d8704e5'

def recognize (img_url, uid):
  url = 'http://api.face.com/faces/recognize.json?api_key=' + key + '&api_secret=' + secret + \
        '&urls=' + img_url + '&uids=' + uid
  
  rsp = requests.get(url)
  
  verification = json.loads(rsp.text)
  return verification['photos'][0]

parser = OptionParser()
args = parser.parse_args()[1]
v = recognize(args[0], args[1])

print json.dumps(v)
