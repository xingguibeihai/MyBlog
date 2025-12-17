// cuit.myblog.controller.FileManagementController.java (新文件或现有文件)
package cuit.myblog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/files")
public class FileManagementController {

    // 只有角色为 'ADMIN' 的用户才能执行此操作
    // 注意：在 hasRole() 中，不需要写 ROLE_ 前缀
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile() {
        return new ResponseEntity<>("File uploaded by Admin.", HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContent(@PathVariable Long id) {
        return new ResponseEntity<>("Content deleted by Admin.", HttpStatus.OK);
    }
}