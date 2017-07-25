
# HTTP 协议
## curl 安装
### windows 下安装 curl
　　从[ curl官网 ](https://curl.haxx.se/download.html)下载 windows 版本的 cab 文件，解压后根据 cpu 架构选择不同的文件夹内的文件（ `curl.exe` 、 `libcurl.dll` 、 `libcurl.exe` 、 `libcurl.lib` ）复制到 C 盘的 Windows 文件夹内即可使用。
### linux 下安装 curl 
　　用过的虚拟机都有 curl ，暂时未尝试安装。
## curl参数
　　通过 `curl --help` 可以查看参数及其作用，常用的参数有 `-i` (显示请求头信息以及网页源代码)、 `-d` (post数据)、 `-H` (发送头信息)。
## 抓包查看网盘 Web 端登录失败 / 成功 后返回什么
### 登录失败
　　通过 `curl -i http://127.0.0.1:8080/auth/api/nameLogin -d {"name":"admin","password":"123456"}` 命令登录网盘，返回结果为
```
Date: Tue, 25 Jul 2017 06:43:29 GMT
Server: xServer/2.0
X-RequestId: lzIPQT9kTdw
X-Cost: 2
Content-Type: text/plain;charset=UTF-8
Content-Length: 53
{"stat":"ERR_INVALID_JSON","errText":"?𽐿?JSON涓?}
```

提示 非法JSON串，在 `JsonProtocol` 的 `parseJsonBody` 方法中打上断点， dubug 发现传进来的 body 是 `{name:admin,password:123456}` ，将引号 `"` 转义后成功发送请求：

```
> curl -i http://127.0.0.1:8080/auth/api/nameLogin -d {\"name\":\"admin\",\"password\":\"123456\"}
HTTP/1.1 200 OK
Date: Tue, 25 Jul 2017 06:47:20 GMT
Server: xServer/2.0
X-RequestId: oz2js1CaQJU
X-Cost: 16
Content-Type: text/plain;charset=UTF-8
Content-Length: 79

{"stat":"ERR_INVALID_PASSWORD","errText":"瀵𷐿𽳘?𼀿,?╀?灏𽀿ì娆℃𻀿锛𼐵"}
```

提示密码错误，后边的字符在浏览器开发者工具中查看，内容为 “密码错误,剩余尝试次数：5”。
#### 一个问题
　　发现一个小问题，如果用一个不存在的账户登录，提示的内容为“{"stat":"ERR_NO_SUCH_ACCOUNT","errText":"帐号或密码不正确"}”。可能会有安全风险。
### 登录成功
　　使用 `curl -i http://127.0.0.1:8080/auth/api/nameLogin -d {\"name\":\"admin\",\"password\":\"123qwe\"}` 命令登录，结果如下：

```
> curl -i http://127.0.0.1:8080/auth/api/nameLogin -d {\"name\":\"admin\",\"password\":\"123qwe\"}
HTTP/1.1 200 OK
Date: Tue, 25 Jul 2017 06:53:30 GMT
Server: xServer/2.0
X-RequestId: Vxd_N1rcSpw
X-Cost: 23
Set-Cookie: token=eRQFy5ItRhA@1;Path=/
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Content-Type: text/plain;charset=UTF-8
Content-Length: 37

{"stat":"OK","token":"eRQFy5ItRhA@1"}
```

返回结果为登录成功，并且返回了 `token`。
## curl 通过 `HTTP Range` 下载部分字节
　　Range 头域可以请求实体的一个或者多个子范围， Range 的值为0表示第一个字节，也就是 Range 计算字节数是从0开始的。
　　使用 curl 命令下载 [http://mirrors.163.com/ubuntu-releases/17.04/ubuntu-17.04-server-amd64.iso](http://mirrors.163.com/ubuntu-releases/17.04/ubuntu-17.04-server-amd64.iso) 的第 0 字节到第 9 字节（一共 10 个字节），结果如下：
```
C:\Users\Alone>curl -i http://mirrors.163.com/ubuntu-releases/17.04/ubuntu-17.04-server-amd64.iso -H "Range:bytes=0-9"

HTTP/1.1 206 Partial Content
Server: nginx
Date: Tue, 25 Jul 2017 07:08:45 GMT
Content-Type: application/octet-stream
Content-Length: 10
Connection: keep-alive
Last-Modified: Wed, 12 Apr 2017 03:20:47 GMT
ETag: "58ed9d0f-2ad00000"
Content-Range: bytes 0-9/718274560

Eaaa惇aa
```
　　206 状态码表示“客户发送了一个带有Range头的GET请求，服务器完成了它”。
　　有些服务器可能不支持 `HTTP Range`，可以看到网易的镜像是支持的，从响应头的 `Content-Length: 10` 可以看出，请求了 10 个字节， `Content-Range: bytes 0-9/718274560` 表示请求的部分为 0 到 9 共 10 个字节的数据，请求的资源总大小为 718274560 字节，也就是 685 M。
### `HTTP Range` 的应用
　　多线程下载文件、断点续传、视频缓冲等。
## 网盘文件上传
### 网盘 Web 端查看上传过程
1. 向 xServer 发送 POST 请求 `requestUpload`，请求上传文件，服务端返回存储结点信息、本次上传 ID 以及存储结点中是否存在该文件；
2. 向存储服务发送 OPTIONS 请求 `formUpload`，传过去上传 ID，获取是否允许上传；
3. 向存储服务发送 POST 请求 `formUpload`，通过 `multipart/form-data` 的方式上传文件，返回结果为 `partCommitId`，`partCommitId` 用来记录分块上传的 ID，如果因为网络等原因没有上传完成，下次可以根据 `partCommitId` 继续上传；
4. 向 xServer 发送 POST 请求 `commitUpload`，传过去上传 ID 、`partCommitId` 以及文件的信息，xServer 将新上传的文件信息更新到数据库中。
### HTTP Multipart
　　HTTP 协议是以 ASCII 码传输，建立在 TCP/IP 协议智商的应用层规范，规范把 HTTP 请求分为三部分：状态行、请求头、响应主体。POST 提交的数据必须放在消息主体中，也就是 Request-Body 中，但是没有规定要使用什么编码方式，只要客户端和服务端约定好使用的编码方式即可。
#### POST Content-Type
　　POST 发送请求时，请求头中都有一个属性叫做 `Content-Type`，表明了这个 POST 请求提交数据的方式，通常有以下几种：

- text/html ： HTML格式
- text/plain ：纯文本格式
- text/xml ：  XML格式
- image/gif ：gif图片格式
- image/jpeg ：jpg图片格式
- image/png：png图片格式
- application/xhtml+xml ：XHTML格式
- application/xml     ： XML数据格式
- application/atom+xml  ：Atom XML聚合格式
- application/json    ： JSON数据格式
- application/pdf       ：pdf格式 
- application/msword  ： Word文档格式
- application/octet-stream ： 二进制流数据（如常见的文件下载）
- application/x-www-form-urlencoded ： form表单默认，form表单数据被编码为key/value格式发送到服务器
- multipart/form-data ： 需要在表单中进行文件上传时，就需要使用该格式
#### multipart/form-data
　　`multipart/form-data` 上传文件时 `Content-Type` 中的内容是这样的： `multipart/form-data; boundary=---------------------------5766576725528`， `boundary` 用于分割不同的字段，为了避免与正文内容重复，在消息主体中字段被分开不同的部分，用 `--boundary` 开始，下一行是本部分的信息是什么，再空一行然后是字段内的信息的值，而文件的信息在最后，在整个消息体的最后用 `--boundary--` 表示消息体结束。
　　下面是我在 Web 端上传文件时的 POST 消息体中的数据：
```
-----------------------------5766576725528
Content-Disposition: form-data; name="id" // 文件id

WU_FILE_5
-----------------------------5766576725528
Content-Disposition: form-data; name="name" // 文件名字

1984.txt
-----------------------------5766576725528
Content-Disposition: form-data; name="type" // 文件种类

text/plain
-----------------------------5766576725528
Content-Disposition: form-data; name="lastModifiedDate" // 最后修改时间

Mon Jul 03 2017 23:05:49 GMT+0800
-----------------------------5766576725528
Content-Disposition: form-data; name="size" // 文件大小

348509
-----------------------------5766576725528
Content-Disposition: form-data; name="relativepath" // 相对路径


-----------------------------5766576725528
Content-Disposition: form-data; name="file"; filename="1984.txt" // 文件的数据流
Content-Type: text/plain

±¾ÊéÀ´×Ôwww.abada.cnÃâ·ÑtxtÐ¡ËµÏÂÔØÕ¾
¸ü¶à¸üÐÂÃâ·Ñµç×ÓÊéÇë¹Ø×¢www.abada.cn   
¡¶£±£¹£¸£´¡· ×÷Õß¼ò½é 

    ±¾Êé¡ª¡ª¡¶1984¡·ÓÉÇÇÖÎ¡¤°ÂÍþ¶û(George Orwell)ÓÚ
1948ÄêÐ´¾Í¡£ËûµÄÁíÒ»²¿ÖØÒªÐ¡ËµÊÇ¡¶¶¯ÎïÅ©³¡¡·(Animal Farm)
    °ÂÍþ¶ûÔ­Ãû°£Àï¿Ë¡¤°¢Éª¡¤²¼À³¶û(Eric Arthur Blair)£¬
    // 省略
    
-----------------------------5766576725528--
```
　　可以看到消息体中标明了文件的各种信息。
### curl 上传文件到网盘
#### `requestUpload`
``` bash
> curl -i http://127.0.0.1:8080/fs/api/requestUpload -d {\"parent\":0,\"name\":\"test.txt\",\"size\":4,\"overWrite\":true,\"token\":\"P4_Im4mbRcg@1\"}
HTTP/1.1 200 OK
Date: Tue, 25 Jul 2017 09:11:04 GMT
Server: xServer/2.0
X-RequestId: oZGi3OnKSWQ
upload_rate_limit: nullk
X-Cost: 133
Content-Type: text/plain;charset=UTF-8
Content-Length: 206

{"stat":"OK","nodes":[{"addr":"http://192.168.1.180:9091/unode/stor","cid":"1"}],"fileUploadId":"1.XUFccvCfTaU.eyJhaSI6Inh4eCIsImJpIjoxLCJjaSI6MSwiY3QiOjE1MDA5NDUwMjMsInN6Ijo0fQ.3830246057","existed":false}
```
#### `formUpload` OPTIONS
``` bash
> curl -i http://192.168.1.180:9091/unode/stor/formUpload?fileUploadId=1.XUFccvCfTaU.eyJhaSI6Inh4eCIsImJpIjoxLCJjaSI6MSwiY3QiOjE1MDA5NDUwMjMsInN6Ijo0fQ.3830246057 -X OPTIONS
HTTP/1.1 200 OK
Server: nginx
Date: Tue, 25 Jul 2017 01:15:34 GMT
Content-Type: text/plain;charset=UTF-8
Content-Length: 13
Connection: keep-alive
X-RequestId: og0H1rKSShw
X-Cost: 1
Access-Control-Allow-Origin: *
Access-Control-Allow-Headers: Content-Type, X-Device

{"stat":"OK"}
```
#### `formUpload` POST
``` bash
> curl -i -X POST http://192.168.1.180:9091/unode/stor/formUpload?fileUploadId=1.XUFccvCfTaU.eyJhaSI6Inh4eCIsImJpIjoxLCJjaSI6MSwiY3QiOjE1MDA5NDUwMjMsInN6Ijo0fQ.3830246057 -F "size=4" -F "file=@D:\Documents\Desktop\test.txt"
HTTP/1.1 100 Continue

HTTP/1.1 200 OK
Server: nginx
Date: Tue, 25 Jul 2017 01:52:39 GMT
Content-Type: text/plain;charset=UTF-8
Content-Length: 250
Connection: keep-alive
X-RequestId: SsrymqXfQhs
X-Cost: 33
Access-Control-Allow-Origin: *
Access-Control-Allow-Headers: Content-Type, X-Device

{"name":"test.txt","partCommitIds":["1.eyJjYyI6MzYzMjIzMzk5NiwiY2lkIjoxLCJjdCI6MTUwMDk0NzU1OSwiZm4iOiJiVW01RlBjQVJfWS0wLTQiLCJwbiI6MCwicHMiOjQsInMxIjoicVVxUDVjeXhtNlljVEFoejA1SHBoNWd2dTlNIiwidWkiOiJYVUZjY3ZDZlRhVSJ9.3821753257"],"size":4,"stat":"OK"}
```
#### `commitUpload`
``` bash
> curl -i http://127.0.0.1:8080/fs/api/commitUpload -d {\"fileUploadId\":\"1.XUFccvCfTaU.eyJhaSI6Inh4eCIsImJpIjoxLCJjaSI6MSwiY3QiOjE1MDA5NDUwMjMsInN6Ijo0fQ.3830246057\",\"partCommitIds\":\"1.eyJjYyI6MzYzMjIzMzk5NiwiY2lkIjoxLCJjdCI6MTUwMDk0NzU1OSwiZm4iOiJiVW01RlBjQVJfWS0wLTQiLCJwbiI6MCwicHMiOjQsInMxIjoicVVxUDVjeXhtNlljVEFoejA1SHBoNWd2dTlNIiwidWkiOiJYVUZjY3ZDZlRhVSJ9.3821753257\",\"parent\":0,\"name\":\"test.txt\",\"size\":4,\"overWrite\":true,\"token\":\"P4_Im4mbRcg@1\"}
HTTP/1.1 200 OK
Date: Tue, 25 Jul 2017 10:00:39 GMT
Server: xServer/2.0
X-RequestId: 1DlqfSBEQP0
X-Cost: 98
Content-Type: text/plain;charset=UTF-8
Content-Length: 50

{"stat":"ERR_FORBIDDEN","errText":"鏃犺闂潈闄?}
```
　　测试时这个测试服务器有问题，所有的用户上传文件都显示没有权限。
## HTTP 返回码 302 / 301
### 301 返回码
　　301 表示永久性转移，如果返回 301 状态码，那么以后浏览器请求数据时就不会再想原地址请求了，而是直接向新地址请求。
### 302 返回码
　　302 表示临时转移，浏览器每次请求时都会请求原地址。
## 浏览器输入地址到加载出网页经历了什么
1. 在浏览器地址栏中输入 `pan.cloudhua.com`，浏览器默认协议为 HTTP，向 `http://pan.cloudhua.com`发送请求，此时浏览器并不知道这个域名的 IP 地址以及端口号，因此要查询 IP 地址。查询 IP 地址时先从本机的 Host 文件中查找，如果没找到再去本机设置的 DNS 服务器查找，如果本机设置的 DNS 服务器没有找到，再向上一级 DNS 服务器发送请求，直到总的 DNS 服务器。
2. `http://pan.cloudhua.com` 接受到请求后，由于服务器的配置，进行了 302 跳转，跳转到 `https://pan.cloudhua.com`。Web 服务器默认监听请求端口是 80 端口， HTTPS 监听端口为 443，从响应头中看到，服务器又返回了 302 重定向，重定向到了 `https://pan.cloudhua.com/web/login.html` 登录页面。


