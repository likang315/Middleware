### 组合模式 (Composite)：也称部分-整体模式，属于结构型模式

------

[TOC]

##### 00：概述

- 将**一些相似的对象看成一个对象处理**，将对象组合成树形结构，创建了对象组的树形结构，区分容器对象和叶子对象，都当成容器对象处理，使得用户对单个对象和组合对象的使用具有一致性；

###### 关键代码：

​	树枝内部组合该接口，并且含有内部属性 List，里面放 Component

##### 01：示例

文件系统，每个目录都可以装内容，目录的内容可以是文件，也可以是目录

```java
public abstract class RootFile {
  private String name;
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public RootFile() {
  }
  public RootFile(String name) {
    this.name=name;
  }

  public abstract void printName();
}
public class Folder extends RootFile {
  private List<RootFile> list = new ArrayList<RootFile>();
  public Folder() {
  }
  
  public Folder(String name) {
    super(name);
  }
  public void add(RootFile t) {
    arraylist.add(t);
  }
  @Override
  public void printName() {
    System.out.println("File name:" + super.getName());
    if(null != list) {
      // 是文件调用文件的printName ，若是文件夹调用文件夹的printName
      for (RootFile rf:arraylist) {
        rf.printName();
      }
    }
  }
}

public class File extends RootFile {
  public File() {
    super();
  }
  public File(String name){
    super(name);
  }
  @Override
  public void printName() {
    System.out.println("File name:"+super.getName());
  }
}

package com.Compsite;
public class Client {
  public static void main(String[] args) {
    File file1=new File("1.java");
    File file2=new File("2.java");
    File file3=new File("3.java");
    Folder root = new Folder("F:\\bb");
    Folder f = new Folder("aa");
    root.add(file1);
    root.add(file2);
    root.add(f);
    f.add(file3);

    root.printName();
  }
}
```