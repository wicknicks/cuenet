from xml.etree import ElementTree
import gdata.calendar.data
import gdata.calendar.client
import atom, getopt, sys
from getpass import getpass

def print_help_and_exit():
  print ("python " + sys.argv[0] + " --user [username] " + \
                           "--pass [password] " + \
                           "--start '2011-01-01' " + \
                           "--end '2011-06-21'")
  sys.exit(2)

try:
  opts, args = getopt.getopt(sys.argv[1:], "hu:p:", \
                              ["user=", "pass=", "start=", "end="])
except getopt.error, msg:
  print_help_and_exit()

if ('-h', '') in opts: print_help_and_exit()

username = None
password = None
start_date = '2012-06-01'
end_date = '2012-07-01'

for opt, arg in opts:
  if opt == '--user' or opt == '-u':
    username = arg
  elif opt == '--pass' or opt == '-p':
    password = arg
  elif opt == '--start':
    start_date = arg
    print 'nnnn', arg
  elif opt == '--end':
    end_date = arg
    print 'mmmm', arg

if username == None or password == None: print_help_and_exit()

print 'Querying:', username, 'from', start_date, 'to', end_date

client = gdata.calendar.client.CalendarClient(source='GCalPy-CN')
client.ClientLogin(username, password, client.source)

query = gdata.calendar.client.CalendarEventQuery(start_date, \
                            end_date);

feed=client.GetCalendarEventFeed(q=query)
print (len(feed.entry))

j=1
for i, an_event in zip(xrange(len(feed.entry)), feed.entry):
  for a_when in an_event.when:
    ## print '%s. %s' % (j, an_event.title.text)
    j+=1
    #print '  Start Time: %s' % (a_when.start,)
    #print '  End Time: %s' % (a_when.end,)
  #for a_who in an_event.who:
    #print '  Email: %s' % (a_who.email,)
    #print '  Value: %s' % (a_who.value,)
  #for a_where in an_event.where:
    #print '  Location: %s' % (a_where.label,)
    #print '  Value: %s' % (a_where.value,)

print j,'entries found'  

