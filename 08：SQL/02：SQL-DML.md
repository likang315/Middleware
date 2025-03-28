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

- SELECT * FROM emp LIMIT 2;      从第0条开始（没有数据），向下查询2条，实际是：0, 2
- SELECT * FROM emp LIMIT 2, 4;  
  - 等价于：SELECT * FROM emp LIMIT  4  OFFSET 2;
  - 跳过第二条，从第3条开始，查询4条
- SELECT * FROM emp WHERE id > 3 LIMIT 5; 
  - 当第一个值比较大时，尽量使用id 的方式高效分页，否则可能会逐行扫描到指定数值后，再进行分页，效率较慢。
  - **OFFSET**：偏移量的下一个值开始取；

##### 15：SELECT 语句的执行过程

###### from->where->group by->having->select-->order by->limit

1. FROM 子句：组装来自不同数据源的数据
2. WHERE 子句：基于指定的条件对记录行进过滤
3. GROUP BY子句：将数据划分为多个分组，使用聚集函数进行计算
4. HAVING 子句：对分组进行过滤
5. 计算所有的表达式，例：select:查看结果集中的哪个列，或列的计算结果 
6. OERDER BY ：对结果集进行排序
7. LIMIT 子句：分页

##### 16：SQL 语句的优化

1. SELECT子句：少用\*号，尽量取字段名称
   - 在解析的过程中, 会将依次转换成所有的列名
2. LIMIT 1：查询一条符合时终止
   - 避免全表扫描，找到即返回
3. ORDER BY：**字段加上索引**，最左前缀匹配原则
   - 把ORDER BY 的字段加上索引，因为符合最左前缀匹配原则
4. WHERE子句
   - WHERE 条件之前，可以过滤掉最大数量记录的条件
5. HAVING子句：很耗资源，**尽量少用**
   - 所有记录之后才对结果集进行过滤，非常消耗资源
6. 使用 EXPLAIN 查看执行计划
   - 查看有没有走索引
