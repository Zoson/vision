//
// Created by zoson on 17-2-7.
//

#ifndef VISION_NET_HPP_H
#define VISION_NET_HPP_H

#include <string>
#include <sys/socket.h>
using namespace std;

namespace zoson
{
class NetClient;

class Data
{
public:
    friend zoson::NetClient;
    Data(unsigned char* b,int s):buf(b),size(s){}
    Data():buf(NULL),size(0){}
    ~Data(){delete buf;}
    unsigned char * buf;
    int size;
};

struct ConnInfo
{
    string server_addr;
    int server_post;
    string cookies;
};

class NetClient
{
public:
    NetClient();
    ~NetClient();
    bool connectServer(ConnInfo connInfo);
    bool sendData(const Data *data);
    bool recData(Data *data);
    string getTag(){return "NetClient";}
    void disConnect();
protected:
    int createConnection();
private:
    ConnInfo mConnInfo;
    int mSockfd;
};



}


#endif //VISION_NET_HPP_H
