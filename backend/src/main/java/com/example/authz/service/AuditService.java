package com.example.authz.service;

import com.example.authz.domain.AuditLogEntity;
import com.example.authz.domain.UserEntity;
import com.example.authz.repo.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
  private final AuditLogRepository auditLogRepository;

  public AuditService(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  @Transactional
  public void log(
      UserEntity actorOrNull,
      String action,
      String targetType,
      String targetId,
      String result,
      String detail,
      String ip,
      String ua) {
    AuditLogEntity e = new AuditLogEntity();
    e.setActorUser(actorOrNull);
    e.setAction(action);
    e.setTargetType(targetType);
    e.setTargetId(targetId);
    e.setResult(result);
    e.setDetail(detail);
    e.setIp(ip);
    e.setUa(ua);
    auditLogRepository.save(e);
  }
}
