from twitter import *
import codecs
from datetime import datetime
import argparse, getpass

#signal handling (clean exit)
import signal
import sys

def signal_handler(signal, frame):
  print ' ... Cleaning up. Bye!'
  streamfile.flush()
  streamfile.close()
  sys.exit(0)

signal.signal(signal.SIGINT, signal_handler)

argparser = argparse.ArgumentParser()
argparser.add_argument('-u', '--username', help='your twitter username')
argparser.add_argument('-p', '--password', help='your twitter password')
args = vars(argparser.parse_args())

USERNAME = ''
PASSWORD = ''
if args['username']: USERNAME = args['username']
if args['password']: PASSWORD = args['password']
else: PASSWORD = getpass.getpass('Please enter your twitter password: ')

consumerKey = 'IsYNgsyYRIUdkVAlbnYw'
consumerSec = 'bZIOaYHEcfPx60Pq1D5YvZNyrMbERt5NkiEDo1fUKVo'
accessToken = '16859645-iXrSTXAp4gdX49zb5hfjG3ve3tkRYbMRwI5oyYgNy'
accessSecret = '1kGnxQ3slb4GXf40FGpW2Nzam5uCyrju4v7FTurXN4'

query = "acm%20multimedia,acmmm,#acmmm12"

fname = 'data/stream_' + datetime.now().strftime('%s') + '.js'
streamfile = codecs.open(fname, 'w', 'utf-8')

twitter_stream = TwitterStream(auth=UserPassAuth(USERNAME, PASSWORD))
iterator = twitter_stream.statuses.filter(track=query)

print 'Streaming (', query, ')\t', fname

for tweet in iterator:
  #print tweet['user']['name'], tweet['text']
  streamfile.write(str(tweet) + '\n')
  streamfile.flush()

