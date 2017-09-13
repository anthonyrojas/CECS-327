/****************************************
Anthony Rojas
ID# 0118191338
CECS 327
Fall 2017
****************************************/
#include <iostream>    //cout
#include <string>
#include <stdio.h> //printf
#include <stdlib.h>
#include <string.h>    //strlen
#include <sys/socket.h>    //socket
#include <arpa/inet.h> //inet_addr
#include <netinet/in.h>
#include <sys/types.h>
#include <unistd.h>
#include <netdb.h>
#include <algorithm>
#include <fstream>

#define BUFFER_LENGTH 2048
#define WAITING_TIME 100000

using namespace std;

int create_connection(string host, int port)
{
    int s;
    struct sockaddr_in saddr;
    
    memset(&saddr,0, sizeof(saddr));
    s = socket(AF_INET,SOCK_STREAM,0);
    saddr.sin_family=AF_INET;
    saddr.sin_port= htons(port);
    
    int a1,a2,a3,a4;
    if (sscanf(host.c_str(), "%d.%d.%d.%d", &a1, &a2, &a3, &a4 ) == 4)
    {
        cout << "by ip";
        saddr.sin_addr.s_addr =  inet_addr(host.c_str());
    }
    else {
        cout << "by name";
        hostent *record = gethostbyname(host.c_str());
        in_addr *addressptr = (in_addr *)record->h_addr;
        saddr.sin_addr = *addressptr;
    }
    if(connect(s,(struct sockaddr *)&saddr,sizeof(struct sockaddr))==-1)
    {
        perror("connection fail");
        exit(1);
        return -1;
    }
    return s;
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
    
    usleep(WAITING_TIME);
    do {
        count = recv(s, buffer, BUFFER_LENGTH, 0);
        buffer[count] = '\0';
        strReply += buffer;
    }while (count ==  BUFFER_LENGTH);
    return strReply;
}

string request_reply(int s, string message)
{
	if (request(s, message) > 0)
    {
    	return reply(s);
	}
	return "";
}

int PASV(string host, int sockpi){
	string strReply = request_reply(sockpi, "PASV \r\n");
	int port, sock;
	if(strReply.find("227") != string::npos){
		cout << "Passive Mode entered" << endl;
		string ip = strReply.substr(strReply.find('(') + 1, (strReply.find(')') - strReply.find(')') - 1));
		int a1, a2, a3, a4, a5, a6;
		sscanf(ip.c_str(), "%d,%d,%d,%d,%d,%d", &a1, &a2, &a3, &a4, &a5, &a6);
		port = ((a5<< 8) | a6);
        cout << port << endl;
        sock = create_connection(host, port);
	}
	return sock;
}

void execute_command(string host, string command, string addInfo, int sockpi){
	string full_command = command + " " + addInfo + "\r\n";
    int sock = PASV(host, sockpi);
    string strReply;
    strReply = request_reply(sockpi, full_command);
    cout << strReply << endl;
	if(command.find("RETR") != string::npos){
        if(strReply.find("550") != string::npos){
            request(sock, "CLOSE \r\n");
            return;
        }
        else{
            ofstream file_download(addInfo.c_str());
            file_download << reply(sock);
        }
    }
    cout << reply(sock) << endl;
    
    request(sock, "CLOSE \r\n");

	cout << reply(sockpi) << endl;
}

void QUIT(int sockpi){
	cout << request_reply(sockpi, "QUIT \r\n") << endl;
}

int main(int argc , char *argv[])
{
    int sockpi;
    string strReply;
    string host = "130.179.16.134";
    //TODO  arg[1] can be a dns or an IP address.
    if (argc > 2)
        sockpi = create_connection(argv[1], atoi(argv[2]));
    if (argc == 2)
        sockpi = create_connection(argv[1], 21);
    else
        sockpi = create_connection("130.179.16.134", 21);
    strReply = reply(sockpi);
    cout << strReply  << endl;
    
    
    strReply = request_reply(sockpi, "USER anonymous\r\n");
    //TODO parse srtReply to obtain the status. 
	// Let the system act according to the status and display
    // friendly message to the user 
	// You can see the ouput using std::cout << strReply  << std::endl;
    cout << strReply << endl;

    /*if(strReply.find("331") != string::npos){
    	cout << "331 Username accepted" << endl;
    }*/
    
    strReply = request_reply(sockpi, "PASS email@gmail.com\r\n");
    cout << strReply << endl;
    //cout << reply(sockpi) << endl; //230
    //TODO implement PASV, LIST, RETR. 
    // Hint: implement a function that set the SP in passive mode and accept commands.
    string input;
    
    cout << "Enter a command: LIST, RETR, QUIT" << endl;
    cin >> input;
    while(input != "QUIT"){
    	if(input == "LIST"){
            string dir;
            cout << "Enter a directory: " << endl;
            cin >> dir;
            execute_command(host, "LIST", dir, sockpi);
    	}
    	else if(input == "RETR"){
            string filename;
            cout << "Enter a filename: ";
            cin >> filename;
            execute_command(host, "RETR", filename, sockpi);
    	}
    	else if(input == "QUIT"){
    		QUIT(sockpi);
    		return 0;
    	}
    	cout << "Enter a command: LIST, RETR, QUIT" << endl;
    	cin >> input;
    }
    QUIT(sockpi);
    return 0;
}
