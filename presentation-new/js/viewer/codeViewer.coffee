define ['jquery', 'socket-io'], ($, ioSocket) ->
  class CodeViewer
    id = 'editor'

    constructor: ->
      console.log("I am here")

      @_initSocket()
      @_initEditors()

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

      console.log($("textarea[data-file='#{fileName}']").size())

      $("textarea[data-file='#{fileName}']").each ->
        console.log this.id
        editAreaLoader.setValue this.id, data.content

    _initEditors: =>
      codeViewer = @
      $('textarea[data-file]').each ->
        codeViewer._initEditor this.id
        codeViewer._fillEditorWithContent this.id

    _initEditor: (id) =>
      console.log 'Creating editor for id ' + id
      editAreaLoader.init({
        id: id,
        syntax: "java",

        start_highlight: true,
        allow_resize: "no",
        allow_toggle: true,
        language: "en",
        toolbar: "Editor",

        replace_tab_by_spaces: 4,
        min_height: 350
      })

    _fillEditorWithContent: (id) =>
      fileName = $('#' + id).attr('data-file')
      @_socket.emit 'file', {fileName: fileName}, (data) =>
        editAreaLoader.setValue id, data.content