package com.uap.proiv.jobs.service.impl;

import com.uap.proiv.jobs.client.JobApiRepository;
import com.uap.proiv.jobs.dto.Job;
import com.uap.proiv.jobs.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobServiceImpl implements JobService {
    private final JobApiRepository jobApiRepository;

    @Autowired
    public JobServiceImpl(JobApiRepository jobApiRepository) {
        this.jobApiRepository = jobApiRepository;
    }

    @Override
    public List<Job> getAllJobs() {
        // El servicio mantiene el acceso a los trabajos encapsulado en el repositorio.
        return jobApiRepository.getAllJobs();
    }

    @Override
    public Job getJobById(int id) {
        // La API disponible devuelve la colección completa, por eso la búsqueda se resuelve aquí.
        List<Job> jobs = jobApiRepository.getAllJobs();
        return jobs.stream().filter(job -> job.getId() == id).findFirst().orElseThrow();
    }
}
