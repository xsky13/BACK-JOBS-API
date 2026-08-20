package com.uap.proiv.jobs.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uap.proiv.jobs.dto.AssignRequest;
import com.uap.proiv.jobs.dto.Job;
import com.uap.proiv.jobs.dto.User;
import com.uap.proiv.jobs.dto.UserApiResponse;
import com.uap.proiv.jobs.dto.UserJobAssigned;
import com.uap.proiv.jobs.service.JobService;
import com.uap.proiv.jobs.service.UserJobAssignedService;
import com.uap.proiv.jobs.service.UserService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class JobControllerTest {

  @Mock
  UserService userService;

  @Mock
  JobService jobService;

  @Mock
  UserJobAssignedService userJobAssignedService;

  @InjectMocks
  JobController jobController;

  private MockMvc mockMvc;

  private UserApiResponse userApiResponse;
  private List<User> users;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(jobController).build();
    objectMapper = new ObjectMapper();

    users = new ArrayList<>();
    User user1 = new User();
    user1.setId(1);
    user1.setEmail("persona@gmail.com");
    user1.setAvatar("null");
    user1.setFirstName("rick");
    user1.setLastName("sanchez");
    users.add(user1);

    User user2 = new User();
    user2.setId(2);
    user2.setEmail("user@as.com");
    user2.setAvatar("null");
    user2.setFirstName("diane");
    user2.setLastName("perez");
    users.add(user2);

    userApiResponse = new UserApiResponse();
    userApiResponse.setPage(1);
    userApiResponse.setPerPage(2);
    userApiResponse.setTotal(2);
    userApiResponse.setTotalPages(1);
    userApiResponse.setData(users);
  }

  @Test
  @DisplayName("GET api /api/job/users/{page} retorna usuarios")
  void getUsers_success_initial_data() throws Exception {
    //userApiResponse.setPage(3);
    when(userService.search(1))
      .thenReturn(userApiResponse)
      .thenReturn(userApiResponse)
      .thenThrow(new RuntimeException("MSG"))
      .thenReturn(userApiResponse);

    mockMvc
      .perform(get("/api/job/users/1"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.data").isArray())
      .andExpect(jsonPath("$.data.length()").value(2))
      .andExpect(jsonPath("$.page").value(1))
      .andExpect(jsonPath("$.total").value(2));

    mockMvc
      .perform(get("/api/job/users/1"))
      .andExpect(status().is5xxServerError());

    mockMvc.perform(get("/api/job/users/1")).andExpect(status().isOk());
  }

  @Test
  @DisplayName(
    "GET api /api/job/users/{page} - Excepcion retornada por el service"
  )
  void getUsers_exception() throws Exception {
    when(userService.search(2)).thenThrow(
      new RuntimeException("Service Error")
    );

    mockMvc
      .perform(get("/api/job/users/2"))
      .andExpect(status().is5xxServerError())
      .andExpect(content().string("Service Error"));
  }

  @Test
  @DisplayName("GET api /api/job/users/{page} retorna usuarios")
  void getUsers_success_set_page() throws Exception {
    userApiResponse.setPage(3);
    when(userService.search(1)).thenReturn(userApiResponse);

    mockMvc
      .perform(get("/api/job/users/1"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.data").isArray())
      .andExpect(jsonPath("$.data.length()").value(2))
      .andExpect(jsonPath("$.page").value(3))
      .andExpect(jsonPath("$.total").value(2));
  }

  @Test
  @DisplayName("POST /api/job/assign - Asignar trabajo a usuario")
  void postAssign_success() throws Exception {
    AssignRequest assignRequest = new AssignRequest();
    assignRequest.setRequestNumber(123);
    assignRequest.setClientName("Name");

    Job job1 = new Job();
    job1.setId(1);
    job1.setName("Manager");
    job1.setSalary(5000);
    job1.setHours(1000);
    job1.setResources(3);

    Job job2 = new Job();
    job2.setId(2);
    job2.setName("Programmer");
    job2.setSalary(1200);
    job2.setHours(200);
    job2.setResources(2);

    List<UserJobAssigned> userJobAssignedList = new ArrayList<>();
    userJobAssignedList.add(new UserJobAssigned(List.of(), job1));
    userJobAssignedList.add(new UserJobAssigned(List.of(), job2));

    when(userJobAssignedService.assign()).thenReturn(userJobAssignedList);

    mockMvc
      .perform(
        post("/api/job/assign")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(assignRequest))
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath(".Assign").isNotEmpty())
      .andExpect(jsonPath(".Assign[0].job.name").value("Manager"))
      .andExpect(jsonPath(".Assign[1].job.name").value("Programmer"))
      .andExpect(jsonPath(".Assign[0].users[0].first_name").value("rick"))
      .andExpect(jsonPath(".Request_number").value(123))
      .andExpect(jsonPath(".Name").value("Name"));
  }
}
