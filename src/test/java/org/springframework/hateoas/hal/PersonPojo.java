package org.springframework.hateoas.hal;

public class PersonPojo {

	private String firstName;
	private String lastName;

	public PersonPojo() {
	}

	public PersonPojo(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
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

	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}
		if (!(o instanceof PersonPojo)) {
			return false;
		}

		PersonPojo that = (PersonPojo)o;

		if (!firstName.equals(that.firstName)) {
			return false;
		}
		if (!lastName.equals(that.lastName)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {

		int result = firstName.hashCode();
		result = 31 * result + lastName.hashCode();
		return result;
	}
}
