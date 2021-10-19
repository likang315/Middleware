### SQL（Structured Query Language）

------

[TOC]

##### 01：DML 【insert、delete、update、select】

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

##### 08：使用集合

###### 前置条件

- 两个数据集合必须具有同样数目的列；
- 两个数据结合中对应列的数据类型必须是一样的；
- 重合行：两个数据集合中完全一样的行；

###### 集合操作符（并，交，差）

- **UNION**、UNION ALL 操作符
  - 用于连接多个结果集，UNION 对连接后的结婚排序并去除重复项，而UNION ALL 保留重复项。
- **INTERSECT**、INTERSECT ALL 操作符
  - 去除交集区域中所有重复的行；
- **EXCEPT**、EXCEPT ALL 操作符
  - 返回第一个表减去与第二个表重合的元素后剩下的部分；
  - 两个操作符的区别在于，except 在集合A中除去所有的重复数据，而except all 则根据重复数据再集合B 中出现的次数进行删除；

###### 集合操作规则

- 排序
  - 如果需要对复合查询的结果进行排序，那么可以在最后一个查询后面增加order by子句。当在 order by子句中指定要排序的列时，需要从复合查询的第一个查询中选择列名。 通常情况下，复合查询中两个查询对应列的名字是相同的；
- 操作符优先级
  - 包含3个以上的查询语句时，他们以自顶向下的顺序被解析执行；
  - 可以用括号明确他们的执行顺序；

##### 09：字符串数据

###### 使用字符串数据

- 向表中插入数据时，需要保证长度不超过字符列的最大长度，否则汇报异常。
- sql_mode 默认是：strict，ansi 会在超出长度时，自动截取字符串长度；

###### 包含单引号

- 使用 反斜杠：\ 转义；

###### 包含特殊字符

- **CHAR()** ：用于从ADSCII 字符集中255 个字符串中任意构键字符串；

  - 不同的字符集，显示不同的字符；

  - ```sql
    select char('128', '132');
    ```

- **ASCII ('A')**  ：查看'A' 对应的ASCII码；

###### 操作字符串

- **LENGTH()** ：返回字符串的字符数；

- **POSITION('子串' IN fieldName)**：定位子串在字段中出现的位置；

  - 没有找到返回：0；

- **STRCMP('A', 'B')**：比较两个字符串的顺序；

  - 0：两个字符串顺序相等，该函数对大小写不敏感的；
  - -1：第一个字符串在第二个字符串之前；
  - 1：第一个字符串在第二个字符串之后；

- **LIKE、REGEXP**：比较字符串

  - 1为true，0为false；

  - ```sql
    select name, name LIKE '%ns' as a from student;
    ```

- **CONCAT()** ：拼接若干个字符串；

  - 

    ```sql
    select CONCAT('the name is', name, 'and age is', age) desc;
    ```

- **INSERT('原始字符串'， 开始操作位置，需要替换的字符数，'替换字符串')**：

  - ```sql
    select INSERT('goofbye world', 1, 7, 'hello');
    # hello world
    ```

- **SUBSTRING（'字符串'，开始位置，截取字符数）**：截取子串；

- **COALESCE**

  - COALESCE 函数需要许多参数，并返回第一个非NULL参数。如果所有参数都为NULL，则COALESCE函数返回`NULL`；

  - ```sql
    COALESCE(value1,value2,...); 
    ```

##### 10：数值数据

###### 使用数值数据

- 如果数值列的精度大于所在列的指定长度，那么在存储时发生取整操作；

- ```sql
  select 4 / 5 * 2;
  ```

###### 算术函数

- **MOD(A, B)**：A除B的余数；
- **POW(A, B)**：求A的B的幂次方；

###### 控制精度函数

- **CEIL()**：向上截取整型数字；

- **FLOOR()**：向下截取整型数字；

- **ROUND()**：四舍五入截取整型数字；

  - 可选的第二个参数：保留几位小数；

  - ```sql
    select ROUND(3.500001)；
    select ROUND(3.500001, 3);
    ```

