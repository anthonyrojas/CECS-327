/*********************************************
*Anthony Rojas
*ID# 011819338
*Assignment 1
*CECS 327 Sec 05
**********************************************/
#include <iostream>    //cout
#include <fstream>
#include <string>
#include <algorithm>
#include <stdio.h> //printf
#include <stdlib.h>
#include <string.h>    //strlen
#include <sys/socket.h>    //socket
#include <arpa/inet.h> //inet_addr
#include <netinet/in.h>
#include <sys/types.h>
#include <unistd.h>
#include <netdb.h>

using namespace std;

#define BUFFER_LENGTH 2048

int createConnection(string host, int port) {
	int sock;
	struct sockaddr_in sockaddr;
	memset(&sockaddr,0, sizeof(sockaddr));
	sock = socket(AF_INET,SOCK_STREAM,0);
	sockaddr.sin_family=AF_INET;
	sockaddr.sin_port= htons(port);
	int a1,a2,a3,a4;
	if (sscanf(host.c_str(), "%d.%d.%d.%d", &a1, &a2, &a3, &a4 ) == 4) {
        	sockaddr.sin_addr.s_addr =  inet_addr(host.c_str());
    	} else {
        	cout << "by name";
        	hostent * record = gethostbyname(host.c_str());
        	in_addr * addressptr = (in_addr *) record->h_addr;
        	sockaddr.sin_addr = *addressptr;
    	}
	if(connect(sock,(struct sockaddr *)&sockaddr,sizeof(struct sockaddr))==-1) {
        	perror("connection fail");
        	exit(1);
        	return -1;
	}
	return sock;
}

int request(int sock, string message)
{
    return send(sock, message.c_str(), message.size(), 0);
}

string reply(int s)
{
    string strReply;
    int count;
    char buffer[BUFFER_LENGTH+1];
    usleep(10000);
    do {
        count = recv(s, buffer, BUFFER_LENGTH, 0);
        buffer[count] = '\0';
        strReply += buffer;
    }while (count ==  BUFFER_LENGTH);
    return strReply;
}

string requestReply(int s, string message)
{
	if(request(s, message) > 0){
		return reply(s);
	}
	return "";
}

int responseToPort(string r) {
	int i = static_cast<int>(r.find("("));
	string parsedIP, strReply;
	uint16_t a, b, c, d, e, f, first,second;
	r = r.substr(i+1,static_cast<int>(r.size()));
	int rSize = static_cast<int>(r.find(")"));
	replace(r.begin(), r.end(), ',', '.');
	parsedIP = r.substr(0,rSize);
    	sscanf(parsedIP.c_str(), "%hu.%hu.%hu.%hu.%hu.%hu.", &a, &b, &c, &d, &e, &f);
    	first = e << 8;
    	return first | f;
}


string responseToIp(string r) {
    int i = static_cast<int>(r.find("("));
    string parsedIP, strReply;
    int a1,a2,a3,a4;
    char buffer[30];
    r = r.substr(i+1,static_cast<int>(r.size()));
    int responseSize = static_cast<int>(r.find(")"));
    replace(r.begin(), r.end(), ',', '.');
    parsedIP = r.substr(0,rSize);
    sscanf(parsedIP.c_str(), "%d.%d.%d.%d", &a1, &a2, &a3, &a4 );
    sprintf(buffer, "%d.%d.%d.%d",a1,a2,a3,a4);
    return buffer;
}

int PASV(int sockpi) {
    string strReply = requestReply(sockpi, "PASV\r\n");
	cout << strReply << endl;
	return createConnection(responseToIp(strReply),responseToPort(strReply));
}

void LIST(int sockpi) {
  int sockdtp = PASV(sockpi);
  request(sockpi, "LIST /\r\n");
  cout << reply(sockpi) << endl;
  cout << endl << reply(sockdtp) << endl;
  request(sockdtp,"CLOSE \r\n");
  cout << reply(sockpi) << endl;
}

void RETR(int sockpi) {
  string filename;
  cin >> filename;
  int sockdtp = PASV(sockpi);
  request(sockpi, "RETR " + filename + "\r\n");
  string strReply =  reply(sockpi);
  std::size_t filefound = strReply.find("550");
  if(filefound!=string::npos){
    cout << "Error: " << strReply << endl;
    return;
  }
  cout <<strReply << endl;

  ofstream myfile(filename.c_str());
  myfile << reply(sockdtp);
  cout <<filename << " downloaded" << endl << endl;
  request(sockdtp,"CLOSE \r\n");
  cout <<reply(sockpi) << endl;
}

void QUIT(int sockpi) {
    cout << requestReply(sockpi, "QUIT\r\n");
}
int main(int argc , char *argv[])
{
    int sockpi,sockdtp;
    string strReply;
    string myinput;
    //TODO  arg[1] can be a dns or an IP address using gethostbyname.
    if (argc > 2){
        sockpi = createConnection(argv[1], atoi(argv[2]));
    }
    if (argc == 2){
        sockpi = createConnection(argv[1], 21);
    } else {
        sockpi = createConnection("130.179.16.134", 21);
    }
    strReply = reply(sockpi);
    cout << strReply  << endl;

    strReply = requestReply(sockpi, "USER anonymous\r\n");
    cout << strReply  << endl;

    strReply = requestReply(sockpi, "PASS anthony.rojas@student.csulb.edu\r\n");
    cout << strReply << endl;
    cout << reply(sockpi) << endl;//230

    while (true) {
	cout << "Enter a command: pasv, list,retr <filename>,quit" << endl;
        cin >> in;
	if (in == "pasv"){
		PASV(sockpi);
	}        
	else if (in == "list") {
            LIST(sockpi);
        } else if (in == "retr") {
            RETR(sockpi);
        } else if(in == "quit") {
            QUIT(sockpi);
            return 0;
        } else {
            cout <<"Enter a command: pasv,list,retr <filename>,quit"<< endl;
        }
    }
}
