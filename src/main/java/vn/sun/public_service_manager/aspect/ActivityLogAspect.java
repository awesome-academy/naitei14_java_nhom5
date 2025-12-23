package vn.sun.public_service_manager.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import vn.sun.public_service_manager.entity.ActivityLog;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.service.ActivityLogService;
import vn.sun.public_service_manager.service.CitizenService;
import vn.sun.public_service_manager.service.UserManagementService;
import vn.sun.public_service_manager.utils.SecurityUtil;
import vn.sun.public_service_manager.utils.annotation.LogActivity;
import vn.sun.public_service_manager.utils.constant.ActorType;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLogAspect {

    private final ActivityLogService activityLogService;
    private final CitizenService citizenService;
    private final UserManagementService userService;

    @AfterReturning(value = "@annotation(vn.sun.public_service_manager.utils.annotation.LogActivity)", returning = "result")
    public void logActivity(JoinPoint joinPoint, Object result) {
        LogActivity logActivity = getLogActivityAnnotation(joinPoint);

        String actorName = SecurityUtil.getCurrentUserName();
        ActivityLog activityLog = new ActivityLog();
        if (actorName != null && !actorName.equals("anonymousUser")) {
            var user = userService.getByUsername(actorName);
            if (user != null) {
                activityLog.setActorId(user.getId());
            } else {
                Citizen citizen = citizenService.getByNationalId(actorName);
                activityLog.setActorId(citizen.getId());
            }
        }
        Object[] args = joinPoint.getArgs();
        Long targetId = null;
        for (Object arg : args) {
            if (arg instanceof Long) {
                targetId = (Long) arg; // Giả sử targetId là kiểu Long
                break;
            }
        }
        activityLog.setTargetId(targetId);
        activityLog.setActorType(ActorType.CITIZEN);
        activityLog.setTargetType(logActivity.targetType());
        activityLog.setDescription(logActivity.description());
        activityLog.setAction(logActivity.action());

        activityLogService.log(activityLog);
        System.out.println("Activity logged: " + activityLog);
    }

    @AfterThrowing(value = "@annotation(vn.sun.public_service_manager.utils.annotation.LogActivity)", throwing = "ex")
    public void logActivityFailure(JoinPoint joinPoint, Throwable ex) {
        LogActivity logActivity = getLogActivityAnnotation(joinPoint);

        String nationalId = SecurityUtil.getCurrentUserName();
        Citizen citizen = citizenService.getByNationalId(nationalId);

        ActivityLog activityLog = new ActivityLog();
        activityLog.setActorId(citizen.getId());
        activityLog.setActorType(ActorType.CITIZEN);
        activityLog.setTargetType(logActivity.targetType());
        activityLog.setDescription(
                "Failed to perform action: " + logActivity.description() + ". Reason: " + ex.getMessage());
        activityLog.setAction(logActivity.action());

        activityLogService.log(activityLog);
        log.warn("Activity failed and logged: {}", activityLog);
    }

    private LogActivity getLogActivityAnnotation(JoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature())
                .getMethod()
                .getAnnotation(LogActivity.class);
    }
}