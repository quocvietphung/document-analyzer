package orgaplan.beratung.kreditunterlagen.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.response.ApiResponse;
import orgaplan.beratung.kreditunterlagen.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse> sayHello() {
        return new ResponseEntity<>(new ApiResponse(true, "Hello, World!", null), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        User user = userService.login(username, password);
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("user_id", user.getId());
            data.put("username", user.getUsername());
            return new ResponseEntity<>(new ApiResponse(true, "Login successful", data), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ApiResponse(false, "Invalid username or password", null), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/createUser")
    public ResponseEntity<ApiResponse> createUser(@RequestBody User user) {
        User newUser = userService.createUser(user);
        return new ResponseEntity<>(new ApiResponse(true, "User created successfully", newUser), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getUsers() {
        List<User> users = userService.getUsers();
        return new ResponseEntity<>(new ApiResponse(true, "Users retrieved successfully", users), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return new ResponseEntity<>(new ApiResponse(true, "User retrieved successfully", user), HttpStatus.OK);
    }

    @PutMapping("/editUser/{id}")
    public ResponseEntity<ApiResponse> editUser(@PathVariable String id, @RequestBody User updatedUserFields) {
        User updatedUser = userService.editUser(id, updatedUserFields);
        return new ResponseEntity<>(new ApiResponse(true, "User updated successfully", updatedUser), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(new ApiResponse(true, "User deleted successfully", null), HttpStatus.NO_CONTENT);
    }
}
