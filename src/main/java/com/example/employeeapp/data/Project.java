package com.example.employeeapp.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private String firstEmployeeId;

    private String secondEmployeeId;

    private Integer projectId;

    private int workTimeDays;
}
