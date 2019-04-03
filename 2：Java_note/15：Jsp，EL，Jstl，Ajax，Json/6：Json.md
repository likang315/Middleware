## Json��JavaScript Object Notation(JavaScript �����ʾ��)

�Ǵ洢�ͽ����ı���Ϣ���﷨������ XML

Json �ļ����ļ������� ".json"
Json �ı��� MIME ������ "application/json"

######  �ص㣺

1:JSON ��ͨ�� JavaScript ���н���
2:JSON ���ݿ�ʹ�� AJAX ���д���
4:�ܹ�ʹ���ڽ��� JavaScript eval() �������н���
5:ʹ������

���� AJAX Ӧ�ó�����˵��JSON �� XML �������ʹ��

######   ʹ�� XML

  	��ȡ XML �ĵ�
    	ʹ�� XML DOM ��ѭ�������ĵ�
    	��ȡֵ���洢�ڱ�����

######   ʹ�� Json

��ȡ Json �ַ���
�� eval() ���� Json �ַ���

##### 1:Json �����ʾ

[{key��value},{key��value}] 
key�������������������ͣ�value:������Number,boolean,String,Array,null,��������Ƕ�����

##### 2:Json ����

ʹ�� ����.key ����
delete �ؼ�����ɾ�� Json ��������ԣ�ֱ�Ӹ�ֵ�����޸�

##### 3��JSON.parse(text,reviver)    ���ַ�������ת��Ϊ JavaScript ����  

    text:    ���裬 һ����Ч�� JSON �ַ���
    reviver: ��ѡ��һ��ת������ĺ���,��Ϊ�����ÿ����Ա���ô˺���

##### 4����������

JSON ���������������������Խ�������Ϊ�ַ����洢��֮���ٽ��ַ���ת��Ϊ����

  var text = '{ "name":"Runoob", "alexa":"function () {return 10000;}", "site":"www.runoob.com"}';
  var obj = JSON.parse(text);
  obj.alexa = eval("(" + obj.alexa + ")");
  document.getElementById("demo").innerHTML = obj.name + " Alexa ������" + obj.alexa();

##### 5��eval() 

JavaScript�ĺ��������ڽ� JSON �ı�ת��Ϊ JavaScript ����,ʹ�õ��� JavaScript ���������ɽ��� JSON �ı���
Ȼ������ JavaScript ���󣬱�����ı���Χ�������У��������ܱ����﷨����

##### 6��JSONObject

ʵ�����˶��󣬾���JSon����Ȼ��ʹ��put(key,value)������toJsonString��������ֱ�ӽ�Json ת��ΪString

```java
 String json = "{'first': 'one','next': 'two'}";
        public String[] convert(String json)
    {
        String[] str=new String[20];
        int i=0;
        try {
            //��תΪJsonObject����
            JSONObject jsonObject = JSONObject.parseObject(json);
            Set<String> set=jsonObject.keySet();
            Iterator ite= set.iterator();
            while (ite.hasNext()){
                str[i++]=(String) jsonObject.get(ite.next());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return str;
    }
```

### 7��alibaba �� fastJson

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.54</version>
</dependency>
```





