### KindEditor��

### ��һ�׿�Դ������HTML�༭������Ҫ�������û�����վ�ϻ�����������ñ༭Ч����������Ա������ KindEditor ���Ѵ�ͳ�Ķ����ı������(textarea)�滻Ϊ���ӻ����ı������ʹ��js ��д

URL��http://kindeditor.net

```javascript
ʹ�÷�������Jsp��
1���Ȳ���KindEditor�ļ���---Web-Content

2��<textarea id="editor_id" name="content" style="width:700px;height:300px;">
</textarea>

3��<script charset="utf-8" src="/editor/kindeditor.js"></script>

<script charset="utf-8" src="/editor/lang/zh-CN.js"></script>
<script>
KindEditor.ready(function(K) {
window.editor = K.create('#editor_id');
});
</script>
```



##### 4��ȡ��HTML������

?	html = $('#editor_id').val(); // jQuery
   ����HTML����
?	editor.html('HTML����');


ע�⣺ K.create�����ĵڶ������������ԶԱ༭����������
	
1��cssPath
	ָ���༭��iframe document��CSS�ļ����������ÿ��ӻ��������ʽ
2��uploadJson
	ָ���ϴ��ļ��ķ������˳���
3��allowFileManager
	trueʱ��ʾ���Զ�̷�������ť
4��fileManagerJson
	ָ�����Զ��ͼƬ�ķ������˳���

```js
����KindEditor.ready(function(K) {
                window.editor = K.create('#${mis.field}_id',{			
                uploadJson : 'admin/uploadpic/imgupload',
    		allowFileManager : false

	       });
});
```



