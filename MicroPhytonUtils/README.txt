I've developed this utils library because working with ESP8266 is hard using only REPL

Requirements:

  - Java 8 SDK
  - Java Comms API ( to connect through Serial COM port )
  - ESP8266 chip flashed with Micropython

Dev env:

  - Lenovo Ideapad Y400 Windows 10
  - Node MCU ESP8266 dev board with Microphyton 1.85 firmware.
  - My Wife, my three golden retrievers and my cat (boots) ;)

Current features:

  - 2016-11-02
  
     - Documented code and some javadoc added
     - Finished file sinchronizer and abstracted the synchronization logic from the filesystem implementation
     - Added a main for command line usage, current commands:
        
        - help:        shows the help
        - synchronize: syncrhonizes a local dir into an ESP8266 microphyton dir
        
     - Separated and restructures the whole project in the following main modules:
        
        - Detached communications utilities for serial communications
        - Detached REPL interface
        - Detached File system sincronization logic
        - Filesystem utils for REPL in the ESP8266
        - Streaming support to write and read remote files

  - 2016-10-26
      - [under development] FileSynchronizer:
        - Synchronizes a local file system folder with the file system in the ESP8266
    
Usage: 

  Main Class for command line usage: 
  
    mx.com.mjkhajl.micropy.utils.PythonUtilsMain <comand> <params...>
    
  Commands available: 
  
        help:        shows the help
        
          params: none
        
        synchronize: syncrhonizes a local dir into an ESP8266 microphyton dir
          
          params:
            <source file path> local dir in the file system that will be used as source, can be a relative path
            
            <dest file path>   remote dir in the ESP8266 that will be updated according to the local dir provided, 
                               should be an absolute path
