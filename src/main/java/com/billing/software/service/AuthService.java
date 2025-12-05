package com.billing.software.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.billing.software.dto.ChangePasswordRequest;
import com.billing.software.dto.ForgotPasswordRequest;
import com.billing.software.dto.ResetPasswordWithOtpRequest;
import com.billing.software.entity.PasswordResetOTP;
import com.billing.software.entity.User;
import com.billing.software.repository.PasswordRepository;
import com.billing.software.repository.UserRepository;

@Service
public class AuthService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private PasswordRepository otpRepository;
	
	@Autowired
	private JavaMailSender mailSender;
	
	
	

	public String changePassword(ChangePasswordRequest changePasswordRequest, String email) {
		
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		
		if(!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
			throw new RuntimeException("Old password did;t match");
		}
		
		if(!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmNewPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
		}
		
		if(passwordEncoder.matches(changePasswordRequest.getConfirmNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password cannot be same as old password");
		}
		
		user.setPassword(changePasswordRequest.getConfirmNewPassword());
		userRepository.save(user);
		
		return "Password Updated Succesfully";
	}
	
	private String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
	
	
	
	@Transactional
	public String sendOtp(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
		
		User user = userRepository.findByEmail(forgotPasswordRequest.getUsername())
				.orElseThrow(() -> new RuntimeException("User Not found"));
		
		if(user.getEmail() == null || user.getEmail().isEmpty()) {
			throw new RuntimeException("User not registered");
		}
		
		String otp = generateOtp();
		
		otpRepository.deleteByUsername(user.getName());

        otpRepository.save(
                PasswordResetOTP.builder()
                        .username(user.getName())
                        .otp(otp)
                        .expiryTime(LocalDateTime.now().plusMinutes(10))
                        .build()
        );
        
        sendEmail(user.getEmail(), otp, user.getName());

        return "OTP sent successfully"; 
	}
	
	
	
	private void sendEmail(String email, String otp, String username) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Password Reset OTP - " + username);
        msg.setText("Your OTP for password reset is: " + otp +
                "\n\nThis OTP expires in 10 minutes.");

        mailSender.send(msg);
    }
	
	
	@Transactional
    public String resetPassword(ResetPasswordWithOtpRequest request) {

        PasswordResetOTP otpData = otpRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Please request OTP again"));

        if (otpData.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepository.deleteByUsername(request.getUsername());
            throw new RuntimeException("OTP expired");
        }

        if (!otpData.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpRepository.deleteByUsername(request.getUsername());

        return "Password reset successfully";
    }

}
