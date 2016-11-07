import socket, sys, gc
from network import WLAN
from webserver.handlers import HandlerBroker

class Server:
    def __init__( self, port ):
        self.port = port
        self.host = WLAN().ifconfig()[0]

    def start( self ):
        print( "starting...", self.host, ":", self.port )
        sock = socket.socket( socket.AF_INET, socket.SOCK_STREAM )
        sock.bind( socket.getaddrinfo( self.host, self.port )[0][4] )
        sock.listen( 5 )
        broker = HandlerBroker()
        try:
            while True:
                c_sock, c_addr = sock.accept()
                try:
                    print( "Incoming: ", c_addr, ", sock: ", c_sock )
                    request = broker.parseRequest( c_sock )
                    broker.getHandler( request ).handle( request, c_sock );
                except Exception as e:
                    sys.print_exception( e )
                finally:
                    c_sock.close()
                    gc.collect()
        finally:
            if sock is not None:
                sock.close()
            print( "good bye!" )