package com.googlecode.toolbits.builder


class Company{
	
	public def deptList = []
	public def name = ""
	
	public add(Department d){
		deptList.add(d)
	}
	
	public String print(){
		println "Company: ${name}"
		deptList.each{it.print()}
	}
}

class Department{
	
	public String name = "default"
	public def Employee = []
	
	public String print(){
		println "Department: " + name
		this.Employee.each{it.print()}
	}
}

class Employee{
	public String name = "default"
	
	public String print(){
		println "Employee: " + name
	}
}
