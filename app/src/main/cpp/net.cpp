//
// Created by zoson on 17-2-7.
//

#include <sys/socket.h>
#include "include/net/net.hpp"
#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<errno.h>
#include<sys/types.h>
#include<netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <android/log.h>

namespace zoson
{

NetClient::NetClient() { }
NetClient::~NetClient() { }
bool NetClient::connectServer(ConnInfo connInfo)
{
    mConnInfo = connInfo;
    createConnection();
    return false;
}
bool NetClient::sendData(const Data* data)
{
    if(mSockfd<0)return false;
    unsigned char* buf = data->buf;
    int size = data->size;
    if( send(mSockfd, buf, size, 0) < 0)
    {
        __android_log_print(ANDROID_LOG_INFO,"Net","send msg error: %s(errno: %d)\n", strerror(errno), errno);
        return false;
    }
    return true;
}

bool NetClient::recData(Data* data)
{
    if(mSockfd<0)return false;
    data->buf = new unsigned char[4096];
    if((data->size = recv(mSockfd, data->buf, 4096,0)) == -1)
    {
        __android_log_print(ANDROID_LOG_ERROR,"NetCLient","recv error");
        return false;
    }

    return true;
}

void NetClient::disConnect()
{
    close(mSockfd);
    mSockfd = -1;
}

int NetClient::createConnection()
{
    __android_log_print(ANDROID_LOG_INFO,"NetClient","createConnection");
    struct sockaddr_in    servaddr;
    if( (mSockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0){
        __android_log_print(ANDROID_LOG_INFO,"NetCLient","create socket error: %s(errno: %d)\n", strerror(errno),errno);
        return -1;
    }
    memset(&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_port = htons(mConnInfo.server_post);
    if( inet_pton(AF_INET, mConnInfo.server_addr.c_str(), &servaddr.sin_addr) <= 0){
        __android_log_print(ANDROID_LOG_INFO,"NetClient","inet_pton error for %s\n",mConnInfo.server_addr.c_str());
        return -1;
    }
    if( connect(mSockfd, (struct sockaddr*)&servaddr, sizeof(servaddr)) < 0){
        __android_log_print(ANDROID_LOG_INFO,"NetClient","connect error: %s(errno: %d)\n",strerror(errno),errno);
        return -1;
    }

    return 0;
}


}