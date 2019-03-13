## JDBC ( java database connectivty )�� Java �������ݿ��һ�ּ���

JDBC API��sun ��˾�����һ�׽ӿڣ� java.sql.* ��������Щ�ӿ����������ݿ�

###### JDBC��������������������ݿ���������,�����ݿ⳧���ṩ�����Ե�Mysql��վ���أ�mysql-connector-j.jar��

����JBDC�������� D:\mysql-connector-java-5.1.20-bin.jar
IDEA���ã�jar������

�����ࣺJDBC ʵ���� Java.SQL.Drive �ӿ�
���ݿ������ֱ�ʵ����JDBC�淶��,��������ݿ���ʽӿڣ���Ҫ������Connection(���ӽӿ�)��Statement(���ӿڣ��� ResultSet (������ӿ�) ���ֽӿ�

##### Java.SQL.Drive �ӿڣ�ÿ���������������ʵ�ֵĽӿ� 

##### JDBC �������������̿ɷ�Ϊ���ؽ׶Ρ��������ӽ׶κ����ݷ��ʽ׶�

�������ӽ׶ν�������ʵ��ע�ᵽDriverManager(�������������)�У��������ݿ�����

###### DriverManager����һ��JDBC������������������е���������ʹ��ǰ�����������ϵǼ�ע�ᣬ����������ע����������Ĺ���	

###### 1������������    �õ�class����

Class.forName("com.mysql.jdbc.Driver");

###### 2���������ݿ⣨�������أ���

```java
1��Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/xupt?user=root&password=mysql");
2��Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/xupt","root","mysql");
3��	    Properties p=new Properties();  //Map<String,String>
		p.setProperty("user","root");
		p.setProperty("password","mysql");
   Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/xupt",p);
```

###### 3�����ݷ���

  1������һ�������������ڷ���sql�������ݿ��
	Statement stat=con.createStatement();
  2������ sql�������ݿ�





### DriverManager �ࣺ�����������࣬���ڹ���������Ҳ���ڵõ����ݿ�����Ӷ��� Connection

static Connection getConnection(String url) 
      	��ͼ�������������ݿ� URL ������
static Connection getConnection(String url, Properties info) 
     	��ͼ�������������ݿ� URL ������ 
static Connection getConnection(String url, String user, String password) 
      	��ͼ�������������ݿ� URL ������

ע�⣺jdbc���Ӳ�ͬ�����ݿ⣬����ȫ��һ����ֻ�������ط���һ�����������url��һ��

���õ����ݿ��Ӧ��JDBC����
	Oracle:oracle.jdbc.driver.OracleDriver 
	MySQL:com.mysql.jdbc.Driver

�������ݿ��URL
	Oracle��jdbc:oracle:thin:@computerName��IP��ַ:�˿�:���ݿ�����
	MySQL�� jdbc:mysql://computerName��IP��ַ:�˿�/���ݿ�����



### Connection �ӿڣ��õ�ʵ�ִ˽ӿڵ�ʵ������ʾ�Ѻ����ݿ⽨������

  ������
	Statement createStatement() 
             ����һ�� Statement �������� SQL ��䷢�͵����ݿ�
	PreparedStatement prepareStatement(String sql) 
             ����һ�� PreparedStatement ���������������� SQL ��䷢�͵����ݿ�
	CallableStatement prepareCall(String sql) 
             ����һ�� CallableStatement �������������ݿ�洢����  
	void close() 
             �����ͷŴ� Connection ��������ݿ�� JDBC ��Դ�������ǵȴ����Ǳ��Զ��ͷ� 
	DatabaseMetaData getMetaData() 
             ��ȡһ�� DatabaseMetaData ���󣬸ö���������ڴ� Connection ���������ӵ����ݿ��ԭ���� 

�ر�
	Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) 
          ����һ�������޸ģ�����ResultSet ���� ��Statement ���󣬲���ΪResultSet�ĳ���
	



