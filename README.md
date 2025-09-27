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