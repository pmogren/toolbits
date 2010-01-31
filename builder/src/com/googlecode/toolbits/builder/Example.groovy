#!/usr/bin/env groovy
package com.googlecode.toolbits.builder

import com.googlecode.toolbits.builder.PojoBuilder
import com.googlecode.toolbits.builder.Company
import com.googlecode.toolbits.builder.Department
import com.googlecode.toolbits.builder.Employee


static void main(args){
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
}
