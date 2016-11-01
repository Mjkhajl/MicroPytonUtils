import socket as socket

running = False

def start( host, port):

    CONTENT = """\
HTTP/1.0 200 OK

Hello #{} from MicroPython!

"""
    addr = (host, port)
    print("starting: ", addr )
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    
    try:
        s.bind(addr)
        s.listen(5)
        counter = 0
        while running:
            res = s.accept()
            client_s = res[0]
            client_addr = res[1]
            print("Client address:", client_addr)
            print("Client socket:", client_s)
            print("Request:")
            req = client_s.recv(4096)
            print(req)
            client_s.send(bytes(CONTENT.format(counter), "ascii"))
            client_s.close()
            parts = req.decode('ascii').split(' ')
            if parts[1] == '/exit':
                break
            counter += 1
    except Exception as e:
        print("Error ", e.message, e.code )
    print( "closing..." + addr )
    s.close();        