##### Statement �ӿڣ�����ִ�� ��̬ SQL ��� �������������ɽ���Ķ���

 ResultSet executeQuery(String sql) 
         	SELECT���
 int executeUpdate(String sql) 
     	INSERT��UPDATE �� DELETE ��� 

 void addBatch(String sql) 
      	�������� SQL ������ӵ��� Statement ����ĵ�ǰ�����б��У�����executeBatch()��������ִ�д��б��е�����
 int[] executeBatch() 
      	��һ�������ύ�����ݿ���ִ�У����ȫ������ִ�гɹ����򷵻ظ��¼�����ɵ�����

##### PreparedStatement �ӿ�:Statement �� �ӽӿڣ���ʾԤ����� SQL ���Ķ��󣬲����ƻ�Sql���ṹ

ResultSet executeQuery() 
		�ڴ� PreparedStatement ������ִ�� SQL ��ѯ�������ظò�ѯ���ɵ� ResultSet ���� 

int executeUpdate() 
    		�ڴ� PreparedStatement ������ִ�� SQL ��䣬INSERT��UPDATE �� DELETE ���

void setInt(int parameterIndex, int x) 
             ��ָ����������Ϊ����int ֵ
void setString(int parameterIndex, String x) 
void setTime(int parameterIndex, Time x) 
void setDate(int parameterIndex, Date x) 

```java
String sql="insert into person(id,name,sex,age) values(?,?,?,?)";
PreparedStatement ps=con.prepareStatement(sql);
		
		for(int i=1;i<10;i++)
		{
			ps.setInt(1, i);
			ps.setString(2, "name"+i);
			ps.setString(3, i%2==0?"M":"F");
			ps.setInt(4, 20+i);
			ps.addBatch();  //��ӵ�����ִ���б�
		}
		int[] re=ps.executeBatch();
```



##### ResultSet �ӿڣ���ʾ���ݿ����������ݱ���ִ�в�ѯ���ݿ��������ɣ�ResultSet ����һ����¼ָ��

###### Ĭ������£���¼ָ�뱻���ڵ�һ��֮ǰ

 	    boolean next() 
          	�����ӵ�ǰλ����ǰ��һ�С� 
	    boolean absolute(int row) 
		�Ѽ�¼ָ���Ƶ�ָ���к�
	    boolean previous() 
          	������ƶ����� ResultSet �������һ��
	    boolean first() 
          	������ƶ����� ResultSet ����ĵ�һ��
	    boolean last() 
          	������ƶ����� ResultSet ��������һ�� 
	    void deleteRow() 
                �Ӵ� ResultSet ����͵ײ����ݿ���ɾ����ǰ�У��õ��Ķ���Ҫ����Statement ��������

####     ResultSetMetaData getMetaData() 

��ȡ�� ResultSet ����Ľ�������ݱ�Ԫ���ݣ��кš����ͺ����� 

getXXX������������
setXXX������������
����
int getInt(int columnIndex) 
	 �� Java ��������� int ����ʽ��ȡ�� ResultSet ����ĵ�ǰ����ָ������ŵ�ֵ
int getInt(String columnLabel) 
	 �� Java ��������� int ����ʽ��ȡ�� ResultSet ����ĵ�ǰ����ָ��������ֵ

ע�⣺
	1��ResultSet �� ��û�й�ϵ��ֻ�� select ����й�ϵ
	2��SQL�����ƴ�ӣ�"+ +"
	3������ ' ���ŵ��ַ������ܻ�ı�SQL���Ľṹ������ʹ��PreparedStatement�ӿڣ��ȷ���Ԥ����ĸ�ʽ���ٷ���������ȫ



##### ResultSetMetaData�ӿڣ���װ�� ResultSet �����ṹ��ԭ������Ϣ

getColunmCount()��ȡ������ܹ��м���
getColumnName(i)��ȡָ���е����ֳ�
getColumnType(i)��ȡָ���е�����

##### DatabaseMetaData�ӿڣ���װ�����ݿ��ԭ���� 

ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) 
��ȡ���ڸ��������ʹ�õı����������������ֱ�Ӵ�null










