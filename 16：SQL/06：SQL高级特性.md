### SQL 高级特性

------

[TOC]

##### 01：事务

###### Mysql

- 显示的开启事务：start transaction；
- 结束事务：commit 或者 rollback，结束事务也可能由以下情景触发；
  1. 服务器宕机，在这种情况下，服务器重启时事务将会被自动回 滚; 
  2. 提交一个DML 语句，比如：alter table，这将会引起当前事务子提交和一个新事务启动; 
  3. 提交另一个start transaction命令，将会引起前一个事务提交;
  4.  服务器检测到一个死锁并且确定当前事务就是罪魁祸首，那么服务器就会提前结束当前的事务。这种情况 下，事务将会被回滚,同时释放错误消息；
- 事务保存点
  - 需要回滚事务，但不想回滚之前所有的操作，才内部创建多个保存点（快照），所有的保存掉都需要一个名字；
  - 创建事务保存点：SAVEPOINT name；
  - 回滚掉事务保存点：ROLLBACK TO SAVEPOINT name；

##### 02：视图

- 一种简单的查询机制。不同于表，不涉及数据的存储（不会占用空间），先创建一个视图以供查询时使用；
- 执行创建语句时，数据库只是**存储了视图的定义**，如果不查询就不会检索任何数据；
- 数据库真正执行的sql 不是用户提交的sql，是**和视图定义结合的SQL语句**；
- 视图可以像表一样写复杂SQL，联表等操作；

###### 示例

- 隐藏用户信息表中的性别

- ```sql
  # 创建的视图没有性别
  CREATE VIEW local_student_info_vw
              (
               stu_global_key,
               name,
               class_id,
               age
                  )
  AS
  SELECT stu_global_key, concat('name: ', name) name, class_id, age
  FROM local_student_info;
  
  # 检索机制
  SELECT stu_global_key, name
  FROM local_student_info_vw;
  # 真正执行的
  SELECT stu_global_key, concat('name: ', name) name
  FROM local_student_info;
  ```

###### 使用场景

1. 隐藏数据，数据安全；
2. 用于中间表，且不会占用空间，还可以禁止查询底表；

###### 可更新的视图

- 在特定的规则下通过视图修改底表数据；

  1. 没有使用聚合函数(max min() 和avg() 等)
  2. 视图没有使用 用group by或 having子句; 
  3. select 或 from 子句中不存在子查询，并且where子句的任何子查询都不引用 from 子句中的表; 
  4. 视图没有使用union、union all 和 distinct;

- 

  ```sql
  # 不能更新name 列，因为它使用了函数；
  UPDATE local_student_info_vw
  SET local_student_info_vw.stu_global_key = '爸爸'
  WHERE local_student_info_vw.stu_global_key = 'xing'
  ```

##### 03：元数据【数据的数据】

- information_schema 数据库里所有的对象都是视图；

- 查询tal 数据库的表信息

  ```sql
  SELECT *
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = 'tal'
  ```

- | 视图名称                        | 相关信息                                                     | 作用                                   |
  | ------------------------------- | ------------------------------------------------------------ | -------------------------------------- |
  | SCHEMATA                        | 提供了当前mysql实例中所有数据库的信息。                      | show databases的结果取之此表。         |
  | TABLES                          | 提供了关于数据库中的表的信息（包括视图）。详细表述了某个表属于哪个schema，表类型，表引擎，创建时间等信息 | show tables from schemaname            |
  | COLUMNS                         | 提供了表中的列信息。详细表述了某张表的所有列以及每个列的信息 | show columns from schemaname.tablename |
  | STATISTICS                      | 提供了关于表索引的信息                                       | show index from schemaname.tablename   |
  | USER_PRIVILEGES（用户权限）表   | 给出了关于局权限的信息                                       |                                        |
  | SCHEMA_PRIVILEGES（数据库权限） | 给出了关于方案（数据库）权限的信息                           |                                        |
  | TABLE_PRIVILEGES（表权限）表    | 给出了关于表权限的信息。                                     |                                        |
  | CHARACTER_SETS（字符集）表      | 提供了mysql实例可用字符集的信息                              | SHOW CHARACTER SET                     |
  | COLLATIONS                      | 提供了关于各字符集的对照信息                                 |                                        |
  | TABLE_CONSTRAINTS               | 描述了存在约束的表。以及表的约束类型                         |                                        |
  | VIEWS                           | 给出了关于数据库中的视图的信息。                             |                                        |





