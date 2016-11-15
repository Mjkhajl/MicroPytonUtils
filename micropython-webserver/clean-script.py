del server, sys.modules['webserver.server'], sys.modules['webserver']

gc.collect()
gc.mem_free()

import webserver.server as server

server = server.Server( 8080 )
gc.collect()
gc.mem_free()