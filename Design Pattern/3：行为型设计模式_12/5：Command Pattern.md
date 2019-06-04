### 命令模式（Command Pattern）：属于行为型模式

​	请求以命令的形式包裹在对象中，并传给调用对象，调用对象寻找可以处理该命令的合适的对象，并把该命令传给相应的对象，该对象执行命令

###### 三个角色：

- Command：命令
- received ：命令的执行对象
- invoker：命令的调用者

###### 示例：

 	GUI 中每一个按钮都是一条命令

```java
//命令接口
public interface Order {
   void execute();
}
//不同的命令执行者，实现类不同
public class BuyStock implements Order {
   public void execute() {
     Ststem.out.println(" buy order excute....");
   }
}

public class SellStock implements Order {
   public void execute() {
     Ststem.out.println(" sell order excute....");
   }
}

//命令的调用者
public class Invoker {
   //存储命令的容器
   private List<Order> orderList = new ArrayList<Order>(); 
   //添加命令
   public void addOrder(Order order){
      orderList.add(order);      
   }
 
   public void placeOrders(){
      for (Order order : orderList) {
         order.execute();
      }
      orderList.clear();
   }
}

public class Calient {
   public static void main(String[] args) {
      Broker broker = new Broker();
      //请求以命令的方式封装在对象中
      broker.takeOrder(new BuyStock());
      broker.takeOrder(new SellStock());
      broker.placeOrders();
   }
}
```

