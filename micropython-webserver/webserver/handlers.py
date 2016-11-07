
class HandlerBroker:

    def __init__( self ):
        self.handlers = {}

    def parseRequest( self, c_sock ):
        c_sock.setblocking( False )
        line = self.parse1stline( c_sock )
        if line is not None:
            method, path, version = line
            headers = {}
            while self.parseHeader( c_sock, headers ):
                continue
            return HttpRequest( method, path, version, headers, "", c_sock )
    def getHandler( self, request ):
        return RequestHandler( request.method, request.path )

    def addHandler( self, handler ):
        self.handlers.append( handler )

    def parse1stline( self, c_sock ):
        line = c_sock.readline()
        if line is not None:
            return line.decode().split( ' ' )

    def parseHeader( self, c_sock, headers ):
        line = c_sock.readline()
        if line is not None:
            idx = line.decode().find( ':' )
            if idx >= 0:
                headers[line[:idx]] = line[idx + 1:]
                return True
        return False

class HttpRequest:

    def __init__( self, method, path, version, headers, content, c_sock ):
        self.method = method
        self.path = path
        self.version = version
        self.headers = headers
        self.content = content

class RequestHandler:

    NOT_FOUND = """\\\nHTTP/1.0 404 NotFound\n\nResource: {0}\nMethod: {1}\n was not found!\n\n"""

    def __init__( self, method, path_regex ):
        self.method = method
        self.path_regex = path_regex

    def handle( self, request, c_sock ):
        c_sock.send( 
            bytes( RequestHandler.NOT_FOUND.format( request.path, request.method ), "utf-8" ) )
