package com.example.employeeapp.services;

import com.example.employeeapp.data.Employee;
import com.example.employeeapp.data.Project;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@AllArgsConstructor
public class ProjectService {
    public static final String delimiter = ", ";
    Map<Integer, ArrayList<Employee>> employees;
    Map<String, Integer> employeePairsWorkDays;
    Map<String, ArrayList<Project>>  employeePairsProjects;

    public void extractDataFromFile(MultipartFile file) {
        employees = new HashMap<>();

        BufferedReader bufferRead;
        try {
            String line;
            InputStream inputStream = file.getInputStream();
            bufferRead = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = bufferRead.readLine()) != null) {
                this.addEmployeeToList(line);
            }
        } catch (IOException | ParseException e) {
            System.err.println(e.getMessage());
        }
    }

    public void addEmployeeToList(String line) throws ParseException {
        String[] results = line.split(delimiter);
        Employee employee = new Employee();
        employee.setEmployeeId(results[0]);
        employee.setProjectId(Integer.parseInt(results[1]));
        employee.setDateFrom(new SimpleDateFormat("yyyy-MM-dd").parse(results[2]));

        if (results[3].equals("NULL")) {
            this.setForNull(employee);
        } else {
            employee.setDateTo(new SimpleDateFormat("yyyy-MM-dd").parse(results[3]));
        }

        if (employees.containsKey(employee.getProjectId())) {
            employees.get(employee.getProjectId()).add(employee);
        } else {
            ArrayList<Employee> projectTeam = new ArrayList<>();
            projectTeam.add(employee);
            employees.put(Integer.parseInt(results[1]), projectTeam);
        }
    }

    public void setForNull(Employee employee) throws ParseException {
        LocalDate date = LocalDate.now();
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);

        employee.setDateTo(new SimpleDateFormat("yyyy-MM-dd").parse(sqlDate.toString()));
    }

    public void makeEmployeePairs() {
        employeePairsWorkDays = new HashMap<>();
        employeePairsProjects = new HashMap<>();

        for (Map.Entry<Integer, ArrayList<Employee>> currentProject : employees.entrySet()) {

            List<Employee> projectEmployees = currentProject.getValue();

            for (int i = 0; i < projectEmployees.size(); i++) {
                Employee currentEmployee = currentProject.getValue().get(i);
                for (int j = i + 1; j < projectEmployees.size(); j++) {

                    Employee nextEmployee = currentProject.getValue().get(j);
                    String pairIds = currentEmployee.getEmployeeId() + ", " + nextEmployee.getEmployeeId();
                    int pairWorkDays = this.getPairWorkDays(currentEmployee, nextEmployee);

                    this.addPairToPairWorksDaysList(pairIds, pairWorkDays, employeePairsWorkDays);
                    this.addPairToPairProjectsList(pairIds, pairWorkDays, currentEmployee.getProjectId(), employeePairsProjects);
                }
            }
        }
    }

    public int getPairWorkDays(Employee currentEmployee, Employee nextEmployee) {

        long numberOfOverlappingDates;
        if (currentEmployee.getDateTo().before(nextEmployee.getDateFrom()) ||
                nextEmployee.getDateTo().before(currentEmployee.getDateFrom())) {
            return 0;
        } else {
            LocalDate fromFirstDate = currentEmployee.getDateFrom().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate toFirstDate = currentEmployee.getDateTo().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            LocalDate fromSecondDate = nextEmployee.getDateFrom().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate toSecondDate = nextEmployee.getDateTo().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate start = Collections.max(Arrays.asList(fromFirstDate, fromSecondDate));
            LocalDate end = Collections.min(Arrays.asList(toFirstDate, toSecondDate));
            numberOfOverlappingDates = ChronoUnit.DAYS.between(start, end);

            return Integer.parseInt(String.valueOf(numberOfOverlappingDates));
        }
    }

    public void addPairToPairWorksDaysList(String pairIds, int pairWorkDays, Map<String, Integer> employeePairsWorkDays) {
        if (employeePairsWorkDays.containsKey(pairIds)) {
            Integer oldWorkDaysValue = employeePairsWorkDays.get(pairIds);
            employeePairsWorkDays.put(pairIds, oldWorkDaysValue + pairWorkDays);
        } else {
            employeePairsWorkDays.put(pairIds, pairWorkDays);
        }
    }

    public void addPairToPairProjectsList(String pairIds, int pairWorkTimeDays, int projectId,
                                          Map<String, ArrayList<Project>> employeePairsProjects) {
        String[] pairIdsArray = pairIds.split(", ");

        Project project = new Project();
        project.setFirstEmployeeId(pairIdsArray[0]);
        project.setSecondEmployeeId(pairIdsArray[1]);
        project.setProjectId(projectId);
        project.setWorkTimeDays(pairWorkTimeDays);

        if (employeePairsProjects.containsKey(pairIds)) {
            ArrayList<Project> pairProjects = employeePairsProjects.get(pairIds);
            pairProjects.add(project);
            employeePairsProjects.put(pairIds, pairProjects);
        } else {
            employeePairsProjects.put(pairIds, new ArrayList<>(Collections.singletonList(project)));
        }
    }

    public String getPairWithMaxDays(Map<String, Integer> employeePairsWorkDays) {
        return employeePairsWorkDays.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
    }

    public Integer getPairDays(Map<String, Integer> employeePairsWorkDays, String pairIds) {

        Map.Entry<String, Integer> pair = employeePairsWorkDays.entrySet().stream()
                .filter(e -> pairIds.equals(e.getKey()))
                .findFirst()
                .orElse(null);

        assert pair != null;
        return pair.getValue();
    }

    public ArrayList<Project> getPairProjects(Map<String, ArrayList<Project>> employeePairsProjects, String pairIds) {
        Map.Entry<String, ArrayList<Project>> pair = employeePairsProjects.entrySet().stream()
                .filter(e -> pairIds.equals(e.getKey()))
                .findFirst()
                .orElse(null);

        assert pair != null;
        return pair.getValue();
    }

    public Map<String, Integer> getEmployeePairsWorkDays() {
        return employeePairsWorkDays;
    }

    public Map<String, ArrayList<Project>> getEmployeePairsProjects() {
        return employeePairsProjects;
    }
}
