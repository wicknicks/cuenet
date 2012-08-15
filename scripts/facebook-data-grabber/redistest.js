var redis = require('redis'),
    channels = require('./channels')

channels.register('gcal_results', results)

var msg = new Object()
msg.session = "ss1"
msg.op = "auth"

setTimeout( function() {
  channels.send('gcal', JSON.stringify(msg))
}, 1000);

function results(msg) {
  console.log('Reply ' + JSON.stringify(msg))
  
  msg = new Object()
  msg.session = "ss1"
  msg.op = "sync"
  
  channels.send('gcal', JSON.stringify(msg))
  
  process.exit(0)
}

