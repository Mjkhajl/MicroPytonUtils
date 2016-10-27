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

  - [under development] FileSynchronizer:
    - Synchronizes a local file system folder with the file system in the ESP8266
    
Usage: 

  ther is no main at the moment... ESP8266FileSystemSynchronizer is the main implementation, use the following method: 
  
    void synchronizeFs( File srcDir, String dest ) throws Exception ;
    
  I will develop a main for commandLine and upload a ready to use Jar as soon as I can.
