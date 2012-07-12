import imaplib, getpass
from email import parser
import time
import uuid
from pymongo import Connection

mparser = parser.Parser();

connection = Connection('128.195.54.27', 27017)
db = connection['test']
collection = db['gmail']

def login(username):
  mail = imaplib.IMAP4_SSL('imap.gmail.com', 993)
  rc = mail.login(username, getpass.getpass())
  print 'Login Response', rc
  # mail.select ('INBOX')    #Inbox
  mail.select ('[Gmail]/All Mail')  #[Gmail]/All Mail
  return mail


def get_uids(mail, since):
  rc, data = mail.uid('search', since);
  if rc != 'OK':
    print rc
    return []
  else: return data[0].split()

def store_payload(message):
  directory = './data/'
  filename = uuid.uuid4().get_hex()
  fd = open(directory + filename, 'w');

  for part in message.walk():
    if part.get_content_type() == 'text/plain':
      filename =fd.write(part.get_payload())

  fd.close()
  return filename

def sync(uid):
  global mparser
  rcd = collection.find_one({'uid' : uid})
  if rcd != None:
    # print 'Record found.. ', uid
    return

  # Alternative Option:
  ## UID FETCH 102 (UID RFC822.SIZE BODY.PEEK[])
  rc, data = mail.uid('fetch', uid, '(RFC822.SIZE BODY.PEEK[])')

  ## Bring entire message. Marks it as read.
  ## rc, data = mail.uid('fetch', uid, '(RFC822)')
  message = mparser.parsestr(data[0][1])

  header = {}
  header['uid'] = uid;
  header['tx_time'] = int(time.time()*1000)

  for key in message.keys():
    header[key] = message[key]

  header['payload'] = store_payload(message)

  try:
    collection.insert(header)
  except:
    print 'Bad insert request %s: %s at %s' % (uid, rc, header['filename'])

def close(mail):
  mail.close()
  mail.logout()


if __name__ == "__main__":
  mail = login('cuenetemailtest')
  uids = get_uids(mail, '(SINCE 1-Jan-2010)')
  print 'Got', len(uids), 'uids'
  ix=0
  for uid in uids:
    sync(uid)
    ix += 1
    if ix % 25 == 0:
      print 'Got %d emails' % ix
  close(mail)