- **TRUNCATE()**：制定保留几位小数，但不进行进位；

  - ```sql
    select TRUNCATE(3.500001, 3)；
    ```

###### 处理有符号数

- **ABS（）**：取绝对值；

##### 11：时间数据

###### 使用时间数据

- 查看时间设置

  - ```sql
    SELECT @@global.time_zone, @@session.time_zone;
    ```

- 表示日期数据的字符串

  - 服务器**提供日期组件自动转换**；

- 产生日期的函数

  - str_to_date('字符串'， '日期格式化符')

  - 转换成datetime类型；

  - ```sql
    SELECT STR_TO_DATE('2017-01-06 10:20:30','%Y-%m-%d');
    ```

- 获取当前时间

  - ```sql
    SELECT current_date, current_time, current_timestamp
    ```

###### 操作日期数据

- **DATE_ADD(param1, param2)**

  - 获取param1 在增加数量后的日期；

  - param1: 日期值；

  - param2: INTERVAL 所需增加的数量以及时间间隔类型；

    - Day: 天数；
    - Month: 月份；
    - Hour_second: 小时数、分钟数和秒数，中间用“：” 隔开

  - 示例

  - ```sql
    SELECT current_date, date_add(current_date, INTERVAL 15 DAY);
    ```

- **LAST_DAY(param1)**

  - 获取所传日期的当月最后一天；
  - param1: 参数是一个时间类型；
  - 返回一个Date类型；

- **EXTRACT(param1)**

  - 获取param1 中的时间属性；

  - param1: 时间类型 FROM 时间

  - ```sql
    SELECT EXTRACT(YEAR FROM current_time) 
    ```

- **DATEDIFF(param1, param2)**

  - 获取两个日期间隔；

  - 只受年月日的影响，与时分秒无关；

  - ```sql
    SELECT DATEDIFF('2021-09-08 22:00:00', '2021-09-05 01:00:00')
    ```

- **TIMESTAMPDIFF**

  - 计算时间差

  - 可以计算day、hour、minute

  - ```sql
    SELECT TIMESTAMPDIFF(DAY, create_time, update_time)
    ```

##### 12：转换

- **CAST(expr AS type)**

  - 将expr 转换成type类型；

  - ```sql
    SELECT CAST('2019-08-29 16:50:21' AS DATE);
    ```

##### 13：条件逻辑

###### case 表达式

- 查找型case 表达式

- ```sql
  SELECT stu.name,
         CASE
             WHEN stu.sex = '1'
                 THEN score.english
             WHEN stu.sex = '2'
                 THEN score.chinese
             ELSE 'empty'
         END score
  FROM local_student_info stu
           LEFT JOIN local_student_score score ON stu.stu_global_key = score.stu_global_key
  ```
  - END 子句是可选的；
  - THEN 的表达式也可以是子查询；

###### case 表达式的范例

- 结果集转换

  - 列转行（两列数据转为一行）；

- 选择性聚合

  - ```sql
    # 统计人力数
    SELECT SUM(CASE
                   WHEN sex = '1'
                       THEN 1
                   WHEN sex = '2'
                       THEN 2
                   ELSE 0
        END) count
    FROM local_student_info;
    ```

- 存在性检查

  - EXIST （）函数用于case 表达式；

- 除零错误

  - case 表达式判断为0时，指定为1；

- null 值处理

  - ```sql
    CASE 
    	WHEN title IS NULL 
    		THEN  'unknown'
        ELSE title
    END
    ```

##### 14：LIMIT：分页查询

- SELECT * FROM emp LIMIT 2;      从第一条开始，查询三条，实际是：0,3
- SELECT * FROM emp LIMIT 2, 4;  
  - 等价于：SELECT * FROM emp LIMIT  4  OFFSET 2;
  - 跳过第二条，从第3条开始，查询4条
- SELECT * FROM emp WHERE id > 3 LIMIT 5; 
  - 当第一个值比较大时，尽量使用id 的方式高效分页，否则可能会逐行扫描到指定数值后，再进行分页，效率较慢。
  - **OFFSET**：偏移量的下一个值开始取；

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
