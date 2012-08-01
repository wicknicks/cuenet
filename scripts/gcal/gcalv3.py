import sys, httplib2, json

import redis

import gflags
from apiclient.discovery import build
from oauth2client.file import Storage
from oauth2client.client import OAuth2WebServerFlow
from oauth2client.tools import run

cache = {}

class CacheItem:
  FLOW = None
  EMAIL = None
  def __init__(self, FLOW, EMAIL):
    self.FLOW = FLOW
    self.EMAIL = EMAIL


def initRedis():
  cp = redis.ConnectionPool()
  client = redis.Redis(connection_pool = cp)
  ps = client.pubsub()
  ps.subscribe('gcal')
  return client, ps
  

def getAuthURL():

  FLAGS = gflags.FLAGS
  FLOW = OAuth2WebServerFlow(  \
    client_id='819119360720.apps.googleusercontent.com',  \
    client_secret='1J8iirUI-nEdi1RxkKNfFQHq', \
    scope='https://www.googleapis.com/auth/calendar.readonly', \
    user_agent='GCalPy-CN/0.3')
  
  FLAGS.auth_local_webserver = False
  oauthURL = OAuth2WebServerFlow.step1_get_authorize_url(FLOW, \
              redirect_uri='http://tracker.ics.uci.edu/gcal')
  
  return oauthURL, FLOW
  
def sync(FLOW, code, email):
  
  http = httplib2.Http()
  s2=OAuth2WebServerFlow.step2_exchange(FLOW, code, http=http)

  http=s2.authorize(http)

  service = build(serviceName='calendar', version='v3', http=http, \
      developerKey='AIzaSyCcT484gf2or7lsOzcOnGolRWRg4c_JJGI')
  
  calendar = service.calendars().get(calendarId='primary').execute()
  
  fp = None
  if 'id' in calendar: 
    fp = open('/data/calendars/' + calendar['id'], 'w')
  else: fp = open('/data/calendars/tmp', 'w')
  
  events=service.events().list(calendarId='primary').execute()
  
  j=0 
  while True:
    for event in events['items']: 
      j += 1
      fp.write(json.dumps(event))
      fp.write('\n')
    
    pt = events.get('nextPageToken')
    if pt: 
      events = service.events().list(calendarId = 'primary',pageToken = pt).execute()
    else: break

  print 'Wrote', j, 'events into ', '/data/calendars/' + calendar['id']
  fp.flush()

if __name__ == "__main__":
  client, pubsub = initRedis()
  while True:
    print 'Listening.... '
    msg = pubsub.listen().next()
    jdata = msg['data']
    jdata = json.loads(msg['data'])
    
    if jdata['op'] == 'auth':
      print 'Starting auth'
      authURL, flow = getAuthURL()
      cache[jdata['session']]=CacheItem(flow, jdata['email'])
      jdata['ourl']=authURL
      client.publish('gcal_results', json.dumps(jdata)) 
    elif jdata['op'] == 'sync':
      ci = cache[jdata['session']]
      print 'Syncing', ci.EMAIL, '@gmail.com'
      if ci == None:
        print 'Session not found'
      else:
        #code = raw_input("Enter code: ")
        sync(ci.FLOW, jdata['code'], ci.EMAIL)


