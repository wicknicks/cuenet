import requests, json

conf = json.load(open('skybio.js', 'r'))
key = conf['key']
secret = conf['secret']

def detect_faces (img_url):
  url='http://api.skybiometry.com/fc/detect.json?api_key='+key+'&api_secret='+secret+'&urls='+img_url
  rsp = requests.get(url)
  detection = json.loads(rsp.text)
  return detection['photos'][0]
  
def save_tag(uid, tid, img_url):
  url='http://api.skybiometry.com/fc/save.json?api_key=' + key + '&api_secret=' + secret + '&uid=' \
      + uid + '&tids=' + tid + '&urls=' + img_url
  rsp = requests.get(url)
  rsp = json.loads(rsp.text)
  if (rsp['status'] != "success"): 
    print rsp
  return rsp
