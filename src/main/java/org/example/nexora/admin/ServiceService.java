package org.example.nexora.admin;

import org.example.nexora.common.BusinessException;
import org.example.nexora.admin.dto.CreateServiceRequest;
import org.example.nexora.admin.dto.UpdateServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    // 📋 GET ALL SERVICES (PAGINATED)
    @Transactional(readOnly = true)
    public Page<org.example.nexora.admin.Service> getAllServices(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return serviceRepository.findAll(pageable);
    }

    // 🔍 GET SERVICE BY ID
    @Transactional(readOnly = true)
    public org.example.nexora.admin.Service getServiceById(Long id) {
        return serviceRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Service not found", "SERVICE_NOT_FOUND"));
    }

    // 🔍 GET SERVICE BY NAME
    @Transactional(readOnly = true)
    public org.example.nexora.admin.Service getServiceByName(String name) {
        return serviceRepository.findByName(name)
            .orElseThrow(() -> new BusinessException("Service not found", "SERVICE_NOT_FOUND"));
    }

    // 🔍 SEARCH SERVICES WITH FILTERS
    @Transactional(readOnly = true)
    public Page<org.example.nexora.admin.Service> searchServices(String name, String category, ServiceStatus status, 
                                       Boolean isPublic, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return serviceRepository.findByFilters(name, category, status, isPublic, pageable);
    }

    // ➕ CREATE NEW SERVICE
    public org.example.nexora.admin.Service createService(CreateServiceRequest request) {
        // Check if service name already exists
        if (serviceRepository.findByName(request.getName()).isPresent()) {
            throw new BusinessException("Service name already exists", "SERVICE_NAME_EXISTS");
        }

        org.example.nexora.admin.Service service = new org.example.nexora.admin.Service();
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setCategory(request.getCategory());
        service.setApiEndpoint(request.getApiEndpoint());
        service.setVersion(request.getVersion());
        service.setStatus(ServiceStatus.ACTIVE);
        service.setIsPublic(request.getIsPublic());
        service.setRequiresAuth(request.getRequiresAuth());
        service.setRateLimit(request.getRateLimit());
        service.setConfig(request.getConfig());
        service.setDocumentationUrl(request.getDocumentationUrl());
        service.setContactEmail(request.getContactEmail());

        return serviceRepository.save(service);
    }

    // ✏️ UPDATE SERVICE
    public org.example.nexora.admin.Service updateService(Long id, UpdateServiceRequest request) {
        org.example.nexora.admin.Service existingService = getServiceById(id);

        // Check if name is being changed and if new name already exists
        if (request.getName() != null && !request.getName().equals(existingService.getName())) {
            if (serviceRepository.findByName(request.getName()).isPresent()) {
                throw new BusinessException("Service name already exists", "SERVICE_NAME_EXISTS");
            }
            existingService.setName(request.getName());
        }

        // Update fields if provided
        if (request.getDescription() != null) {
            existingService.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            existingService.setCategory(request.getCategory());
        }
        if (request.getApiEndpoint() != null) {
            existingService.setApiEndpoint(request.getApiEndpoint());
        }
        if (request.getVersion() != null) {
            existingService.setVersion(request.getVersion());
        }
        if (request.getStatus() != null) {
            existingService.setStatus(request.getStatus());
        }
        if (request.getIsPublic() != null) {
            existingService.setIsPublic(request.getIsPublic());
        }
        if (request.getRequiresAuth() != null) {
            existingService.setRequiresAuth(request.getRequiresAuth());
        }
        if (request.getRateLimit() != null) {
            existingService.setRateLimit(request.getRateLimit());
        }
        if (request.getConfig() != null) {
            existingService.setConfig(request.getConfig());
        }
        if (request.getDocumentationUrl() != null) {
            existingService.setDocumentationUrl(request.getDocumentationUrl());
        }
        if (request.getContactEmail() != null) {
            existingService.setContactEmail(request.getContactEmail());
        }

        return serviceRepository.save(existingService);
    }

    // DELETE SERVICE
    public void deleteService(Long id) {
        org.example.nexora.admin.Service service = getServiceById(id);
        serviceRepository.delete(service);
    }

    // TOGGLE SERVICE STATUS
    public org.example.nexora.admin.Service toggleServiceStatus(Long id) {
        org.example.nexora.admin.Service service = getServiceById(id);
        if (service.getStatus() == ServiceStatus.ACTIVE) {
            service.setStatus(ServiceStatus.INACTIVE);
        } else {
            service.setStatus(ServiceStatus.ACTIVE);
        }
        return serviceRepository.save(service);
    }

    // 📊 GET SERVICE STATISTICS
    @Transactional(readOnly = true)
    public Map<String, Object> getServiceStatistics() {
        Map<String, Object> stats = Map.of(
            "totalServices", serviceRepository.count(),
            "activeServices", serviceRepository.countByStatus(ServiceStatus.ACTIVE),
            "inactiveServices", serviceRepository.countByStatus(ServiceStatus.INACTIVE),
            "maintenanceServices", serviceRepository.countByStatus(ServiceStatus.MAINTENANCE),
            "deprecatedServices", serviceRepository.countByStatus(ServiceStatus.DEPRECATED),
            "publicServices", serviceRepository.countByIsPublic(true),
            "privateServices", serviceRepository.countByIsPublic(false)
        );
        return stats;
    }

    // 📂 GET ALL CATEGORIES
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return serviceRepository.findAllCategories();
    }

    // GET SERVICES BY CATEGORY
    @Transactional(readOnly = true)
    public List<Service> getServicesByCategory(String category) {
        return serviceRepository.findByCategory(category);
    }

    // BULK UPDATE SERVICE STATUS
    public int bulkUpdateServiceStatus(List<Long> serviceIds, ServiceStatus status) {
        List<org.example.nexora.admin.Service> services = serviceRepository.findAllById(serviceIds);
        services.forEach(service -> service.setStatus(status));
        serviceRepository.saveAll(services);
        return services.size();
    }

    // GET SERVICES BY STATUS
    @Transactional(readOnly = true)
    public List<Service> getServicesByStatus(ServiceStatus status) {
        return serviceRepository.findByStatus(status);
    }

    // 🔒 GET PUBLIC SERVICES
    @Transactional(readOnly = true)
    public List<Service> getPublicServices() {
        return serviceRepository.findByIsPublic(true);
    }

    // 🔒 GET PRIVATE SERVICES
    @Transactional(readOnly = true)
    public List<Service> getPrivateServices() {
        return serviceRepository.findByIsPublic(false);
    }
}
