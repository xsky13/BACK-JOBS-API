package com.uap.proiv.jobs.service.impl;

import com.uap.proiv.jobs.dto.*;
import com.uap.proiv.jobs.service.AssignedService;
import com.uap.proiv.jobs.service.JobService;
import com.uap.proiv.jobs.service.UserJobAssignedService;
import com.uap.proiv.jobs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserJobAssignedServiceImpl implements UserJobAssignedService {
    private final JobService jobService;
    private final UserService userService;
    private final AssignedService assignedService;
    @Autowired
    public UserJobAssignedServiceImpl(JobService jobService, UserService userService, AssignedService assignedService) {
        this.jobService = jobService;
        this.userService = userService;
        this.assignedService = assignedService;
    }
    @Override
    public List<UserJobAssigned> assign() {
        // Primero se reúnen los trabajos y la primera página de usuarios.
        List<Job> jobs = jobService.getAllJobs();
        UserApiResponse userApiResponse = userService.search(1);
        List<User> users = new ArrayList<>(userApiResponse.getData());
        int totalPages = userApiResponse.getTotalPages();

        // Se recorren las páginas restantes para trabajar con todos los usuarios disponibles.
        while (userApiResponse != null && userApiResponse.getPage() <= totalPages){
            userApiResponse = userService.search(userApiResponse.getPage() + 1);
            if (userApiResponse != null && userApiResponse.getData() != null) {
                users.addAll(userApiResponse.getData());
            }
        }

        // El asignador trabaja con identificadores; luego se reconstruyen los usuarios completos.
        List<AssignedResponse> assigned = assignedService.create(jobs, users.stream().map(User::getId).toList());

        return jobs.stream().map(job -> {
           List<User> usersJob = assigned.stream().filter(a -> a.jobId() == job.getId())
                   .map(assignedResponse ->
                           users.stream().filter(u -> u.getId() == assignedResponse.userId()).findFirst().orElseThrow()
                   ).toList();
           return new UserJobAssigned(usersJob, job);
        }).toList();

    }
}
