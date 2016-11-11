import socket, sys, gc
import ure as re

from network import WLAN

class Server:
    VERSION = "1.0_12"
    ERROR = "HTTP/1.0 500 Server Error\n\nResource: {0}\nError: {1}\n\n"
    def __init__( self, port ):
        self.port = port
        self.host = WLAN().ifconfig()[0]
        self.defaultHandler = DefaultReqHandler( "" );
        self.handlers = []

    def start( self ):
        print( "(v.", Server.VERSION, ") starting...", self.host, ":", self.port )
        sock = socket.socket( socket.AF_INET, socket.SOCK_STREAM )
        sock.bind( socket.getaddrinfo( self.host, self.port )[0][4] )
        sock.listen( 5 )
        try:
            while True:
                c_sock, c_addr = sock.accept()
                try:
                    print( "Incoming: ", c_addr, ", sock: ", c_sock )
                    request = self.parseRequest( c_sock )
                    if request is not None:
                        self.getHandler( request ).handle( request, c_sock );
                except Exception as e:
                    sys.print_exception( e )
                    c_sock.send( bytes( Server.ERROR.format( request.path, e ), "utf-8" ) )
                finally:
                    c_sock.close()
                    gc.collect()
        finally:
            if sock is not None:
                sock.close()
            print( "good bye!" )

    def parseRequest( self, c_sock ):
        c_sock.setblocking( False )
        line = c_sock.readline()
        if line is not None:
            method, path, version = line.decode().strip( '\n' ).split( ' ' )
            headers = {}
            while self.parseHeader( c_sock, headers ):
                continue
            return HttpRequest( method, path, version, headers, "", c_sock )

    def parseHeader( self, c_sock, headers ):
        line = c_sock.readline()
        if line is not None:
            idx = line.decode().find( ':' )
            if idx >= 0:
                headers[line[:idx]] = line[idx + 1:]
                return True
        return False

    def getHandler( self, request ):
        for handler in self.handlers:
            if handler.url_pattern.match( request.path ) is not None:
                return handler;
        return self.defaultHandler

class HttpRequest:

    def __init__( self, method, path, version, headers, content, c_sock ):
        self.method = method
        self.path = path
        self.version = version
        self.headers = headers
        self.content = content
        print( "'".join( ["method: ", method, " path: ", path, " version: ", version, " headers: ", str( headers ), " content: ", content] ) )

class DefaultReqHandler:

    RESPONSE = "HTTP/1.0 {0} {1}\n{2}\n\n{3}\n\n"

    def __init__( self, url_regex ):
        self.url_pattern = re.compile( url_regex )

    def handle( self, request, c_sock ):
        self.res_code = 200
        self.res_message = "OK"
        res_headers = []
        body = self.getBody( request, c_sock, res_headers )
        res_headers.append( "Content-Length: {0}".format( len( body ) ) )
        c_sock.send( bytes( DefaultReqHandler.RESPONSE.format( self.res_code, self.res_message, '\n'.join( res_headers ), body ), "utf-8" ) )

    def getBody( self, request, c_sock , res_headers ):
        self.res_code = 404
        self.res_message = "Not Found"
        return "Micropython server\nResource: {0}\nMethod: {1}\nNot Found!!!".format( request.path, request.method )

class MyHandler( DefaultReqHandler ):

    def getBody( self, request, c_sock , res_headers ):
        res_headers.append( "Content-Type: text/html" )
        return "<html><body><h3>Resource: {0}\nMethod: {1}\nWas Found!!!<h3></html>".format( request.path, request.method )