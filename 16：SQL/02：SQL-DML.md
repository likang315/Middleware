### SQL（Structured Query Language）

------

[TOC]

##### 0：DML 【insert、delete、update、select】

1. **INSERT INTO** 向表中插入数据
  
   - INSERT INTO tableName(id，name，birth) VALUES(1，'jack','199-22-45'）)；
   
     ```sql
     # 批量插入 
     INSERT INTO table_name (id, name,sex,address)
     VALUES
     (?,?,?,?),(?,?,?,?),(?,?,?,?),(?,?,?,?)
     ```
   
2. **DELETE FROM ** 删除表中的数据，要加WHERE语句限定删除的记录，否则就是清空表操作
  
   - DELETE FROM 表名 WHERE id=i;
   
3. **UPDATE**：修改表中的数据
  
   - UPDATE 表名 SET 字段名=值，字段名=值 WHERE 限制记录的字段；
   - UPDATE stu t set t.name = 'mike', t.id = '1' where t.ID = '2';

##### 02：查询语句

| 子句名称 | 使用目的                                     |
| :------: | -------------------------------------------- |
|  select  | 确定结果集应该包含那些列                     |
|   from   | 指明所要提取数据的表，以及这些表是如何连接的 |
|  where   | 过滤不需要的数据                             |
| group by | 用于对具有相同列值的行进行分组               |
|  having  | 过滤掉不需要的组                             |
| order by | 按一个或多个列,对最后结果集中的行进行排序    |

##### 03：SELECT

- SELECT * FROM tableName； 查看表的所有列；

###### SELECT 子句包含的内容：

1. 字符，比如数字或者字符串；
2. 表达式：比如：id * 100;
3. 内建函数：比如：count(*);
4. 自定义函数的调用；
   - 
   - 可以使用 "" 给别名区分大小写和添加空格

###### 列的别名【AS】

- **AS 别名** 或者 直接加 **别名**：那么结果集会以这个别名作为字段的名字； 

###### 去除重复的列【DISTINCT】

- 在可能存在重复的列名之前加上 DISTINCT 关键字；
- 对指定字段去重，用多字段去重时，并不是字段的拼接没有重复值，而是单独按顺序作用于每个字段的；
  - SELECT DISTINCT column_name as new_name, column_name FROM table_name; (组合去重)
- distinct必须放在开头；
- SELECT count(DISTINCT stu_global_key, name)
  -   去重统计；
- 注意：产生无重复的结果集需要首先对数据进行排序，这对于大结果集来说是非常耗时的，因此不要随意使用DISTINCT；

##### 04：FROM 子句

- from 限定了查询所使用的表，以及连接这些表的方式；

###### 表的概念

- 永久表：使用create table 语句创建的表；
- 临时表：子查询所返回的表；
- 虚拟表：使用create view 子句锁创建的视图；
  - 视图：存储在数据字典中的查询，它的行为变现得像一个表，但实际上并不拥有任何数据；

###### 定义表表名

- 在表名后直接加别名，用于多表连接，和联表查询时选择字段；

##### 05：where 子句

- 用于结果集中过滤掉不需要的行；
- 在该子句中同时使用and 和 or 操作符时，需要**使用括号来对条件分组**；

##### 06：order by  子句

- 用于对结果集中的**原始列数据**或是**列数据计算的表达式结果**进行排序；
- 按照**多字段排序**时，首先按照第一个字段排序，当第一个字段有重复值时才会按照第二个字段排序方式进行排序
- 在排序时，可以通过关键字 ASC 和 DESC 指定是升序还是降序。由于默认情况下是升序排序，因此想要降序排序时，加上DESC 关键字即可。
- 根据表达式排序：ORDER BY right(doc_id, 3)；
- 在排序中NULL值被认为是最大值;

##### 07：过滤

##### 构键条件

###### and 、or 操作符

- and：所有条件必须全部为 true 才可以包含在结果集中；
- or：只要有一个条件为true，就会包含在结果集中；

###### NOT 操作符（少用）

- AND NOT（ column_1 = A OR column_2 = B）
- 等价于：AND column_1 != A OR column_2 != B

##### 条件类型

###### 相等条件

- column = B

###### 不等条件

- column != B  
- column <> B

###### 范围条件

- between A and B
  - 同时需要限制范围上下限的，需要注意小在前，大在后；

