package com.example.employeeapp.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    private String employeeId;

    private Integer projectId;

    private Date dateFrom;

    private Date dateTo;
}
