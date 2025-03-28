### Chrome DevTools

------

[TOC]

##### 01：面板概述

- **Elements**：查找网页源代码HTML中的任一元素，**手动修改任一元素的属性和样式且能实时在浏览器里面得到反馈**。
- **Console**：记录开发者开发过程中的日志信息，且可以作为与JS进行交互的命令行Shell。
- **Sources：**Sources 功能面板是资源面板，他主要分为四个部分，四个部分并不是独立的，他们互相关联，互动共同实现一个重要的功能：**监控js在执行期的活动**。简单来说就是**断点**啦。
- **Network：**从发起网页页面请求 Request 后分析HTTP请求后得到的各个请求资源信息（包括状态、资源类型、大小、所用时间等），可以根据这个进行网络性能优化。
- **Performance**：记录JS CPU执行时间细节、显示JS对象和相关的DOM节点的内存消耗、记录内存的分配细节；
- **Application：**记录网站加载的所有资源信息，包括存储数据（Local Storage、Session Storage、IndexedDB、Web SQL、Cookies）、缓存数据、字体、图片、脚本、样式表等；
- **Security**：判断当前网页是否安全；

##### 02：元素（Element）面板

- 右键检查，打开Elements面板；

###### 常用工能

- 实时编辑 DOM节点： 
  - 在Element面板的DOM树视图中，呈现出了当前页面中的所有的DOM节点，鼠标**双击任何DOM节点都可以修改其中的属性值，修改完成之后按回车键**浏览器会立即显示出修改后的效果；
- 实时编辑CSS样式：
  - 通过先点击开发者工具面板最左侧的放大镜,然后再去**点击页面上要修改的DOM元素**选中这个要调试的DOM元素，此时**控制面板右侧的Style**中就呈现出了当前选中的DOM元素的**CSS属性双击属性值**即可修改；
- 打开盒子模型，调试边框参数：
  - 点击右侧的 Computed 页签可以看到当前选中的元素的盒子模型参数，所有的值都是可以修改的，点击不同的位置(top,bottom,left,right)就可以修改元素的padding,border,margin属性值；

##### 03：控制台（Console） 面板

- 查看脚本运行过程中的异常信息
  - 只有运行结束后才会抛出异常信息到控制台，如果想查看具体的异常信息，直接点击右边的异常信息控制台将会把我们带到程序中错误出现的具体位置；
- 打印日志信息
  - console.log(“info”)：显示一般的基本日志信息；
  - console.clear()：清楚控制台日志；
- 运行JavaScript脚本
  - 可以运行你输入的JavaScript脚本；

##### 04：资源（Source）面板

- 在Source面板中可以找到当前浏览器加载的页面，然后对其中的**JavaScript脚本进行断点调试**；

##### 05：网络（NetWork）面板

- NetWork面板可以记录页面上的网络请求的详细信息，从发起网页请求Request后分析HTTP请求后得到的各个请求资源信息；

###### 五部分组成

- **Controls：**控制Network的外观和功能；

- **Filters：**控制Requests Table具体显示哪些内容；

  - Preserve log：跳转页面或者刷新页面还保留之前发起的请求详情。可以用来追踪一些接口。
  - **Disable cache**：在调试一些加载时间或者性能的时候需要用到，**禁止从缓存中加载**。
  - No throttling：用来设置网络带宽，设置UA，模拟网络请求。

- **Overview：**显示获取到资源的时间轴信息；

  - Show overview：控制时间轴的展示；
  - Capture screenshots：控制抓图信息的展示；

- **Requests Table：**按资源获取的前后顺序显示所有获取到的资源信息，点击资源名可以查看该资源的详细信息；

  - Name：资源名称，点击名称可以查看资源的详情情况，包括Headers、**Preview**、Response、**Cookies**、Timing。

  - Status：HTTP状态码。

  - Type：请求的资源MIME类型。

  - Initiator：标记请求是由哪个对象或进程发起的（请求源）。

  - - Parser：请求由Chrome的HTML解析器时发起的。
    - Redirect：请求是由HTTP页面重定向发起的。Script：请求是由Script脚本发起的。
    - Other：请求是**由其他进程发起**的，比如用户点击一个链接跳转到另一个页面或者在地址栏输入URL地址。

  - Size：从服务器下载的文件和请求的资源大小。如果是从缓存中取得的资源则该列会显示(from cache)

  - Time：请求或下载的时间，从发起Request到获取到Response所用的总时间。

  - **Timeline**：显示**所有网络请求的可视化瀑布流（时间状态轴）**，点击时间轴，可以查看该请求的详细信息。

    - queueing：排队，浏览器给每个域名都限制了连接最大的数量，超过了就要排队等待。
    - stalled：停滞，发起连接之前，有些原因使得连接过程被推迟，主要就是**TCP的连接检测阶段**。
    - Proxy Negotiation：**代理协商阶段**，代理服务器连接协商所用的时间。
    - DNS Lookup：域名解析；
    - **Initial connection**：初始化连接，和服务器建立连接的阶段，这包括了**建立 TCP 连接所花费的时间**。
    - SSL：使用https才有，额外的 SSL 握手时间；
    - Request sent：数据发送，通常这个阶段非常快，因为**只需要把浏览器缓冲区的数据发送出去就结束了**，并不需要判断服务器是否接收到了，所以这个时间通常不到 1 毫秒。
    - **Waiting (TTFB)**：通常称为“第一字节时间”，TTFB 是反映服务端响应速度的重要指标，从发送请求到收到响应之间的空隙；
    - **Content Download**：从接收到第一个字节开始到陆续接收完整数据的阶段，这意味着从第一字节时间到接收到全部响应数据所用的时间。如果时间很长，那就是文件太大。

- **Summary：**显示总的请求数、数据传输量、加载时间信息；

  - requests：是请求数量；
  - transferred：是通过网络加载的资源大小；
  - **resources**：页面加载所有资源大小；
  - **finish**：是所有请求发起到响应完成的时间，一般来说是比Load大；
  - Load：当整个页面及所有依赖资源如样式表和图片都已完成加载时，将触发load事件。