###### 成员条件

- IN ('A', 'B')
  - 限制条件是一个有限值得集合
- NOT  IN ('A', 'B')

###### 匹配条件

- 使用通配符

  - | 通配符    | 匹配                    |
    | --------- | ----------------------- |
    | _         | 正好一个字符            |
    | %   %123% | 任意数目的字符（包括0） |

- 使用正则表达式

  - field REGEXP  正则

##### NULL 关键字

- IS NULL
  - 不能使用 = null ，不会报错，并且不会返回任何结果；
- IS NOT NULL
  - 写限定条件时，首先确定该字段为不为NULL ，防止忽略

##### 08：多表查询

###### 笛卡尔积

- 直接用 JOIN  将两个表的数据简单连接到一起；

###### 内连接

- INNER JOIN  ... ON 连接条件
  - 只有符合的才出现在结果集里，没有匹配上（某个表为NULL，使用外连接）会剔除；
  - 如果两个表的列名相同可以使用 USING 代替 ON
  - INNER JOIN ... USING(fieldName)

###### 连接多张表

- 多表的连接视为“滚雪球” ，前两个表形成一个开始滚动的“雪球”，而之后的每个表都在“雪球” 滚动时依附在上面。
- 中间结果表再去连接其他表；

###### 将子查询结果作为查询表

- INNER JOIN （SELECT * FROM tableName）a

###### 自连接

- 不仅可以在同一查询中多次包含同一个表，还可以对表自身进行连接。

- 同一张人员表表，存储了上下级关系；

- ```sql
  SELECT e.fname, e.l name, e mgr.fname mgr_fname, e_mgr.l name mgr_l name
  FROM employee e INNER JOIN employee e_mgr ON e.superior_emp_id = e mgr.emp_id;
  ```

###### 相等连接和不等连接

- ON a.start_time >= b.start_time;
  - 查询的两个表可能并**没有外键关联**；
- ON a.id != b.id
  - 对表自身使用不等连接；

###### 连接条件和过滤条件

- ON 子句使用连接条件，WHERE 使用过滤条件，可以灵活的放置条件的位置；

##### 09：使用集合

- 



















7. ANY，ALL：常用于子查询
   - ANY：任意一个
   - ALL：所有的
   - select * from stu where class='1' and age > any (select age from stu where class='2')；
   - 查询出01班中，年龄大于 02班任意一个的同学

##### 06：GROUP BY：分组

- 将结果集按照指定的**字段值相同的记录分为一组**，常和分组函数联用；

- 如果在以某字段为根据进行了分组之后，select 那个字段得到的不是这个字段的信息，而是这个字段的分组信息，如果不与聚合函数联合使用，只显示该组第一条记录；

- 使用Select查询的列，在使用分组的语句中**要不包含在group by 字段中，要不包含在聚合函数中**。不然查询会报错。

- ```sql
  select stu.name, avg(s.math) as math, avg(s.chinese) as chinese
  from score s INNER JOIN student stu
  ON stu.id = s.student_id
  group by stu.name;
  ```
  
  - score 表中可能会有多条一个学生的成绩，求平均
    - 先找到联表中符合限定条件的信息；
    - 以group by 后的字段作为分组key；
    - 进行聚合计算，最终显示也是key，字段名；
  
- **多列分组**：按照多列（字段A+字段B）合并后的值进行分组；

- **HAVING 条件表达式**：用来限制分组后的显示，符合条件表达式的结果将被显示；
  
  - HAVING   COUNT(sex) >= 1;
  
- **WITH ROLLUP**：将会加上一条记录，这一条记录是上面所有记录的总和。

###### 聚合函数

- MAX（），MIN（）：求指定字段的最大值和最小值
- AVG，SUM：求平均值和总和
- COUNT( )：对给定字段的记录进行统计 
  - COUNT（*）：统计这个表有多少记录，有时候返回long，有时候返回 BigInteger，配合进行
  - SELECT price, **count(*) AS number** FROM tablename GROUP BY price；

##### 08：WHERE：限制条件

