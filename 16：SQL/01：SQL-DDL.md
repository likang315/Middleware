##### SQL-DDL

------

[TOC]

##### 01：SQL 语句分类

​	结构化查询语言：在关系数据库上执行数据操作，检索及维护所使用的标准语言；

1. 数据定义语言（DDL）
   - 创建，修改，删除数据库的对象，数据库对象包括：表，视图，索引，序列 
2. 数据操作语言（DML）
   - 修改数据表的数据，增，删，改
3. 事物控制语言（TCL）
   -  维护数据一致性的语，开始，提交，回滚，保存点
4. 数据查询语言（DQL）
   - 查询所需要的语言
5. 数据控制语言（DCL）
   - 执行权限的授予和回收操作，维护数据库，授予，回收，创建用户

######  注意

- SQL语句本身不区分大小写，但是出于可读性的目的，我们通常会将SQL的关键字全部大写，非关键字全部小写
- ；：分隔符，是不必要的，但是作为一次语句的结束
- %：替代一个或多个字符
- ?：仅替代一个字符

##### 02：DDL【create、drop、alter、show】

- SHOW databases；查看有多少个数据库
- USE 数据库名；选择数据库
- SHOW TABLES；查看数据库中有多少表
- SHOW CREATE TABLE 表名； 查看表创建的语法
- DESC 表名；查看表的结构

##### 03：建表（CREATE）

```SQL
CREATE TABLE 表名 (
    id	 BIGINT UNSIGNED AUTO_INCREMENT  NOT NULL comment '主键ID',
    列名	数据类型（）DEFAULT defalut_value NOT NULL comment 'xx',
    列名	数据类型（）DEFAULT defalut_value NOT NULL comment 'xx',
    c_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    u_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 
    '更新时间',
    PRIMARY KEY (`id`)，
    Foreign key(列名) references 目标表名（列名)
);
```

- 创建相同的表：CREATE TABLE 表1 LIKE 表2;
- 从已有的表中创建新表： CREATE TABLE 新表1 SELECT 字段1，字段2... FROM 原表2；

###### 约束条件

- 既可以写在字段之后，也可在最后用括号用约束字段，例：PRIMARY KEY(id)


1. **NULL，NOT NULL** ：指示某列存储可以为 NULL或者不能为NULL值；
2. **PRIMARY KEY** ：主键，确保某列（或两个列多个列的结合）有唯一标识，有助于更容易更快速地找到表中的一个特定的记录；
3. **FOREIGN KEY **：外键，一个表中的 FOREIGN KEY 指向另一个表中的 PRIMARY KEY，一般不用外键；

   - 外键约束：外键必须在另一个表中存在，否则不能添加；
4. **DEFAULT**：列赋值时的默认值；
5. **CHECK**：用于限制列中的值的范围，CHECK （条件）；
6. **UNSIGNED**：定义数值类型是一定要说明是否有无符号；

##### 04：修改表（ALTER）

1. 修改表名
   - AITER TABLE 表名 **RENAME** 新表名；
2. 修改表的结构
   - 增加新的字段：ALTER TABLE 表名 **ADD** 字段名... after 字段名|first；
   - 修改现有字段：ALTER TABLE 表名 **MODIFY** 修改的字段；
   - 修改字段名：   ALTER TABLE 表名 **CHANGE**  旧字段名 新字段名 数据类型 ... ;
   - 删除现有字段：ALTER TABLE 表名 **DROP** 字段名；

- alter table stu add name varchar(30) default 'xxx' not null after id;
- alter table stu modify name varchar(16) default 'unknown';

##### 05：删除表（DROP）

- DROP TABLE 表名； 删除表
- TRUNCATE TABLE tableName;
  - 清空表；