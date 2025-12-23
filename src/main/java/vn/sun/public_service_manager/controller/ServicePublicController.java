package vn.sun.public_service_manager.controller;

import vn.sun.public_service_manager.entity.Service;
import vn.sun.public_service_manager.service.ServiceManagementService;
import vn.sun.public_service_manager.service.ServiceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/services")
public class ServicePublicController {

    private final ServiceManagementService serviceManagementService;
    private final ServiceTypeService serviceTypeService;

    @GetMapping({"/list", "/"})
    public String listServices(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long serviceTypeId)
    {

        Page<Service> servicePage = serviceManagementService.getServices(
                page, size, sortBy, sortDir, keyword, serviceTypeId);

        model.addAttribute("servicePage", servicePage);
        model.addAttribute("serviceTypes", serviceTypeService.getAllServiceTypes());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", servicePage.getTotalPages());
        model.addAttribute("totalItems", servicePage.getTotalElements());

        model.addAttribute("keyword", keyword);
        model.addAttribute("serviceTypeId", serviceTypeId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "admin/service_list";
    }
}