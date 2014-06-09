var fs = require('fs'),
    sys = require('sys'),
    express = require('express'),
    http = require('http'),
    io = require('socket.io'),
    childProcess = require('child_process');

var app = express();
var server = http.createServer(app);
var socket = io.listen(server);

socket.set('log level', 1);

server.listen(3000);

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
                callback({error: 'No such file'});
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
        console.log("Executing " + data.className);
        childProcess.exec("java/buildAndExecute.sh " + data.className, function(error, stdout) {
            console.log("Execution ended " + (error == null ? "successfully" : "with an error: " + error));
            data = {className: data.className, output: error == null ? stdout : error.toString()};

            callback(data);
            sendToAllClients('executionResult', data);
        });
    });

    client.on('disconnect', function() {
        console.log("disconnect");
        removeClient(client);
    });
});