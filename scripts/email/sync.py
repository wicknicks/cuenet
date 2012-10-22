import os, sys, time, json
import imaplib, getpass
from email import parser
import re, argparse

# Username for the email account
# Gmail: arjun.satish (for arjun.satish@gmail.com)
# UCI: arjun (for arjun@uci.edu or arjun@ics.uci.edu)
USERNAME = 'cuenetemailtest'

# Location of the IMAP server 
# mail.ucsd.edu for UCSD
# imap.sdsc.edu for SDSC
# imap.gmail.com for Gmail
IMAP_SERVER = 'imap.gmail.com'
OUTPUT_FILE = None
IMAP_PORT = 993                      # for secure connection, otherwise 143
MAILBOXES = []

START_DATE = '1-Jan-2012'
END_DATE = '1-Oct-2012'

argparser = argparse.ArgumentParser()
argparser.add_argument('-i', '--imap', help='IMAP Server (like imap.sdsc.edu)')
argparser.add_argument('-o', '--out', help='location of output file.')
argparser.add_argument('-u', '--user', help='location of output file.')
argparser.add_argument('-s', '--start', help='start date (eg: "25-Jul-2012").')
argparser.add_argument('-e', '--end', help='end date (eg: "27-Jul-2012").')
args = vars(argparser.parse_args())

if args['imap']: IMAP_SERVER = args['imap']
if args['out']: OUTPUT_FILE = args['out']
if args['user']: USERNAME = args['user']
if args['start'] and args['end']: 
  START_DATE = args['start']
  END_DATE = args['end']

mparser = parser.Parser();
cFile = None

def listall(mail): 
  pattern = re.compile(r'\((?P<flags>.*?)\) "(?P<delimiter>.*)" (?P<name>.*)')
  rc, boxes = mail.list(pattern='*')
  mailboxes = []
  for box in boxes:
    flags, delimiter, mailbox_name = pattern.match(box.decode("utf-8")).groups()
    if "Noselect" in flags: continue
    #print (flags + "%%%%%" + delimiter + "%%%%%" + mailbox_name)
    mailboxes.append(mailbox_name)
  
  return mailboxes

def login(username):
  global MAILBOXES

  mail = imaplib.IMAP4_SSL(IMAP_SERVER, IMAP_PORT)
  pwd = getpass.getpass('Password for ' + USERNAME + ' at ' + IMAP_SERVER + ': ')
  print ('Authenticating...')
  rc = mail.login(username, pwd)
  print ('Login Response', rc)
  
  if IMAP_SERVER == 'imap.gmail.com':
    MAILBOXES = ['"[Gmail]/All Mail"']
  else:
    MAILBOXES = listall(mail)
    #print (MAILBOXES)
  
  # mail.select ('INBOX')    #Inbox
  # mail.select ('ongoing')  #
  return mail
  
def get_uids(mail, since):
  rc, data = mail.uid('search', since);  ##2.6 & 2.7  
  if rc != 'OK':
    print ('Error in getting uids', rc)
    return []
  else: return data[0].split()

def filter_and_write(uid, header):
  keys = ['uid', 'date', 'from', 'cc', 'to']
  buff = {}
  
  for _k in dict.keys(header):
    for rk in keys:
      if _k.lower() == rk:
        buff[rk] = header[_k]
  
  csv_write(buff)
  
def csv_write(buff):
  global cFile
  cFile.write(json.dumps(buff))
  cFile.write('\n')

def sync(uid):
  global mparser
  
  # Alternative Option:
  ## UID FETCH 102 (UID RFC822.SIZE BODY.PEEK[])
  rc, data = mail.uid('fetch', uid, '(RFC822.SIZE BODY.PEEK[])')
  if rc != 'OK':
    print ('Sync Error', rc)
    return
    
  ## Bring entire message. Marks it as read.
  ## rc, data = mail.uid('fetch', uid, '(RFC822)')
  # message = mparser.parsestr(data[0][1].decode('utf-8'))  ## py 3.x
  message = mparser.parsestr(data[0][1])

  header = {}
  header['uid'] = uid;
  header['tx_time'] = int(time.time()*1000)

  for key in message.keys():
    header[key] = message[key]

  filter_and_write(uid.decode('utf-8'), header)

def close(mail):
  mail.close()
  mail.logout()
  
if __name__ == "__main__":

  if OUTPUT_FILE == None: OUTPUT_FILE = '/data/email/' + USERNAME
  
  if os.path.exists(OUTPUT_FILE): 
    print ('The file', OUTPUT_FILE, 'exists.')
    print ('Press any key if you want to overwrite. Else press Ctrl-C')
    sys.stdin.read(1)

  cFile = open(OUTPUT_FILE, 'w')
  mail = login(USERNAME)
  for mailbox in MAILBOXES:
    print ('Selecting folder', mailbox)
    rc, data = mail.select(mailbox)
    #print (rc, data[0], mailbox)
    if data[0].decode('utf-8') == '0': continue
    #uids = get_uids(mail, '(SINCE "25-Jul-2012")')  
    uids = get_uids(mail, '(SINCE "' + START_DATE + '" BEFORE "' + END_DATE + '")')
    print ('Got', len(uids), 'uids in: ', mailbox)
    ix=0
    for uid in uids:
      sync(uid)
      ix += 1
      if ix % 200 == 0: print ('Got %d emails' % ix)
  close(mail)
  cFile.flush()
  cFile.close()

