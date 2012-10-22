from twitter import *
import codecs
from datetime import datetime

twitter_search = Twitter(domain="search.twitter.com")

query = "(acm multimedia) OR (acmmm) OR (#acmmm12)"

r = twitter_search.search(q=query)

count = len(r['results'])
print count, 'results found.'

t = lambda i: r['results'][i]

fname = 'data/search_' + datetime.now().strftime('%s') + '.js'
streamfile = codecs.open(fname, 'w', 'utf-8')

for i in xrange(0, count):
  #print i, t(i)['from_user_name'], t(i)['text']
  streamfile.write(str(t(i)) + '\n')
  streamfile.flush()
  
streamfile.close()
