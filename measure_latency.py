import sys
import time
import socket
import subprocess


def ping(host_ip):
    cmd = 'ping -c 1 '+host_ip
    p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    out, err = p.communicate()
    #print out
    out = out.split('\n')
    for line in out:
        if ('64 bytes from' in line) and ('time=' in line):
            #print line
            line = line.split()
            t = line[-2]
            t = t[5:]
            return float(t)

    return None
    

def main():
    servers = []
    fin = open('gigapaxos.properties', 'r')
    for line in fin:
        if line[:6] != "active":
            continue
        line = line[:-1]
        socketAddress = line.split("=")[1]
        servers.append(socketAddress.split(":")[0])
    fin.close()
    print servers
    
    fout = open('latency','w+')
    for host in servers:
        arr = []
        for i in range(10):
            start = time.time()
            p = ping(host)
            if p is None:
                print "can not ping "+host
                break
            arr.append(p)
            eclapsed = time.time() - start
            time.sleep(1)
        fout.write(str(sum(arr)/len(sum))+" ")
    fout.close()


if __name__ == "__main__":
    main()