- WHERE ：不能使用聚合函数作为过滤条件，原因是过滤时机不对，子查询；
- WHERE：是在数据库检索表中数据时，对数据**逐行过滤**以决定是否查询该数据是否使用的，所以WHERE用来**确定结果集**的数据的，聚合函数**是从结果集中，并且分组完毕才进行过滤的**，由此可见这个过滤时机是在WHERE之后进行的，所以**聚合函数的过滤条件**要在HAVING子句中使用， HAVING必须在GROUP BY之后；
- SELECT dep_id,count(dep_id) AS count FROM emp GROUP BY dep_id **HAVING count(dep_id)>1**；

##### 09：LIMIT：分页查询

- SELECT * FROM emp LIMIT 2;      从第一条开始，查询三条，实际是：0,3
- SELECT * FROM emp LIMIT 2, 4;  
  - 等价于：SELECT * FROM emp LIMIT  4  OFFSET 2;
  - 跳过第二条，从第3条开始，查询4条
- SELECT * FROM emp WHERE id > 3 LIMIT 5; 
  - 当第一个值比较大时，尽量使用id 的方式高效分页，否则可能会逐行扫描到指定数值后，再进行分页，效率较慢。
  - **OFFSET**：偏移量的下一个值开始取；

##### 10：UNION、UNION ALL：合并结果集

- 用于**合并两个或多个 SELECT 语句的结果集**，UNION 命令只会选取不同的值
- UNION 内部的 SELECT 语句必须拥有相似的列对应
- SELECT column_name(s) FROM table_name1  UNION ALL  SELECT column_name(s) FROM table_name2；
- UNION ALL：命令会列出所有的值，不去重，效率高；

##### 11：多表查询

​	从多张表中查询信息，关联查询的重点与这些表中的记录的对应关系，这个关系也称**连接条件**，N张表就有N-1个连接条件；

- 当两张表有相同字段时，SELECT子句中必须明确指定该字段来自那张表，在关联查询中，表名也可以有别名，在表名其后直接写，可以简化语句的复杂度
- 关联查询要添加连接条件，否则会产生**笛卡尔积它是一个无意义的结果集**，它的记录数是与**所有参与查询的表的记录数乘积的结果**，可能会出现内存溢出；
- SELECT e.id,e.name FROM emp e INNER JOIN dep d ON e.dep_id=d.dep_id；

##### 12：关联查询【连接查询】

