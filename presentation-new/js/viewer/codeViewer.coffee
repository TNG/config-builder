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
        $('#' + this.id).html(highlight.highlight("java", @_getContent(data.content)).value);

    _initViewers: =>
      codeViewer = @
      $('code[data-file]').each ->
        codeViewer._fillViewerWithContent this.id

    _fillViewerWithContent: (id) =>
      fileName = $('#' + id).attr('data-file')
      @_socket.emit 'file', {fileName: fileName}, (data) =>
        if not data.error?
          $('#' + id).html(highlight.highlight("java", @_getContent(data.content)).value)
        else
          console.log "Error loading file: #{data.error}"

    _getContent: (data) =>
      # Replace every line between "// not shown" and "// shown"
      data = data.replace /.*\/\/(\s)*not shown([\s\S])*?\/\/(\s)*shown.*\n/g, ""
      return data
