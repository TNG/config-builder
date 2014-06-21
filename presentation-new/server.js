var fs = require('fs'),
    sys = require('sys'),
    express = require('express'),
    http = require('http'),
    io = require('socket.io'),
    childProcess = require('child_process'),
    minimist = require('minimist');

var app = express();
var server = http.createServer(app);
var socket = io.listen(server);

var args = minimist(process.argv.slice(2));
var port = args.port || 8000;

socket.set('log level', 1);

server.listen(port);

app.use(express.static(__dirname));

clients = [];
function addClient(client) {
    clients.push(client);
}

function removeClient(client) {
    var index = clients.indexOf(client);
    if (index != -1) {
        clients.splice(index, 1);
    }
}

socket.on('connection', function(client) {
    console.log('new connection');
    addClient(client);

    function sendToAllClients(eventName, data) {
        for (var i = 0; i < clients.length; i++) {
            clients[i].emit(eventName, data)
        }
    }

    client.on('file', function(data, callback) {
        console.log("File " + data.fileName + " requested");
        fs.readFile(data.fileName, function(err, fileData) {
            if (err) {
                console.log("File " + data.fileName + " not present");
            } else {
                console.log("File " + data.fileName + " sent");
                callback({fileName: data.fileName, content: fileData.toString()});
            }
        });
    });

    client.on('changeFile', function(data) {
        console.log("Writing to file " + data.fileName);
        fs.writeFile(data.fileName, data.content);
        sendToAllClients('file', data);
    });

    client.on('execute', function(data, callback) {
        console.log("Executing " + data.command);
        childProcess.exec(data.command, function(error, stdout) {
            console.log("Execution ended " + (error == null ? "successfully" : "with an error: " + error));
            data = {fileName: data.fileName, output: error == null ? stdout : error.toString()};

            callback(data);
            sendToAllClients('executionResult', data);
        });
    });

    client.on('disconnect', function() {
        console.log("disconnect");
        removeClient(client);
    });
});