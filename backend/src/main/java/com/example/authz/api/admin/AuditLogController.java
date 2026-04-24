package com.example.authz.api.admin;

import com.example.authz.domain.AuditLogEntity;
import com.example.authz.repo.AuditLogRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
  private final AuditLogRepository auditLogRepository;

  public AuditLogController(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  public static final class AuditLogDto {
    public Long id;
    public String action;
    public String targetType;
    public String targetId;
    public String result;
    public String detail;
    public String ip;
    public String createdAt;

    public static AuditLogDto from(AuditLogEntity e) {
      AuditLogDto d = new AuditLogDto();
      d.id = e.getId();
      d.action = e.getAction();
      d.targetType = e.getTargetType();
      d.targetId = e.getTargetId();
      d.result = e.getResult();
      d.detail = e.getDetail();
      d.ip = e.getIp();
      d.createdAt = e.getCreatedAt() == null ? null : e.getCreatedAt().toString();
      return d;
    }
  }

  @PreAuthorize("hasAuthority('audit:read')")
  @GetMapping
  public List<AuditLogDto> list() {
    List<AuditLogEntity> all = auditLogRepository.findAll();
    all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
    List<AuditLogDto> out = new ArrayList<>();
    for (AuditLogEntity e : all) {
      out.add(AuditLogDto.from(e));
    }
    return out;
  }
}
