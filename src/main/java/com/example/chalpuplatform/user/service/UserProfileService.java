package com.example.chalpuplatform.user.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.UserException;
import com.example.chalpuplatform.user.domain.CustomerTaste;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.domain.UserProfile;
import com.example.chalpuplatform.user.dto.CustomerTasteDto;
import com.example.chalpuplatform.user.repository.UserProfileRepository;
import com.example.chalpuplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {
    
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public CustomerTasteDto updateCustomerTaste(Long userId, CustomerTasteDto customerTasteDto) {
        customerTasteDto.validate();
        
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserProfile(userId));
        
        CustomerTaste customerTaste = customerTasteDto.toEntity();
        customerTaste.validateTasteValues();
        
        userProfile.setCustomerTaste(customerTaste);
        userProfileRepository.save(userProfile);
        
        log.info("event=CustomerTasteUpdated, userId={}", userId);
        
        return CustomerTasteDto.from(customerTaste);
    }

    @Transactional
    public CustomerTasteDto getCustomerTaste(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserProfile(userId));
        
        return CustomerTasteDto.from(userProfile.getCustomerTaste());
    }
    
    private UserProfile createNewUserProfile(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        UserProfile newProfile = UserProfile.create(user);
        user.setUserProfile(newProfile);
        return userProfileRepository.save(newProfile);
    }
}