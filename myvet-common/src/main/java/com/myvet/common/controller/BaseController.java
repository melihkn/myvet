// myvet-common/src/main/java/com/myvet/common/controller/BaseController.java
package com.myvet.common.controller;

import com.myvet.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;

public abstract class BaseController<T> {

    protected ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    protected ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(201).body(ApiResponse.success(data));
    }

    protected ResponseEntity<ApiResponse<Void>> deleted() {
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted successfully"));
    }
}