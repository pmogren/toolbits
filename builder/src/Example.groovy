#!/usr/bin/env groovy

def c = PojoBuilder.build([Dept:Department.class, Employee:Employee.class], Company.class){
	name = "Acme"
	Dept{
		name = "HR"
		Employee{
			name = "John"
		}
		Employee{
			name = "Jack"
		}
	}
	Dept{
		name = "IT"
		Employee{
			name = "Jimmy"
		}
		Employee{
			name = "James"
		}
	}
}

c.print()
