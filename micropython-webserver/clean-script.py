del server
sys.modules.clear()

gc.collect()
aft = gc.mem_free()
aft

import webserver.server as server

gc.collect()
curr = gc.mem_free()
curr
aft - curr
del aft, curr