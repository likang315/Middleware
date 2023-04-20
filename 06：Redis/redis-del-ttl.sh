#!/bin/bash

# 定义变量
NODE=127.0.0.1
NODE_PORT=6379
REDIS_PASSWORD=likang2023
# scan 参数
MATCH=li*

# 定义导出文件名（当前目录）
FILE_NAME="no_ttl_keys.txt"
rm -f $FILE_NAME

# 定义redis集群的ip地址
REDIS_NODES=$(redis-cli -h $NODE -p $NODE_PORT -a $REDIS_PASSWORD --no-auth-warning cluster nodes | awk '{print $2}')
# 遍历redis集群中的所有节点
for node in $REDIS_NODES; do
    # 获取节点的ip地址和端口号
    IP=$(echo $node | cut -d ':' -f 1)
    PORT=$(echo $node | cut -d ':' -f 2)
    # 跳过 role:slave 节点
    NODE_ROLE=$(redis-cli -h $IP -p $PORT -a $REDIS_PASSWORD --no-auth-warning info replication | grep role)
    if [[ "$NODE_ROLE" =~ "slave" ]]; then
        continue
    fi

    CURSOR=-1
    # 记录工作节点
    echo new node: ----------------$IP:$PORT----------------
    # 循环执行 scan 命令，scan 遍历结束时返回 0
    while [[ $CURSOR -ne 0 ]]; do
        # 初始化下标
        if [[ $CURSOR -eq -1 ]]; then
            CURSOR=0
        fi
        # 执行 scan 命令
        RESULT=$(redis-cli -h $IP -p $PORT -a $REDIS_PASSWORD --no-auth-warning scan $CURSOR MATCH $MATCH count 1000)
        # 获取新的 cursor
        CURSOR=$(echo $RESULT | awk '{print $1}')
        echo $CURSOR
        # 获取 key 列表, 列转行，替换第一个，然后取全部
        KEYS=$(echo $RESULT | awk '{$1=""; print $0}')
        echo CURSOR: $CURSOR

        # 循环删除 ttl 为 -1 的 key
        for KEY in $KEYS; do
            TTL=$(redis-cli -h $IP -p $PORT -a $REDIS_PASSWORD --no-auth-warning ttl $KEY)
            if [ $TTL -eq -1 ]; then
                # 将key写入文件 & del
                redis-cli -h $IP -p $PORT -a $REDIS_PASSWORD --no-auth-warning del $KEY
                echo $KEY >>$FILE_NAME
            fi
        done
    done
done

echo execute successful !!!