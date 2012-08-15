var redis = require('redis'),
  client = redis.createClient(),
  pubClient = redis.createClient(),
  callbackHash = new Object();

exports.register = function (channel, callback) {
  client.subscribe(channel);
  callbackHash[channel] = callback;
};

exports.send = function (channel, message) {
  if (message.length > 500) console.log("[" + (new Date()) + "] " + "sending on: " + channel);
  else console.log("[" + (new Date()) + "] " + "sending on: " + channel + ", " + message);
  pubClient.publish(channel, message);
};

client.on("subscribe", function (channel, count) {
    console.log("subscribed to " + channel + 
		", count: " + count);
  });

client.on("message", function (channel, message) {
    var cb = callbackHash[channel];
    var msg = JSON.parse(message);
    console.log("[" + (new Date()) + "] " + "Received msg of " + msg.op + " ( " + message.length + " bytes )");
    cb(msg);
  });