![](https://github.com/likang315/Middleware/blob/master/Mysql%EF%BC%8CInnoDB/InnoDB/%E8%BF%9E%E6%8E%A5%E6%9F%A5%E8%AF%A2.png?raw=true)

###### 内连接：获取两个表中字段匹配关系的记录，可以省略 INNER 使用 JOIN，效果一样

- FROM 表名1 表1对象 INNER JOIN 表2名 表2对象 ON 连接条件 WHERE 过滤条件
- select e.id,d.dname as dep_name,e.name,e.sex,e.age FROM emp e **INNER JOIN** dep d **ON e.dep_id=d.id;**
- 会自动优化成小表驱动大表，大表加索引提高查找速度；

###### 外链接：所有数据都显示

- **左外连（LEFT JOIN）**：以JOIN左侧作为驱动表，获取左表所有记录，即使右表没有对应匹配的记录的字段值，用NULL 填充
- select e.id,d.dname as dep_name,e.name,e.sex,e.age from emp e **LEFT JOIN** dep d **ON** e.dep_id=d.id;
- **右外连（RIGHT JOIN）**：与 LEFT JOIN 相反，用于获取右表所有记录，即使左表没有对应匹配的记录的字段值，用NULL填充
- select e.id,d.dname as dep_name,e.name,e.sex,e.age from emp e **RIGHT JOIN** dep d **ON** e.dep_id=d.id;

##### 13：count（1）和 count（*），count（字段） 的区别

- 统计有多少条的记录
- count( 1 )：忽略所有列，**用1 （常量）代表代码行**，在统计结果的时候，**包含值为NULL的记录**，除非是主键索引，否则和count( * )没有区别
- count( * )：会自己优化指定到使用索引的字段，**包含值 null 的记录**；
- count( 字段名 )：统计该字段在表中出现的次数，**不包含值为 null记录**；

##### 14：SQL 中单引号和双引号的区别

- 标准 SQL 中，**字符串使用的是单引号**，但是mysql做了兼容双引号也可以；
- 若字符串中也有单引号，则使用两个单引号，转义的；

##### 15：EXPLAIN【SQL执行计划】

查看运行SQL语句时哪种策略预计会被优化器采用，查看有没有走索引

```shell
# EXPLAIN select * from student;
| id | select_type | table | type  | possible_keys | key  | key_len | ref  | rows | Extra										 |
|  1 | SIMPLE      | t1    | index | PRIMARY       | name | 63      | NULL |    4 | Using where; Using index |
```

1. ###### id：包含一组数字，表示查询中执行 select 子句 或 操作表的顺序	

   - id相同，执行顺序由上至下
   - id 如果相同，可以认为是一组，从上往下顺序执行；在所有组中，id值越大，优先级越高，先执行

2. ###### select_type 示查询中每个select子句的类型（简单OR复杂）

   - **SIMPLE：**查询中不包含子查询或者UNION；
   - **PRIMARY：**查询中若包含任何复杂的子部分，最外层查询被标记；
   - **SUBQUERY：**在SELECT或WHERE列表中包含了子查询，该子查询被标记；
   - **DERIVED（派生）：**用来表示包含在from子句中的子查询语句
   - 若第二个SELECT出现在UNION之后，则被标记为UNION；若UNION包含在FROM子句的子查询中，外层SELECT将被标记为：DERIVED 
   - 从UNION表获取结果的SELECT被标记为：UNION RESULT

3. ###### table ：执行SQL用到的表名

4. ###### type ：表示MySQL在表中找到所需行使用的方式，又称“访问类型”

   - ALL, index, range, ref, eq_ref, const, system, NULL，从左到右，性能从最差到最好
     - ALL：全表扫描；
     - index：只遍历索引树；
     - range：索引范围扫描，对索引的扫描开始于某一点，返回匹配值域的；
     - ref：**使用非唯一索引扫描或者唯一索引的前缀扫描**，返回匹配某个单独值的记录行；
     - NULL：MySQL在优化过程中分解语句，执行时甚至不用访问表或索引（覆盖索引）；

5. ###### possible_keys ：

   - 指出MySQL**可能使用哪个索引**在表中找到记录的，查询涉及到的字段上若存在索引，则该索引将被列出，但不一定被查询使用；

6. ###### key：

   - 显示MySQL在查询中**实际使用的索引**，若没有使用索引，显示为NULL

7. ###### key_len：

   - 表示索引中使用的字节数，可通过该列计算查询中使用的索引的长度（key_len显示的值为索引字段的最大可能长度，并非实际使用长度，即key_len是根据表定义计算而得，不是通过表内检索出的）

8. ###### ref：

   - 表示上述表的连接匹配条件，即哪些列或常量被用于查找索引列上的值

9. ###### rows：

   - 估算的找到所需的记录所需要读取的行数

10. ###### Extra：

    - 包含不适合在其他列中显示但十分重要的额外信息
    - **Using index：**该值表示相应的select操作中使用了覆盖索引
    - **Using where：**表示 mysql 服务器将在存储引擎检索行后再进行过滤
    - **Using temporary：**表示 MySQL 需要**使用临时表来存储结果集**，常见于排序和分组查询

##### 16：SELECT 语句的执行过程

###### from->where->group by->having->select-->order by->limit

1. FROM 子句：组装来自不同数据源的数据
2. WHERE 子句：基于指定的条件对记录行进过滤
3. GROUP BY子句：将数据划分为多个分组，使用聚集函数进行计算
4. HAVING 子句：对分组进行过滤
5. 计算所有的表达式，例：select:查看结果集中的哪个列，或列的计算结果 
6. OERDER BY ：对结果集进行排序
7. LIMIT 子句：分页

##### 17：SQL 语句的优化

1. WHERE子句：执行顺序为**从右到左**
   - WHERE 条件之前，可以过滤掉最大数量记录的条件，必须写在WHERE 子句的末尾
3. HAVING子句：很耗资源，**尽量少用**
   - 所有记录之后才对结果集进行过滤，非常消耗资源
4. SELECT子句：少用\*号，尽量取字段名称
   - 在解析的过程中, 会将依次转换成所有的列名
4. ORDER BY：**字段加上索引**，最左前缀匹配原则
   - 把ORDER BY 的字段加上索引，因为符合最左前缀匹配原则
5. LIMIT 1：查询一条符合时终止
   - 避免全表扫描，找到即返回
6. 使用 EXPLAIN 查看执行计划
   - 查看有没有走索引
