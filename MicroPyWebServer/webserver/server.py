import socket as socket
from network import WLAN

class Server:

    CONTENT = """\\\nHTTP/1.0 200 OK\n\nHello #{} from MicroPython!\n\n"""

    def __init__( self, port ):
        self.port = port
        self.host = WLAN().ifconfig()[0]

    def start( self ):

        print( "starting...", self.host, ":", self.port )
        self.s = socket.socket( socket.AF_INET, socket.SOCK_STREAM )
        print( socket.getaddrinfo( self.host, self.port ) )
        self.s.bind( socket.getaddrinfo( self.host, self.port )[0][4] )
        self.s.listen( 5 )
        counter = 0

        while True:
            res = self.s.accept()
            client_s = res[0]
            client_addr = res[1]
            print( "Client address:", client_addr )
            print( "Client socket:", client_s )
            print( "Request:" )
            req = client_s.recv( 4096 )
            print( req )
            client_s.send( bytes( Server.CONTENT.format( counter ), "ascii" ) )
            client_s.close()
            parts = req.decode( 'ascii' ).split( ' ' )
            if parts[1] == '/exit':
                break
            counter += 1

    def stop( self ):

        print( "close socket... " )
        self.s.close();

