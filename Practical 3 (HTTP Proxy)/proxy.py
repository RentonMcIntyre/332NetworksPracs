#!/usr/bin/env python
import socket, sys
try:
    from thread import *
except ImportError:
    from _thread import *

port = 55555 #this will be our port on which we listen
buffer_size = 8192
numConns = 10
def start():
    try:
        print("Initializing server socket... done")
        
        server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        print("Binding sockets...")
        server.bind(('', port))
        print("Done")
        server.listen(numConns)
        print("Server Started Successfully [%d]\n" % (port))
    except Exception as e:
        print("Something failed")
        sys.exit(2)
    
    while 1:
        try: 
            conn, client = server.accept()
            data = conn.recv(buffer_size)
            start_new_thread(conn_request, (conn, data, client))
            
        except KeyboardInterrupt:
            server.close()
            print("\nServer shutting down")
            sys.exit(1)
            
    server.close()
      
      
def conn_request(conn, data, client):
    try:
        first_line = data.split('\n')[0]
        url = first_line.split(' ')[1]
        http_pos = url.find("://")
        
        if(http_pos==-1):
            temp = url
        else:
            temp = url[(http_pos+3):]
            
        port_pos = temp.find(":")
        webserver_pos = temp.find("/")
        if webserver_pos == -1:
            webserver_pos = len(temp)
        
        webserver = ""
        port = -1
        if(port_pos == -1 or webserver_pos < port_pos):
            exPort = 80
            webserver = temp[:webserver_pos]
        else:
            exPort = int((temp[(port_pos+1):])[:webserver_pos-port_pos-1])
            
        proxy(webserver, exPort, conn, client, data)
    except Exception as e:
        pass
        
def proxy(webserver, exPort, conn, client, data):
    try:  
        server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server.connect((webserver, exPort))
        server.send(data)
        
        while 1:  
            response = server.recv(buffer_size)
            if(len(response) > 0 ):
                conn.send(response)
                notif = float(len(response))
                notif = float(notif/1024)
                notif = "%.3s" % str(notif)
                notif = "%s KB" % (notif)
                print("Request done: %s => %s <=" % (str(client[0]), str(notif)))
            else:
                break
                      
        server.close()
        conn.close()
    except socket.error as e:
        server.close()
        conn.close()
        sys.exit(1)
    except KeyboardInterrupt:
        server.close()
        conn.close()
        sys.exit(2)
                
start()