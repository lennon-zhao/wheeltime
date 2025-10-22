# TimeWheel - 时间轮实现

基于Spring Boot的时间轮（Time Wheel）实现，用于高效管理大量延时任务。

## 项目特性

- 使用Maven进行项目管理
- 基于Spring Boot框架
- 高效的时间轮算法实现
- 支持自定义配置
- 提供REST API接口

## 时间轮原理

时间轮是一个环形数据结构，类似时钟，分为多个槽位（bucket）。每个槽位存储到期时间相同的任务列表。时间轮以固定的时间间隔（tick）转动，当指针指向某个槽位时，执行该槽位中的所有任务。

**优势：**
- O(1) 时间复杂度添加任务
- 高效处理大量延时任务
- 内存占用可控

## 快速开始

### 1. 编译项目

```bash
mvn clean package
```

### 2. 运行应用

```bash
mvn spring-boot:run -f "E:\project\wheeltime\pom.xml"
```

或

```bash
java -jar target/timewheel-1.0.0.jar
```

### 3. 测试时间轮

应用启动后，访问以下接口：

**添加延时任务：**
```bash
curl -X POST "http://localhost:8080/api/timewheel/task?delayMs=3000&message=HelloWorld"
```

**查看时间轮状态：**
```bash
curl http://localhost:8080/api/timewheel/status
```

## 配置说明

在 `application.properties` 中配置时间轮参数：

```properties
# 时间轮每个刻度的时间间隔（毫秒）
timewheel.tick-duration=100

# 时间轮的槽位数量
timewheel.wheel-size=60

# 任务执行线程池大小
timewheel.task-thread-pool-size=10
```

## 代码使用示例

```java
@Autowired
private TimeWheel timeWheel;

public void scheduleTask() {
    TimedTask task = new SimpleTimedTask(() -> {
        System.out.println("Task executed!");
    }, 5000); // 5秒后执行
    
    timeWheel.addTask(task);
}
```

## 项目结构

```
wheeltime/
├── src/
│   ├── main/
│   │   ├── java/com/wheeltime/timewheel/
│   │   │   ├── TimeWheelApplication.java      # Spring Boot主类
│   │   │   ├── TimeWheel.java                 # 时间轮核心实现
│   │   │   ├── TimedTask.java                 # 定时任务接口
│   │   │   ├── SimpleTimedTask.java           # 简单任务实现
│   │   │   ├── TimeWheelConfig.java           # Spring配置
│   │   │   └── TimeWheelController.java       # REST控制器
│   │   └── resources/
│   │       └── application.properties          # 应用配置
│   └── test/
│       └── java/
├── pom.xml                                     # Maven配置
└── README.md
```

## 技术栈

- Java 8
- Spring Boot 2.7.18
- Maven
- Lombok

## API 接口

### 添加延时任务
- **URL:** `POST /api/timewheel/task`
- **参数:**
  - `delayMs`: 延时时间（毫秒），默认1000
  - `message`: 任务消息内容
- **响应:** JSON格式的任务添加结果

### 查看时间轮状态
- **URL:** `GET /api/timewheel/status`
- **响应:** 
  ```json
  {
    "running": true,
    "currentIndex": 25,
    "wheelSize": 60,
    "tickDuration": 100
  }
  ```
