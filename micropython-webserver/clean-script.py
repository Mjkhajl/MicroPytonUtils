import sys

del server, handler, sys.modules['webserver.server'], sys.modules['webserver']

gc.collect()
gc.mem_free()

import webserver.server as server

handler = server.MyHandler( "/handler" )
server = server.Server( 8080 )
server.handlers.append( handler )