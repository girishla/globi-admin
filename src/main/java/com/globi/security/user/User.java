package com.globi.security.user;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.globi.infa.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Table(name = "M_USERS")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor

public class User extends AbstractEntity {

	@NonNull
	@NotBlank(message = "user name cannot be empty!")
	private String username;

	@NonNull
	@NotBlank(message = "password cannot be empty!")
	private String password;
	
	private String email;
	
	
	private Date lastPasswordReset;
	private String authorities;

}