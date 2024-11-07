package com.brianuceda.sserafimflow.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.brianuceda.sserafimflow.dtos.CompanyDashboard;

@Service
public class CompanyUpdateService {
  private final SimpMessagingTemplate messagingTemplate;

  public CompanyUpdateService(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void sendDashboardUpdate(CompanyDashboard updatedDashboard) {
    messagingTemplate.convertAndSend("/topic/dashboard", updatedDashboard);
  }
}
