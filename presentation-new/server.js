var fs = require('fs'),
    sys = require('sys'),
    express = require('express'),
    http = require('http'),
    io = require('socket.io');



var app = express();
var server = http.createServer(app);
var socket = io.listen(server);

socket.set('log level', 1);

server.listen(3000);

app.use(express.static(__dirname));

socket.on('connection', function(client) {
    console.log('new connection');

    client.on('file', function(data, callback) {
        console.log("File " + data.fileName + " requested");
        fs.readFile(data.fileName, function(err, fileData) {
            if (err) {
                console.log("File " + data.fileName + " not present");
                callback({error: 'No such file'});
            } else {
                console.log("File " + data.fileName + " sent");
                callback({content: fileData.toString()});
            }
        });
    });

    client.on('changeFile', function(data) {
        console.log("Writing to file " + data.fileName);
        fs.writeFile(data.fileName, data.content)
    });

    client.on('disconnect', function() {
        console.log("disconnect");
    });
});