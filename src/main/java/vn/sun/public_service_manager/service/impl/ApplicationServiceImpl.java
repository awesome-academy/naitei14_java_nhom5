package vn.sun.public_service_manager.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import vn.sun.public_service_manager.entity.Application;
import vn.sun.public_service_manager.entity.ApplicationDocument;
import vn.sun.public_service_manager.entity.ApplicationStatus;
import vn.sun.public_service_manager.exception.ResourceNotFoundException;
import vn.sun.public_service_manager.repository.ApplicationDocumentRepository;
import vn.sun.public_service_manager.repository.ApplicationRepository;
import vn.sun.public_service_manager.repository.ApplicationStatusRepository;
import vn.sun.public_service_manager.repository.ServiceRepository;
import vn.sun.public_service_manager.service.ApplicationService;
import vn.sun.public_service_manager.utils.constant.StatusEnum;
import vn.sun.public_service_manager.utils.constant.UploadType;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ServiceRepository serviceRepository;
    private final ApplicationStatusRepository applicationStatusRepository;
    private final ApplicationDocumentRepository applicationDocumentRepository;

    public ApplicationServiceImpl(
            ApplicationRepository applicationRepository,
            ServiceRepository serviceRepository,
            ApplicationStatusRepository applicationStatusRepository,
            ApplicationDocumentRepository applicationDocumentRepository) {
        this.applicationRepository = applicationRepository;
        this.serviceRepository = serviceRepository;
        this.applicationStatusRepository = applicationStatusRepository;
        this.applicationDocumentRepository = applicationDocumentRepository;
    }

    @Transactional
    @Override
    public Application createApplication(Long serviceId, String note, MultipartFile[] files) {
        vn.sun.public_service_manager.entity.Service serviceInDb = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));

        // save application data
        Application application = new Application();
        application.setService(serviceInDb);
        application.setNote(note);
        Application applicationInDb = applicationRepository.save(application);

        // save application status
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setApplication(applicationInDb);
        applicationStatus.setStatus(StatusEnum.PROCESSING);
        applicationStatusRepository.save(applicationStatus);

        // save application documents
        for (MultipartFile file : files) {
            ApplicationDocument applicationDocument = new ApplicationDocument();
            applicationDocument.setApplication(applicationInDb);
            applicationDocument.setFileName(file.getOriginalFilename());
            applicationDocument.setType(UploadType.USER_UPLOAD);

            applicationDocumentRepository.save(applicationDocument);
        }
        return applicationInDb;
    }
}
