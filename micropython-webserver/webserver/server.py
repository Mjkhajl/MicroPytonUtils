from socket import socket, AF_INET, SOCK_STREAM
from gc import collect, mem_free
from ure import compile
from sys import print_exception

_RESPONSE = "HTTP/1.0 {0} {1}\n{2}\n\n{3}\n\n"

def start( port ):
	print( "(v.0.3_12) starting... 0.0.0.0:", port )
	sock = socket( AF_INET, SOCK_STREAM )
	sock.setblocking( True )
	sock.bind( ( "0.0.0.0", port ) )
	sock.listen( 5 )
	try:
		while True: # Server Loop
			c_sock, c_addr = sock.accept()
			try:
				print( "incomming...", c_addr )
				c_sock.settimeout( 5 )
				req = __parseRequest( c_sock ) # parse the request
				res = req_processor.process( req ) # handle the request
				res.headers.append( "Content-Length: {0}".format( len( res.body ) ) )
				response = bytes( _RESPONSE.format( res.code, res.message, '\n'.join( res.headers ), res.body ), "utf-8" )
				print( response )
				c_sock.send( response ) # write response
			except Exception as e:
				print_exception( e )
			finally:
				c_sock.close();
				collect();
				print( "------", mem_free() )
	finally:
		if sock: sock.close();
		print( "good bye!" )

def __parseRequest( c_sock ):
	line = c_sock.readline()
	req = type( "",(),[] )
	if line:
		meth, path, ver = line.decode().split( ' ' )
		req.method, req.path, req.version, req.params, req.headers = meth, path, ver, {}, {}
		if path.find( '?' ) != -1 :
			req.path, params = path.split( '?' )
			for param in params.split( "&" ):
				name, val = param.split( '=' )
				req.params[ name ] = val
	line = c_sock.readline()
	while line and len( line ) > 2:
		line = line.decode().strip( '\n\r' )
		idx = line.find( ':' )
		req.headers[ line[:idx] ] = line[idx:]
		line = c_sock.readline()
	return req;

class RequestProcessor:

	_ERR_MSG = "Server Error"
	_ERR_RES = "Request: {0}\nError:{1}"

	def __init__( self ):
		self.handlers = []

	def register( self, regexp ):
		def gethandler( f ):
			self.handlers.insert( 0, ( compile( regexp ), f ) )
			return f
		return gethandler;

	def process( self, req ):
		if req:
			res = type( "",(),[] )
			res.code, res.message = 200, "OK"
			res.headers = []
			try:
				for pattern, handler in self.handlers:
					if pattern.match( req.path ):
						handler( req, res );
						break;
			except Exception as e:
				print_exception( e )
				res.code, res.message, res.body = 500, RequestProcessor._ERR_MSG, RequestProcessor._ERR_RES.format( req , e )
			return res;

req_processor = RequestProcessor()

@req_processor.register( "." )
def default_handler( request, response ):
	response.code, response.message = 404, "Not Found"
	response.body = "Micropython server\nResource: {0}\nMethod: {1}\nNot Found!!!".format( request.path, request.method )

@req_processor.register( "/algo" )
def algo_handler( request, response ):
	response.headers.append( "Content-Type: text/html" );
	response.body = "<html><body><h3>Resource: {0}\nMethod: {1}\nWas Found!!!<h3></html>".format( request.path, request.method )

@req_processor.register( "/led" )
def led_handler( request, response ):
	from machine import Pin
	pin = Pin( 5, Pin.OUT )
	pin.value( request.params[ "value" ] == "on" )
	response.headers.append( "Cache-Control: no-cache" )
	response.body = "Led is turned {0}".format( request.params[ "value" ] )
