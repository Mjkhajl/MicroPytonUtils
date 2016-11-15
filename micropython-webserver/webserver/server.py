from network import WLAN
from ure import compile
from socket import socket, AF_INET, SOCK_STREAM, getaddrinfo
from sys import print_exception

class Server:

    def __init__( self, port ):
        self.port = port
        self.host = WLAN().ifconfig()[0]
        self.version = "0.02_12";

    def start( self ):
        print( "(v.", self.version, ") starting...", self.host, ":", self.port )
        sock = socket( AF_INET, SOCK_STREAM )
        sock.bind( getaddrinfo( self.host, self.port )[0][4] )
        sock.listen( 5 )
        try:
            while True:
                c_sock, c_addr = sock.accept()
                try:
                    req_processor.process( self.parseRequest(c_sock, c_addr) )
                except Exception as e:
                    print_exception( e )
                finally:
                    c_sock.close();
        finally:
            if sock: sock.close();
            print( "good bye!" )

    def parseRequest( self, c_sock, c_addr ):
        line = c_sock.readline()
        if line is not None:
            method, path, version = line.decode().strip( '\n' ).split( ' ' )
            headers = {}
            while self.parseHeader( c_sock, headers ):
                continue
            return HttpRequest( method, path, version, headers, "", c_sock, c_addr )

    def parseHeader( self, c_sock, headers ):
        line = c_sock.readline()
        if line is not None:
            idx = line.decode().find( ':' )
            if idx >= 0:
                headers[line[:idx]] = line[idx + 1:]
                return True
        return False

class HttpRequest:
    def __init__( self, method, path, version, headers, content, c_sock, c_addr ):
        ( self.method, self.path, self.version, self.headers, self.content, self.c_sock, self.c_addr ) = ( method, path, version, headers, content, c_sock, c_addr )
        print( "'".join( ["method: ", method, " path: ", path, " version: ", version, " headers: ", str( headers ), " content: ", content, " addr: ", str( c_addr )] ) + '\n' +  str(c_sock) )

class GenericRequestProcessor:

    RESPONSE = "HTTP/1.0 {0} {1}\n{2}\n\n{3}\n\n"

    def __init__( self ):
        self.handlers = []

    def register( self, regexp ):
        def gethandler( f ):
            self.handlers.insert( 0, ( compile( regexp ), f ) )
            return f
        return gethandler;

    def process( self, request ):
        ex = None
        res_headers=[]
        try:
            for pattern, handler in self.handlers:
                if pattern.match( request.path ):
                    res_code, res_message, body = handler( request, res_headers )
                    break
        except Exception as e:
            ex = e
            res_code, res_message, body = 500, "Server Error", "Resource: " + str( request.path ) + "\nError: " + str( e );
        res_headers.append( "Content-Length: " + str( len( body ) ) )
        response = bytes( GenericRequestProcessor.RESPONSE.format( res_code, res_message, '\n'.join( res_headers ), body ), "utf-8" )
        print( response )
        request.c_sock.send( response )
        if ex: raise ex;

req_processor = GenericRequestProcessor()

@req_processor.register( "." )
def default_handler( request, res_headers ):
    return ( 404, "Not Found", "Micropython server\nResource: {0}\nMethod: {1}\nNot Found!!!".format( request.path, request.method ) )

@req_processor.register( "/algo" )
def algo_handler( request, res_headers ):
    res_headers.append( "Content-Type: text/html" );
    return ( 200, "OK", "<html><body><h3>Resource: {0}\nMethod: {1}\nWas Found!!!<h3></html>".format( request.path, request.method ) )
