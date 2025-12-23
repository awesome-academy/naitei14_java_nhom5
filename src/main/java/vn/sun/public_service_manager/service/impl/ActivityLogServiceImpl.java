package vn.sun.public_service_manager.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.sun.public_service_manager.entity.ActivityLog;
import vn.sun.public_service_manager.repository.ActivityLogRepository;
import vn.sun.public_service_manager.service.ActivityLogService;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    public void log(ActivityLog activityLog) {
        activityLogRepository.save(activityLog);
    }

}
