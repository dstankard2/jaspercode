package net.sf.jaspercode.example1.obj;

import net.sf.jaspercode.patterns.java.handwritten.DataObject;
import net.sf.jaspercode.patterns.java.handwritten.DataObjectAttribute;

@DataObject(name = "User", priority = 0)
public class User {

	@DataObjectAttribute
	private Integer userId = null;
	
	@DataObjectAttribute
	private String firstName = null;
	
	@DataObjectAttribute
	private String lastName = null;

	@DataObjectAttribute
	private String email = null;

	public User() {
	}

	public User(Integer userId, String firstName, String lastName, String email) {
		super();
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}

