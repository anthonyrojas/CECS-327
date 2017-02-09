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

string requestReply(int sock, string message)
{
    char buffer[BUFFER_LENGTH];
    string reply;
    int count = send(sock, message.c_str(), message.size(), 0);
    if (count > 0)
    {
        usleep(1000);
        do {
            count = recv(sock, buffer, BUFFER_LENGTH-1, 0);
            buffer[count] = '\0';
            reply += buffer;
        }while (count ==  BUFFER_LENGTH-1);
    }
    return buffer;
}

int request(int sock, string message)
{
    char buffer[BUFFER_LENGTH];
    string reply;
    return send(sock, message.c_str(), message.size(), 0);
}

string reply(int sock)
{
    string strReply;
    int count;
    char buffer[BUFFER_LENGTH];
    usleep(10000);
    do {
        count = recv(sock, buffer, BUFFER_LENGTH-1, 0);
        buffer[count] = '\0';
        strReply += buffer;
    }while (count ==  BUFFER_LENGTH-1);
    return strReply;
}
/*! \fn int responseToPort(string response)
    \brief Returns server response parsed into Port
    \param string response from server
*/
int responseToPort(string response) {
    int parenIndex = static_cast<int>(response.find("("));
    string parsedIP, strReply;
    uint16_t a, b, c, d, e, f, first,second;

    response = response.substr(parenIndex+1,static_cast<int>(response.size()));
    int responseSize = static_cast<int>(response.find(")"));
    replace(response.begin(), response.end(), ',', '.');
    parsedIP = response.substr(0,responseSize);
    sscanf(parsedIP.c_str(), "%hu.%hu.%hu.%hu.%hu.%hu.", &a, &b, &c, &d, &e, &f);
    first = e << 8;
    return first | f;
}

/*! \fn string responseToIp(string response)
    \brief Returns server response parsed into Buffer
    \param string response from server
*/
string responseToIp(string response) {
    int parenIndex = static_cast<int>(response.find("("));
    string parsedIP, strReply;
    int a1,a2,a3,a4;
    char buffer[30];

    response = response.substr(parenIndex+1,static_cast<int>(response.size()));
    int responseSize = static_cast<int>(response.find(")"));
    replace(response.begin(), response.end(), ',', '.');
    parsedIP = response.substr(0,responseSize);
    sscanf(parsedIP.c_str(), "%d.%d.%d.%d", &a1, &a2, &a3, &a4 );
    sprintf(buffer, "%d.%d.%d.%d",a1,a2,a3,a4);
    return buffer;
}
/*! \fn int PASV(int sockpi)
    \brief Establishes a passive connection with the server
    \param sockpi socket to the main server
*/
int PASV(int sockpi) {
    string strReply = requestReply(sockpi, "PASV\r\n");
	cout << strReply << endl;
    return createConnection(responseToIp(strReply),responseToPort(strReply));
}
/*! \fn void LIST(int sockpi)
    \brief Establishes a passive connection with the server
    \brief Connects to the dtp server
    \param sockpi socket to the main server
*/
void LIST(int sockpi) {
  int sockdtp = PASV(sockpi);
  request(sockpi, "LIST /\r\n");
  cout << "Server response: " << reply(sockpi) << endl;
  cout << "DTP response:" << endl << reply(sockdtp) << endl;
  request(sockdtp,"CLOSE \r\n");
  cout << "Server response: " << reply(sockpi) << endl;
}
/*! \fn void RETR(int sockpi)
    \brief Returns selected file arguemnt
    \param sockpi socket to the main server
*/
void RETR(int sockpi) {
  string filename;
  cin >> filename;
  int sockdtp = PASV(sockpi);
  request(sockpi, "RETR " + filename + "\r\n");
  string strReply =  reply(sockpi);
  std::size_t found = strReply.find("550");
cout << "File name: " << filename.c_str() << endl;
  if(found!=string::npos){
    cout << "Error: " << strReply << endl;
    return;
  }
  cout << "Server response: " << strReply << endl;

  ofstream myfile(filename.c_str());
  myfile << reply(sockdtp);
  cout << "File: " << filename << " sucessfully downloaded!" << endl << endl;
  request(sockdtp,"CLOSE \r\n");
  cout << "Server response: " << reply(sockpi) << endl;
}
/*! \fn void QUIT(int sockpi)
    \brief Ends the connection with the server
    \param sockpi socket to the main server
*/
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

    strReply = requestReply(sockpi, "PASS asa@asas.com\r\n");
    cout << strReply << endl;
    cout << reply(sockpi) << endl;//230

    while (true) {
	cout << "Please enter a command: (ls,get <filename>,quit)" << endl;
        cin >> myinput;
        if (myinput == "ls") {
            LIST(sockpi);
        } else if (myinput == "get") {
            RETR(sockpi);
        } else if(myinput == "quit") {
            QUIT(sockpi);
            return 0;
        } else {
            cout <<"Please enter a command(ls,get <filename>,quit): Once, More..."<< endl;
        }
    }
}
