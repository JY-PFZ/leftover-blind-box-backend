### 获取当前请求用户信息
1. 注入UserContextHolder
事例：
   @RestController
   @RequestMapping("/api/user")
   @RequiredArgsConstructor
   public class UserController {

   private final IUserService userService;
   private final UserContextHolder userContextHolder;

   @GetMapping("/hello")
   public Result<String> hello(){
   UserContext currentUser = userContextHolder.getCurrentUser();
   return Result.success("hello spring security: " + currentUser.toString());
   }
}

前端	Vue.js	快速开发、组件化、生态好
后端	Spring Boot	成熟稳定、集成强、易维护
认证鉴权	Spring Security + JWT	安全、无状态、多角色支持
数据库	MySQL	稳定、事务支持、易上手
ORM	MyBatis + MP	灵活+高效，兼顾控制与速度
缓存	Redis	高性能，为未来扩展准备
日志系统：SLF4J + Logback
部署与运维：Docker
接口文档管理：SpringDoc



## V1.3

1. 实现一个完整的 **CI/CD 流水线**，涵盖：

   - 代码构建与单元测试
   - 静态应用安全测试（SAST）
   - 容器化构建与推送
   - 自动部署到 AWS EC2
   - 动态应用安全测试（DAST）

2. AWS S3 作为存储解决方案

   
