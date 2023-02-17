### Spring-Bean 的加载

------

[TOC]

##### 01：Bean 的加载核心流程

- 核心类：org.springframework.beans.factory.support.AbstractBeanFactory#doGetBean
- 

###### 转换对应beanName

- 将别名转换为最基本的beanName值；

###### 尝试从缓存中加载单例

- **尝试加载：**首先尝试从缓存中加载，如果加载不成功则再次尝试从 singletonFactories 中加载
  - 在创建单例bean的时候会存在依赖注入的情况，而在创建依赖的时候**为了避免循环依赖**，在Spring中创建bean的原则是**不等 bean 创建完成就会将创建 bean 的ObjectFactory提早曝光加入到缓存中，一旦下一个 bean 创建时候需要依赖上一个bean 则直接使用ObjectFactory**。

###### bean的实例化 

- 如果从缓存中得到了bean的原始状态，则需要对bean进行实例化。这里有必要强调一下，缓存中记录的只是最原始的bean状态，并不一定是我们最终想要的bean。

###### 原型模式的依赖检查 

- 只有在单例情况下才会尝试解决循环依赖，如果存在A中有B的属性，B中有A的属性，那么当依赖注入的时候，就会产生当A还未创建完的时候，因为对于B的创建再次返回创建A，造成循环依赖， 也就是情况： isPrototypeCurrentlyInCreation(beanName)判断true。

###### 检测parentBeanFactory 

- 重要的判断条件： parentBeanFactory != null && !containsBean Definition (beanName)，parentBeanFactory != null。 !containsBeanDefinition(beanName)，它是在检测如果当前加载的XML配置文件中不包含beanName所对应的配置，就只能到 parentBeanFactory去尝试下了，然后再去递归的调用getBean方法。

###### 将存储XML配置文件的GernericBeanDefinition

- 转换为RootBeanDefinition，因为从XML配置文件中读取到的bean信息是存储在GernericBeanDefinition中的，但是所有的bean 后续处理都是针对于 **RootBeanDefinition** 的，所以这 里需要进行一个转换，转换的同时如果父类bean不为空的话，则会合并父类的属性。

###### 寻找依赖，递归实例化依赖的Bean

- 因为bean的初始化过程中很可能会用到某些属性，而某些属性很可能是动态配置的，并且配置成依赖于其他的bean，那么这个时候就有必要先加载依赖的bean，所以，**在Spring的加载顺序中，在初始化某一个bean的时候首先会初始化这个bean所对 应的依赖**。

###### 针对不同的scope进行bean的创建

- 在Spring中存在着不同的scope， 其中默认的是singleton，但是还有些其他的配置诸 如prototype、request之类的。在这个步骤中， Spring会根据不同的配置进行不同的初始化策略。

###### 类型转换

- 到这里返回bean后已经基本结束了，通常对该方法的调用参数 requiredType 是为空的，但是可能会存在这样的情况，返回的bean其实是个 String，但是requiredType却传入Integer类型，那么 这时候本步骤就会起作用了，它的功能是**将返回的 bean转换为requiredType所指定的类型**。

##### 02：FactoryBean 的使用

- Spring提供了一个 org.Springframework.bean.factory.FactoryBean的工厂类接口，用户可以通过**实现该接口定制实例化bean** 的逻辑。























