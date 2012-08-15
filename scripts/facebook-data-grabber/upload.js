var redis = require('redis'),
    channels = require("./channels"),
    express = require('express'),
    app = express.createServer(),
    fs = require('fs'),
    storage = require('./storage'),
    format = require('util').format;
    
app.configure(function() {
  app.use(express.favicon(__dirname + '/public/img/emme.png'))
  app.use(express.methodOverride());
  app.use(express.bodyParser());
  app.use(app.router);
  app.use(express.static(__dirname + '/public'));
  app.use(express.logger('dev'));
  app.use(app.error);
});

app.get('/', function(req, res, next){
  res.send('<form action="/uploader" method="post" enctype="multipart/form-data">'
    + '<p>Title: <input type="text" name="title" /></p>'
    + '<p>Image: <input type="file" name="image" /></p>'
    + '<p><input type="submit" value="Upload" /></p>'
    + '</form>');
});

app.post('/', function(req, res, next){
  // the uploaded file can be found as `req.files.image` and the
  // title field as `req.body.title`
  res.send(format('\nuploaded %s (%d Kb) to %s as %s'
    , req.files.image.name
    , req.files.image.size / 1024 | 0
    , req.files.image.path
    , req.body.title));
});

/*
app.get('/', function(req, res, next) {
  console.log(req.body)
  res.send('')
  //serve_html(res, '/web/fb-grabber.html');
});

app.post('/', function(req, res, next) {
  console.log(req.body)
  res.send('')
});
*/


app.post('/uploader', function(req, res, next) {
  res.send(format('\nuploaded %s (%d Kb) to %s as %s'
    , req.files.image.name
    , req.files.image.size / 1024 | 0
    , req.files.image.path
    , req.body.title));
  res.send('');
});

app.post('/relationships', function (req, res, next) {
  storage.relationships(req.body.user_id, req.body.friends);
  res.send('');
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

app.listen(3000);

process.on ('SIGINT', clean_up);
process.on ('SIGTERM', clean_up);

function clean_up() {
  console.log('going down');
  storage.close();
  app.close();
  process.exit();
}

app.error(function(err, req, res, next) {
  res.send('stop scanning my server\n', 404);
});

