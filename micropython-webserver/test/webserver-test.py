import unittest
from webserver.server import Server

class TestServer( unittest.TestCase ):

    def test_start( self ):

        server = Server( 8080 )
        server.start()


if __name__ == '__main__':
    unittest.main()