package vn.sun.public_service_manager.service;

import vn.sun.public_service_manager.dto.CitizenProfileResponse;
import vn.sun.public_service_manager.dto.CitizenProfileUpdateRequest;
import vn.sun.public_service_manager.dto.ChangePasswordRequest;

public interface CitizenService {

    CitizenProfileResponse getProfile(String nationalId);

    CitizenProfileResponse updateProfile(String nationalId, CitizenProfileUpdateRequest request);

    void changePassword(String nationalId, ChangePasswordRequest request);
}
