var redis = require('redis'),
    channels = require("./channels"),
    express = require('express'),
    app = express.createServer(),
    fs = require('fs'),
    storage = require('./storage'),
    format = require('util').format,
    executor = require('child_process').exec;
    
app.configure(function() {
  app.use(express.cookieParser());
  app.use(express.session({ secret: "godel, escher, bach" }));
  app.use(express.favicon(__dirname + '/public/img/emme.png'))
  app.use(express.methodOverride());
  app.use(express.bodyParser());
  app.use(app.router);
  app.use(express.static(__dirname + '/public'));
  app.use(express.logger('dev'));
  app.use(app.error);
});

channels.register('gcal_results', gcal_results)
responseCache = new Object()

app.get('/gcal', function(req, res) {
  serve_html(res, '/public/gcaldone.html')
  
  msg = new Object
  msg.session = req.session.id
  msg.op = "sync"
  msg.code = req.query['code']
  
  channels.send('gcal', JSON.stringify(msg))
});

app.post('/gcal', function (req, res) {
  var msg = new Object()
  msg.session = req.session.id
  msg.op = "auth"
  msg.email = req.body.username
  responseCache[msg.session] = res
  channels.send("gcal", JSON.stringify(msg))
});

function gcal_results(msg) {
  var res = responseCache[msg['session']]
  if (msg.op != 'auth') return;
  var o = new Object()
  o.ourl=msg['ourl']
  res.send(o);
}

app.get('/', function(req, res, next) {
  serve_html(res, '/public/fb-grabber.html');
});

app.post('/', function(req, res, next) {
  console.log('app.post /')
  res.send('')
});

app.post('/profile', function(req, res, next) {
  storage.profile(req.body);
  res.send('');
});

app.post('/relationships', function (req, res, next) {
  storage.relationships(req.body.user_id, req.body.friends);
  res.send('');
});

app.get('/sfu', function(req, res, next) {
  res.send('use post');
});

app.post('/sfu', function (req, res, next) {
  res.send('Got it');
  console.log(Object.keys(req.files));
  console.log(format('Received upload %s (%d Kb) to %s'
    , req.files.image.name
    , req.files.image.size / 1024 | 0
    , req.files.image.path));
  console.log('Original filename: ' + req.files.image.filename);
});

app.get('/uploader', function(req, res, next) {
  res.send('Hello, Android')
  console.log(req.body)
});

app.post('/psources', function(req, res, next) {
  console.log("Received: " + Object.keys(req.body));
  var content = JSON.stringify(req.body);
  
  var file = '/data/facebook/';
  
  var profile = req.body.profile
  if (profile == null) return
  if (profile.email) file += profile.email;
  else if (profile.username) file += profile.username
  else file = 'tmp__' + (new Date());
  
  res.send('')
  
  fs.writeFileSync(file, content, 'utf-8')
  console.log('[Facebook] Wrote file... ' + file)
  
});


app.post('/linkedin', function(req, res, next) {
  console.log("Received: " + Object.keys(req.body));
  var content = JSON.stringify(req.body);
  res.send('');
  
  var file = '/data/linkedin/';
  if (req.body.me.firstName) file += req.body.me.firstName
  if (req.body.me.lastName) file += '.' + req.body.me.lastName
  
  fs.writeFileSync(file, content, 'utf-8')
  console.log('[LinkedIn] Wrote file... ' + file)
})

app.post('/uploader', function(req, res, next) {
  console.log(format('Received upload %s (%d Kb) to %s'
    , req.files.image.name
    , req.files.image.size / 1024 | 0
    , req.files.image.path));
  resize(res, req.files.image.path)
  //redirect_to(res, '/verifier.html?i=' + img)
});

app.post('/verify', function(req, res, next) {
   var url = req.body.url;
   var uid = req.body.uid;
   var cmd = "python verify.py " + url + " " + uid;
   
   executor(cmd, function (error, stdout, stderr) {
    if (error) {
      console.log('Error ' + error);
      res.send('error');
    }
    if (stderr) {
      console.log('Error ' + stderr);
      res.send('error');
    }
    if (stdout) {
      //console.log('Data ' + stdout);
      console.log('Verification results sent to client');
      res.send(stdout);
    }
    console.log('Command Executed.');
  });
});

app.post('/photos', function (req, res, next) {
  storage.user_photos(req.body.user_id, req.body.photos);
  res.send('');
});

function serve_html(res, file_path) {
  serve_file(res, 'text/html', __dirname + file_path);
}

function serve_file(res, content_type, file_path) {
  res.header ('Content-Type', content_type);
  var content = fs.readFileSync (file_path, 'utf8');
  res.send(content);
}

app.listen(8080);

process.on ('SIGINT', clean_up);
process.on ('SIGTERM', clean_up);

function clean_up() {
  console.log('shutting down server');
  storage.close();
  app.close();
  process.exit();
}

app.error(function(err, req, res, next) {
  res.send('stop scanning my server!!\n', 404);
});

function redirect_to(res, url) {
  res.statusCode = 302;
  res.setHeader('Location', url);
  res.end();
}

function resize(res, img_path) {
  var cmd = "convert -resize 600x600 " + img_path + " " + img_path
  console.log('Running command: ' + cmd);
  executor(cmd, function (error, stdout, stderr) {
    if (error) {
      console.log('Error ' + error);
      redirect_to(res, 'verifier.html?i=error.png');
    }
    if (stderr) {
      console.log('Error ' + stderr);
      redirect_to(res, 'verifier.html?i=error.png');
    }
   
    var path = img_path.substr(img_path.lastIndexOf('/')+1);
    redirect_to(res, 'verifier.html?i='+path);
    
    console.log('Command Executed.');
  });
}

