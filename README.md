CueNet
======

Context Discovery Framework, with extensions and test cases, implemented as part of my PhD work. For information on CueNet, please visit [http://www.ics.uci.edu/~arjun/](http://www.ics.uci.edu/~arjun/).

Source Code
==========

The latest version of CueNet is maintained here: 

git clone https://github.com/wicknicks/cuenet.git

That should give you all the latest stuff.

Try doing the following commands to make sure the code builds and test
cases run:

```
mvn clean install
```

Install softwares wherever necessary with apt-get.

Note: the mvn command might take some time if you are running it for
the first time on your machine.

IDE (Eclipse)
============

Add the maven plugin for eclipse. Use the m2e plugin for this. 
http://www.eclipse.org/m2e/download/

Import the project into Eclipse:

File > Import > Maven > Existing Maven Project.


Image Processing Tools
===================
Install exiftool (http://www.sno.phy.queensu.ca/~phil/exiftool/)
and image-magick (http://www.imagemagick.org/script/index.php),
and make the executables (exiftool and convert) available on $PATH.

On Ubuntu, the commands to install these:

```
sudo apt-get install libimage-exiftool-perl
sudo apt-get install imagemagick
```

MongoDB
======

We use Mongo as our backend database. Current version is 1.8.3.

Download and extract the zip archive from either of the following
locations:

32 Bit: http://fastdl.mongodb.org/linux/mongodb-linux-i686-1.8.3.tgz
64 Bit: http://fastdl.mongodb.org/linux/mongodb-linux-x86_64-1.8.3.tgz

More detailed instructions on how to setup is given here:
http://www.mongodb.org/display/DOCS/Quickstart+Unix

Redis
=====

Also, we'll need Redis. Look [here](http://redis.io/) to see what it does. Get the latest
version from here:
http://redis.googlecode.com/files/redis-2.2.11.tar.gz

Unzip the package to a directory and run make to build it. The
executables are in the src directory itself. Use the command

```
cd /path/where/you/extracted/redis
make
./src/redis-server
```

to start the redis server.

Node.js
=======

The latest stable version of node is 0.8.16. Download it from here:
http://nodejs.org/dist/v0.8.16/node-v0.8.16.tar.gz

Install SSL libs if you don't have them on your machine.

```
sudo apt-get install libssl-dev
```

Extract the contents to a directory on your computer, and cd into it.
Now, run the following commands:

```
cd /path/where/you/extracted/zip/file
./configure 
make 
sudo make install
```

This will install node into your machine. To see if it works, type
node and see if it starts a REPL.

To start playing with node, please read the synopsis here:
http://nodejs.org/docs/v0.8.16/api/synopsis.html

Local Folders
============

Also, ensure that you have the folders called /data/photos, /data/db
and /data/index -- this is where all the photos, data and indexes will
come and sit after upload. Ensure that your dataset is in $HOME/dataset.


Starting The Application
=====================

You must start four different programs for the entire application
stack to be functional.

#### Start Redis

Redis is configured through a config file called redis.conf. This is
located in the root directory of your redis installation. Change the
following entries in it.

a) change the loglevel entry to "loglevel notice".
b) change the timeout entry to "timeout 0".

start the redis server as:

```
./src/redis-server ./redis.conf
```

(from the redis root directory).

#### Start MongoDB

Start mongodb using the mongo daemon.

```
./mongod
```

#### Start Web Server

cd to the "web" folder which is part of the EPT folder you downloaded
from the SVN repository. The main file for the server impl is called
server.js which is part of the web sub-directory.

Then, start it using node as:

```
cd cuenet/scripts/web
node server.js
```

This should give you a message like:

```
   subscribed to gcal_results, count: 1
   Connection to DB established 
```

This means the server has connected to redis and MongoDB. Now point your browser to: [http://localhost:8080/](http://localhost:8080/) and the webpage will ask you to log into different websites so it can pull data from them.


#### Tests

Start the java program from your IDE. Run any tests in the src/tests/java folder. To test the discovery algorithm, try DiscoveryTester.java or Scratch.java in src/tests/java/test package.
