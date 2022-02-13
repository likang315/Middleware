### Json

------

[TOC]

##### 01：Json 概述

- JSON: **J**ava**S**cript **O**bject **N**otation(JavaScript 对象表示法)；
- JSON 是轻量级的文本数据交换格式；

##### 02：Json 对象

- JSON 对象使用在大括号({})中书写，对象可以包含多个 **key/value（键/值）**对。
- key 必须是字符串，value 可以是合法的 JSON 数据类型（字符串, 数字, 对象, 数组, 布尔值或 null），key 和 value 中使用冒号(:)分割，每个 key/value 对使用逗号(,)分割。

###### 访问对象值

- 使用点号（.）来访问对象的值；

###### Json示例

```json
{
    "code": 0,
    "message": "0",
    "data": {
        "list": {
            "tlist": {
                "1": {
                    "tid": 1,
                    "count": 5,
                    "name": "动画"
                },
                "181": {
                    "tid": 181,
                    "count": 1091,
                    "name": "影视"
                }
            },
            "array": [
                1,
                2,
                3
            ]
        }
    }
}
```

##### 03：JsonPath

- 获取 Json 值的的一种方式

- | Xpath | jsonpath |                  概述                  |
  | :---: | :------: | :------------------------------------: |
  |   /   |    $     |                 根节点                 |
  |   .   |    @     |                当前节点                |
  |   /   |  .or[]   |                取子节点                |
  |   *   |    *     |              匹配所有节点              |
  |  []   |    []    | 迭代器标识（如数组下标，根据内容选值） |
  |  //   |    ..    |   不管在任何位置，选取符合条件的节点   |
  |  n/a  |   [,]    |            支持迭代器中多选            |
  |  n/a  |   ?()    |              支持过滤操作              |
  |  n/a  |    ()    |             支持表达式计算             |

- 使用过滤器

  - ```python
    isbn_book = jsonpath.jsonpath(data_json, '$..list[?(@.array)]')
    ```