define ['jquery', 'socket-io'], ($, ioSocket) ->
  class Editor
    id = 'editor'

    constructor: ->
      @_initSocket()
      @_initEditor()
      @_initChangeHandler()
      @_initSaveHandler()
      @_initExecuteHandler()

      @_editorContent = ""
      @_loadNewFile()

    _initSocket: =>
      @_socket = ioSocket.connect();

      @_socket.on 'connect', =>
        console.log 'connect'
      @_socket.on 'disconnect', =>
        console.log 'disconnect'

    _initEditor: =>
      console.log 'Creating editor'
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

    _initChangeHandler: =>
      $('#editor-file').change @_loadNewFile

    _loadNewFile: =>
      fileName = @_getCurrentFileName()
      @_socket.emit 'file', {fileName: fileName}, (data) =>
        if not data.error?
          editAreaLoader.setValue id, data.content
          $('#' + id).attr('data-file', fileName)
          $('.ouput').text('');

          @_editorContent = data.content
        else
          console.log "Error loading file: #{data.error}"

    _initSaveHandler: =>
      setInterval @_saveNewFileContent, 300

    _saveNewFileContent: =>
      fileName = @_getCurrentFileName()
      newEditorContent = editAreaLoader.getValue(id)
      if newEditorContent != @_editorContent
        @_socket.emit 'changeFile', { fileName: fileName, content: newEditorContent }
        @_editorContent = newEditorContent

    _initExecuteHandler: =>
      $('#execute').click =>
        className = @_getCurrentClassName()

        $('.output').text "Executing ... Please wait!"
        @_socket.emit 'execute', {className: className}, (data) =>
          $('.output').text data.output

    _getCurrentFileName: =>
      $('#editor-file').attr('data-base-path') + "/" + $('#editor-file').attr('data-base-package-name') + '/' + $('#editor-file option:selected').attr('value')

    _getCurrentClassName: =>
      classNamePath = $('#editor-file').attr('data-base-package-name') + '/' + $('#editor-file option:selected').attr('value')
      className = classNamePath.replace(/\.java/, "").replace(/\//g, ".")
      return className
