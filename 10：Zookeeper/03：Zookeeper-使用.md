### 使用Zookeeper

------

[TOC]

##### 01：Zookeeper 部署

- ZooKeeper 有两种运行模式：集群模式和单机模式。

###### 集群模式

1. 下载压缩包：下载完成后会得到一个文件名类似于：apache-zookeeper-3.7.1.tar.gz 的文件；
2. 解压，配置环境变量： 用**%ZK_HOME%** 代表该目录；
3. 配置 zoo.cfg 文件
   - 初次使用ZooKeeper，需要将%ZK_HOME%/conf目录下的zoo_sample.cfg 文件重命名为zoo.cfg，并且按照如代码下进行简单配置即可:
     - tickTime=2000
     - dataDir=/var/lib/zookeeper/
     - clientPort=2181
     - initLimit=5
     - syncLimit=2
     - server.1=IP1:2888:3888 （集群IP）
     - server.2=IP2:2888:3888
     - server.3=IP3:2888:3888
   - 在集群模式下，集群中的每台机器都需要感知到整个集群是由哪几台机器组成的，在配置文件中，可以按照这样格式的进行配置，每一行都代表一个机器
     - 配置：server.id=host:port:port
     - 其中，id被称为 Server ID，用来标识该机器在集群中的机器序号。同时在每台ZooKeeper机器上，我们都需要在数据目录(即dataDir参数指定的那个目录)下创建一个myid文件，该文件只有一行内容，并且是一个数字，即**对于应每台机器的Server ID数字**。
4. 创建 myid 文件
   - 在dataDir所配置的目录下，创建一个名为 myid 的文件，在该文件的第一行写上一个数字，和zoo.cfg中当前机器编号对应上。
5. 按照相同的步骤，为其他机器都配置上zoo.cfg和myid 文件。
6. 启动服务器；
   - 使用 %ZK_HOME%/bin 目录下的 zkServer.sh 脚本进行服务器的启动；
   - 若有报错，120行 echo -e，需要修改脚本；
7. 验证服务器；
   - telnet 127.0.0.1 2181 
     - mode：leader
   - **sh zkServer.sh start** 
     - 启动服务
   - sh zkServer.sh status
     - 检查服务状态
   - sh zkServer.sh stop
     - 关闭服务

###### 单机模式

- 配置 zoo.cfg 文件
  - tickTime=2000
  - dataDir=/var/lib/zookeeper/
  - clientPort=2181
  - initLimit=5
  - syncLimit=2
  - server.1=IP1:2888:3888 
- telnet 127.0.0.1 2181
  - Mode：standalone

###### 伪集群模式

- 一台机器上多个进程充当不同机器；

##### 02：Zookeeper 运行服务

- zk bin 目录下的可执行脚本
  - zkCleanup： 清理 ZooKeeper 历史数据，包括事务日志文件和快照数据文件；
  - zkCli：zk 的一个简易客户端；
  - zkEnv：设置zk的环境变量；
  - zkServer：zk服务器的启动、停止和重启脚本；

###### 停止服务

- sh zkServer.sh stop

##### 03：客户端脚本

- 连接指定zk服务器
  - sh zkCli.sh -server ip:port
  - sh zkCli.sh -server 192.168.1.7:2181

###### 创建

- create [-s] [-e] path  data acl
  - 创建一个zk节点
  - -s：顺序节点
  - -e：临时节点
  - 默认：持久节点
  - ACL：权限控制

###### 读取

- ls
  - 列出zk指定节点的所有子节点；
  - ls path [watch]
    - path：数据节点的节点路径；
- get
  - 获取ZooKeeper指定节点的数据内容和属性信息；
  - get path [watch]

###### 更新

- set
  - 更新指定节点的数据内容
  - set path data [version]
    - version：用于指定本次更新操作是基于ZNode的哪一个数据版本进行的；

###### 删除

- delete
  - 删除zk 上的指定节点；
  - delete path [version]
    - 删除某个节点时，该节点必须没有子节点存在；

##### 04：Java 客户端API 使用

- 导入 jar 包

  ```xml
  <dependency>
      <groupId>org.apache.zookeeper</groupId>
      <artifactId>zookeeper</artifactId>
      <version>3.7.1</version>
  </dependency>
  ```

- 创建会话

  - ZooKkeeper(String connectString，int sessionTimeout，Watcher watcher) ; 
    - connectString：ZK服务器列表，ip:port
      - Ip:port/根目录：指所有对zk的操作，都会基于这个根目录。
    - sessionTimeout：心跳超时时间；
    - canReadOnly：当一个zk 服务器与集群中过半其他机器失去联系时，是否希望该机机器继续支持只读模式；

- 创建节点

- 读取数据

- 更新数据

- 删除节点

- 检测节点

##### 05：zk开源客户端

###### ZkClient

- 依赖 jar 包

  - ```xml
    <dependencies>
        <dependency>
            <groupId>org,apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>${fzookeeper.version}</version>
        </dependency>
        <dependency>
            <groupId>com.github.sgroschupf</groupId>
            <artifactId>zkcLient</artifactId>
            <version>${zkcLient.version}</version>
        </dependency>
    </dependencies> 
    ```

###### Curator

- 依赖Jar包

  - ```xml
    <dependency>
        <groupId>org,.apache,curator</groupId>
        <artifactId>curator-framework</artifactId>
        <Version>2.4.2 </version>
    </dependency> 
    ```









