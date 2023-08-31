### MySQL-Install

------

[TOC]

##### 01：Linux 安装MySQL

1. 首先查看服务器系统版本；
2. 选择对应版本下载：https://downloads.mysql.com/archives/community/
3. 解压：tar -xvf mysql-server_5.7.31-1debian9_amd64.deb-bundle.tar
4. sudo dpkg -i *.deb
   - 若有依赖，下载对应依赖包；
5. 出现设置密码页面（两次）
6. 查看是否安装成功：systemctl WGET
7. mysql
8. 登录mysql：mysql -y root -ppassword

##### 02：远程访问MySQL

- 如果需要远程访问MySQL，修改配置，默认只能本地访问

- ```shell
  bind-address    = 127.0.0.1 # 修改为0.0.0.0即可
  # 字符编码设置为utf8
  character_set_server = utf8
  init_connect = 'SET NAMES utf8'
  ```

- 重启服务：systemctl restart mysql

- 查看服务状态：systemctl status mysql

- 更改用户Host

  - ```shell
    mysql> use mysql
    mysql> update user set Host="%" where User="root";
    mysql> flush privileges;
    mysql> select Host,User from user;
    ```

  - Host列：指定了允许用户登录所使用的IP，% 通配符指所有IP；

##### 用户授权

- 默认：root 具有用户的全局权限

- 创建用户

  - ```sql
    # 创建用户，使用密码验证
    CREATE USER 'username'@'host' IDENTIFIED WITH authentication_plugin BY 'password';
    ```

- 授权语法：GRANT PRIVILEGE ON database.table TO 'username'@'host';
  - 例如：全局权限：GRANT ALL PRIVILEGE ON  `*.*`  TO 'prod_hot'@'%'；
- 查看用户权限：SHOW GRANTS FOR 'username'@'host';

##### 04：MySQL读取配置文件

​	当启动实例时，mysql数据库会去读取配置文件，若没有配置文件，它会按照编译时的默认参数设置启动实例

###### 读取顺序：

- /etc/my.cnf ----> /etc/mysql/my.cnf ----> /usr/local/mysql/etc/my.cnf ----> ~/my.cnf


##### 05：MySQL 服务的启动、关闭、查看状态

- 使用Systemctl 工具 
  - systemctl restart mysql
  - systemctl stop mysql
  - systemctl status mysql

##### 06：Mac 配置Path

###### 环境变量

- etc/profile、/etc/paths：是系统级别的，系统启动就会加载
- ～/.bash_profile：用户的环境变量，该文件包含专用于你的账户bash，shell的bash信息，当登录时以及每次打开新的shell时，该文件被读取。

###### 查看PATH

-  echo  $PATH：查看环境变量
- /usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:/Users/xxx/Code/Java/Maven/apache-maven-3.6.1/bin
  - ：为path分隔符

###### 修改PATH 

1. sudo vi ～/.bash_profile :使用vi打开.bash_profile，必须使用sudo ，否则没有权限保存

1. ```shell
   export MySQL_HOME=/usr/local/mysql/bin
   export PATH=$PATH:$Maven_HOME:$MySQL_HOME
   # 配置多个时用：分割
   # 在PATH变量后面加多一个目录Maven_HOME
   ```

4：source ~/.bash_profile ：立即生效

##### 08：修改密码

1. 关闭mysql服务
2. cd /usr/local/mysql/bin/
   - sudo su
   - ./mysqld_safe --skip-grant-tables & ：关闭mysql验证功能
3. ./mysql
4. FLUSH PRIVILEGES;  
5. SET PASSWORD FOR 'root'@'localhost' = PASSWORD('你的新密码');

##### 09：MySQL登录、退出

- 登录： mysql -u root -pmysql(密码) 
- 退出：\q 
- 查看状态： \s 
- 修改分隔符：delimiter 分隔符

##### 09：数据库管理

- show databases：查看当前服务器上管理的所有数据库 
- create database 数据库名; 
- use 数据库名; 
- 修改编码格式：alter database 数据库名 character set gbk; 
- 删除数据库：drop database 数据库名; 
- 导入数据库：mysql -u root -p 数据库名 < sql 文件
- 导出数据库：mysqldump -u root -p xupt > xupt.sql

