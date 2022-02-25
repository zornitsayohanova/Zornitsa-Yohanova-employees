package com.example.employeeapp.web.controllers;

import com.example.employeeapp.data.Project;
import com.example.employeeapp.services.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/")
    public String viewIndexPage() {

        return "index";
    }

    @GetMapping("/send-file")
    public String getEmployeesPage(Model model) {

        model.addAttribute("pair", "");
        model.addAttribute("pairDays", 0);
        model.addAttribute("pairProjects", new ArrayList<Project>());

        return "employees";
    }

    @PostMapping("/send-file")
    public String getEmployees(Model model, @RequestParam MultipartFile file) {
        projectService.extractDataFromFile(file);
        projectService.makeEmployeePairs();

        Map<String, Integer> employeePairsWorkDays = projectService.getEmployeePairsWorkDays();
        String pair = projectService.getPairWithMaxDays(employeePairsWorkDays);
        int pairDays = projectService.getPairDays(employeePairsWorkDays, pair);
        List<Project> pairProjects = projectService.getPairProjects(projectService.getEmployeePairsProjects(), pair);

        model.addAttribute("pair", pair);
        model.addAttribute("pairDays", pairDays);
        model.addAttribute("pairProjects", pairProjects);

        return "employees";
    }
}
