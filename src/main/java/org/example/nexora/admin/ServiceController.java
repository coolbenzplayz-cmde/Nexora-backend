package org.example.nexora.admin;

import org.example.nexora.admin.Service;
import org.example.nexora.admin.dto.CreateServiceRequest;
import org.example.nexora.admin.dto.UpdateServiceRequest;
import org.example.nexora.common.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/services")
@PreAuthorize("hasRole('ADMIN')")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    // 📋 GET ALL SERVICES (PAGINATED)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Service>>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Page<Service> services = serviceService.getAllServices(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    // 🔍 GET SERVICE BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Service>> getServiceById(@PathVariable Long id) {
        Service service = serviceService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.success(service));
    }

    // 🔍 SEARCH SERVICES WITH FILTERS
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<Service>>> searchServices(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) ServiceStatus status,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<Service> services = serviceService.searchServices(name, category, status, isPublic, page, size);
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    // ➕ CREATE NEW SERVICE
    @PostMapping
    public ResponseEntity<ApiResponse<Service>> createService(@Valid @RequestBody CreateServiceRequest request) {
        Service service = serviceService.createService(request);
        return ResponseEntity.ok(ApiResponse.success(service));
    }

    // ✏️ UPDATE SERVICE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Service>> updateService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request) {
        
        Service service = serviceService.updateService(id, request);
        return ResponseEntity.ok(ApiResponse.success(service));
    }

    // 🗑️ DELETE SERVICE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success("Service deleted successfully"));
    }

    // 🔄 TOGGLE SERVICE STATUS
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Service>> toggleServiceStatus(@PathVariable Long id) {
        Service service = serviceService.toggleServiceStatus(id);
        return ResponseEntity.ok(ApiResponse.success(service));
    }

    // 📊 GET SERVICE STATISTICS
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getServiceStatistics() {
        Map<String, Object> stats = serviceService.getServiceStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // 📂 GET ALL CATEGORIES
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
        List<String> categories = serviceService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    // 📂 GET SERVICES BY CATEGORY
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Service>>> getServicesByCategory(@PathVariable String category) {
        List<Service> services = serviceService.getServicesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    // 📈 GET SERVICES BY STATUS
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Service>>> getServicesByStatus(@PathVariable ServiceStatus status) {
        List<Service> services = serviceService.getServicesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    // 🔒 GET PUBLIC SERVICES
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<Service>>> getPublicServices() {
        List<Service> services = serviceService.getPublicServices();
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    // 🔒 GET PRIVATE SERVICES
    @GetMapping("/private")
    public ResponseEntity<ApiResponse<List<Service>>> getPrivateServices() {
        List<Service> services = serviceService.getPrivateServices();
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    // 🔄 BULK UPDATE SERVICE STATUS
    @PatchMapping("/bulk-status")
    public ResponseEntity<ApiResponse<String>> bulkUpdateServiceStatus(
            @RequestBody List<Long> serviceIds,
            @RequestParam ServiceStatus status) {
        
        int updatedCount = serviceService.bulkUpdateServiceStatus(serviceIds, status);
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Updated %d services to status %s", updatedCount, status)));
    }

    // 🔍 GET SERVICE BY NAME
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<Service>> getServiceByName(@PathVariable String name) {
        Service service = serviceService.getServiceByName(name);
        return ResponseEntity.ok(ApiResponse.success(service));
    }
}
