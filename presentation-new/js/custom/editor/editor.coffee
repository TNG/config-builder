define ['jquery', 'socket-io'], ($, ioSocket) ->
  class Editor
    activeOptionSelector = '#editor-file option:selected'

    id = 'editor'

    constructor: ->
      @_initSocket()
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

    _initChangeHandler: =>
      $('#editor-file').change @_loadNewFile

    _loadNewFile: =>
      fileName = @_getCurrentFileName()
      @_socket.emit 'file', {fileName: fileName}, (data) =>
        if not data.error?
          @_createEditor(@_getCurrentLanguage())
          editAreaLoader.setValue id, data.content
          $('#' + id).attr('data-file', fileName).attr('data-execute', @_getCurrentExecuteCommand())
          $('.ouput').text('');

          @_editorContent = data.content
        else
          console.log "Error loading file: #{data.error}"

    _createEditor: (language) =>
      editAreaLoader.init({
        id: id,
        syntax: language,

        start_highlight: true,
        allow_resize: "no",
        allow_toggle: true,
        language: "en",
        toolbar: "Editor",

        replace_tab_by_spaces: 4,
        min_height: 350
      })

    _initSaveHandler: =>
      setInterval @_saveNewFileContent, 300

    _saveNewFileContent: =>
      fileName = $('#' + id).attr('data-file')
      language = $('#' + id).attr('data-language')

      newEditorContent = editAreaLoader.getValue(id)
      if newEditorContent != @_editorContent
        @_socket.emit 'changeFile', { fileName: fileName, content: newEditorContent }
        @_editorContent = newEditorContent

    _initExecuteHandler: =>
      $('#execute').click =>
        executeCommand = $('#' + id).attr('data-execute')
        fileName = $('#' + id).attr('data-file')
        $('.output').text "Executing ... Please wait!"
        @_socket.emit 'execute', {fileName: fileName, command: executeCommand}, (data) =>
          $('.output').text data.output

    _getCurrentFileName: =>
      $('#editor-file').attr('data-base-path') + '/' + $('#editor-file option:selected').attr('value')

    _getCurrentLanguage: =>
      $(activeOptionSelector).attr('data-language')

    _getCurrentExecuteCommand: =>
      $(activeOptionSelector).attr('data-execute')

  $ ->
    new Editor()