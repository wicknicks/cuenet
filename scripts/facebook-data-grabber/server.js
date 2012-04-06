var redis = require('redis'),
    channels = require("./channels"),
    express = require('express'),
    app = express.createServer(),
    fs = require('fs'),
    storage = require('./storage'),
    format = require('util').format,
    executor = require('child_process').exec;
    
app.configure(function() {
  app.use(express.favicon(__dirname + '/public/img/emme.png'))
  app.use(express.methodOverride());
  app.use(express.bodyParser());
  app.use(app.router);
  app.use(express.static(__dirname + '/public'));
  app.use(express.logger('dev'));
  app.use(app.error);
});

app.get('/', function(req, res, next) {
  console.log(req.body)
  res.send('')
  //serve_html(res, '/web/fb-grabber.html');
});

app.post('/', function(req, res, next) {
  console.log(req.body)
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
  serve_file(res, 'text/html', __dirname + '/public/fb-grabber.html');
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
  console.log('going down');
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


