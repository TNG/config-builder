define ['jquery', 'socket-io', 'highlight'], ($, ioSocket, highlight) ->
  class CodeViewer
    id = 'editor'

    constructor: ->
      @_initSocket()
      @_initViewers()

    _initSocket: =>
      @_socket = ioSocket.connect();

      @_socket.on 'connect', =>
        console.log 'connect'
      @_socket.on 'disconnect', =>
        console.log 'disconnect'
      @_socket.on 'file', @_fileContentChanged

    _fileContentChanged: (data) =>
      fileName = data.fileName
      console.log "Content of file #{fileName} has changed"

      $("code[data-file='#{fileName}']").each ->
        $('#' + this.id).html(highlight.highlight("java", data.content).value);

    _initViewers: =>
      codeViewer = @
      $('code[data-file]').each ->
        codeViewer._fillViewerWithContent this.id

    _fillViewerWithContent: (id) =>
      fileName = $('#' + id).attr('data-file')
      @_socket.emit 'file', {fileName: fileName}, (data) =>
        $('#' + id).html(highlight.highlight("java", data.content).value)
