package org.springframework.hateoas.hal;

import static java.util.Arrays.asList;

import java.util.Arrays;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

public class SimpleResourcePojo extends ResourceSupport {

	private String[] roles;

	public SimpleResourcePojo(String[] roles, Link... links) {
		super();
		this.roles = roles;
		
		if(links != null) {
			add(asList(links));
		}
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(roles);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleResourcePojo other = (SimpleResourcePojo) obj;
		if (!Arrays.equals(roles, other.roles))
			return false;
		return true;
	}

}
