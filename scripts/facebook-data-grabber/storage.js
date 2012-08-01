var Db = require('mongodb').Db,
    Connection = require('mongodb').Connection,
    Server = require('mongodb').Server;
    
var client = new Db('arjun', new Server("127.0.0.1", 27017, {}));

client.open(function(err, p_client) {
  if (!err) console.log('Connection to DB established ');
});

exports.profile = function(user_profile) {
  console.log('update profile ' + user_profile.id);
  user_profile.tx_time = Date.now();
  client.collection('fb_users', function(err, collection) {
    collection.remove({id: user_profile.id}, function (err, result) {
      collection.insert(user_profile, function (err, docs) {});
    });
  });
};

exports.relationships = function(user_id, relationships) {
  console.log('relationships bet ' + user_id + ' ' + relationships.length);
  client.collection('fb_relationships', function(err, collection) {
    collection.remove({id: user_id}, function (err, result) {
      relationships.map (function (relation) {
        store_relationship(user_id, relation, collection);
      });
    });
  });
};

function store_relationship(user_id, relation, collection) {
  var o = new Object();
  o.id = user_id;
  o.tx_time = Date.now();
  o.relationship_type = 'i_knows'; //knows + inverse
  o.relation = relation.id;
  o.relation_name = relation.name; //just to make db more readable
  collection.insert(o, function (err, docs) {});
}

exports.user_photos = function(user_id, photos) {
  if (photos.length <= 0) return;
  console.log('adding ' + photos.length);
  client.collection('fb_photos', function(err, collection) {
    collection.remove({id: user_id}, function (err, result) {
      for (var i=0; i<photos.length; i++) {
        var o = new Object();
        o.id = user_id;
        o.tx_time = Date.now();
        o.photo = photos[i];
        collection.insert(o, function (err, docs) {});
      }
    });
  });
};

exports.friends_photos = function(user_id, friends_photos) {
  console.log('friends photos');
};

exports.close = function () {
  client.close();
};



